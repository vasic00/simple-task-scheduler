package simpletaskscheduler;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;

import application.TaskExample;


public class Scheduler {
	
	
	public enum Scheduling
	{
		FCFS,
		PRIORITY;
	}
	
	// all worker threads
	private final ArrayList<TaskExecutorThread> workerThreads = new ArrayList<>();
	
	// all tasks that are scheduled for execution (tasks that worker threads will poll to execute)
	// needs to be synchronized with taskLock
	private final Queue<Task> taskQueue;
	
	// map made to see which thread is executing which task
	// needs to be synchronized with taskLock
	// has no use at the moment
	// TODO: use this map to stop tasks that have been executing for too long
	private final HashMap<Task, Thread> taskMap = new HashMap<>();
	
	// this is also a thread, made to start threads for tasks that have isSelfThread set to true
	private final ThreadStarter threadStarter;
	
	// all tasks that we attempted to pause
	private final HashSet<Task> pausedTaskSet = new HashSet<>();
	
	private final DateManagerThread dateManagerThread;
	
	private final Object taskLock = new Object();
	
	private volatile boolean off = false;
	
	public Scheduler(Scheduling scheduling, int numberOfWorkerThreads, int maxNumberOfConcurrentTasks)
	{
		if (scheduling.equals(Scheduling.FCFS))
			taskQueue = new ArrayDeque<>();
		else taskQueue = new PriorityQueue<>();
		
		if (numberOfWorkerThreads < 1 || maxNumberOfConcurrentTasks < 1)
			throw new IllegalArgumentException();
		
		dateManagerThread = new DateManagerThread(this);
		dateManagerThread.start();
		
		TaskCounterWrapper taskCounter = new TaskCounterWrapper(0, maxNumberOfConcurrentTasks);
		threadStarter = new ThreadStarter(taskLock, taskCounter, taskQueue, taskMap, pausedTaskSet);
		threadStarter.start();
		for (int i = 0; i < numberOfWorkerThreads; i++)
		{
			TaskExecutorThread thread = new TaskExecutorThread(taskLock, taskCounter, taskQueue, taskMap, pausedTaskSet, threadStarter, dateManagerThread);
			workerThreads.add(thread);
			thread.start();
		}
	}
	
	public TaskWaiter scheduleForExecution(Task task)
	{
		// disallow further scheduling if scheduler is off
		if (off)
			throw new RuntimeException(); // TODO: custom exception
		
		// no null tasks
		if (task == null)
			throw new NullPointerException();
		
		// make tasks executable only once
		synchronized(task)
		{
			// if task is already scheduled for execution or completed execution, throw some exception
			// if task is executing, but gets interrupted, it will count as scheduled until it completes its execution
			// worker thread will enqueue it again in that case
			if (task.getIsScheduled() || task.getIsCompleted())
				throw new RuntimeException(); // TODO: custom exception
			task.setIsScheduled(true);
		}
		
		synchronized(taskLock)
		{
			TaskExample te = (TaskExample)task;
			if (task.getIsSelfThread())
				System.out.println("Postavljanje zadatka " + te.ID + " u red, treba da ga uzme sopstvena nit");
			else System.out.println("Postavljanje zadatka " + te.ID + " u red, treba da ga uzme radna nit");
			
			// NYI: interrupting lower priority tasks
			/*
			if (!task.getIsSelfThread() && taskMap.size() == workerThreads.size())
			{
				boolean taskOffered = false;
				Set<Map.Entry<Task, TaskExecutorThread>> taskEntrySet = taskMap.entrySet();
				
				for (Map.Entry<Task, TaskExecutorThread> singleEntry : taskEntrySet)
				{
					
					if (task.compareTo(singleEntry.getKey()) > 0)
					{
						taskQueue.offer(task);
						// up to the user to implement tasks that benefit from interruption, or that are interruptible
						// only some minor deadlock protection is implemented
						singleEntry.getValue().interrupt(); 
						System.out.println("Zadatak prekinut");
						taskOffered = true;
						break;
					}
					
				}
				if (!taskOffered)
					taskQueue.offer(task);
				
			}
			*/
			// might need else here
			
			taskQueue.offer(task);
			
			if (task == taskQueue.peek() && task.getIsSelfThread())
				threadStarter.interrupt();
			else taskLock.notify();
			
		}
		
		return new TaskWaiter(task);
		
	}
	
	// interrupting will "overwrite" pausing
	// if a task is both paused and interrupted, it will be treated as interrupted
	// which means worker thread will automatically enqueue it again
	
	public boolean tryPause(Task task)
	{
		synchronized(taskLock)
		{
			if (taskMap.get(task) != null || taskQueue.contains(task))
			{
				System.out.println("PAUZA " + ((TaskExample)task).ID);
				task.setIsPaused(true);
				pausedTaskSet.add(task);
				return true;
			}
		}
		return false;
	}
	
	public boolean tryResume(Task task)
	{
		synchronized(taskLock)
		{
			if (pausedTaskSet.contains(task) && task.getIsPaused())
			{
				System.out.println("RESUME " + ((TaskExample)task).ID);
				
				synchronized(task.getPauseLock())
				{
					task.setIsPaused(false);
					task.getPauseLock().notifyAll();
				}
				
				if (taskMap.get(task) == null)
				{
					taskQueue.offer(task);
					pausedTaskSet.remove(task);
				}
			
				if (taskQueue.peek() == task)
				{
					if (task.getIsSelfThread())
						threadStarter.interrupt();
					else taskLock.notify();
				}
				return true;
			}
		}
		return false;
	}
	
	public boolean tryStop(Task task)
	{	
		synchronized(taskLock)
		{	
			if (taskMap.get(task) != null || taskQueue.contains(task))
			{
				task.setIsStopped(true);
				return true;
			}
		}
		
		return false;
	}
	
	public void shutdown()
	{
		// can't shutdown more than once
		if (off)
			throw new RuntimeException(); // TODO: custom exception
		off = true;
		 
		// this will stop all queued tasks from executing
		// TODO: better implementation (do we really have to stop queued tasks from executing?)
		// or just disallow further scheduling?
		
		dateManagerThread.interrupt();
		
		PriorityQueue<Task> queue = new PriorityQueue<>();
		queue.addAll(taskQueue);
		
		synchronized(taskLock)
		{
			taskQueue.clear();
			taskMap.clear();
			
			for (TaskExecutorThread t : workerThreads)
				t.setStopExecuting(true);
			threadStarter.setStopExecuting(true);
			taskLock.notifyAll();
		}
		
		for (Task task : queue)
		{
			synchronized(task)
			{
				task.setIsScheduled(false);
				task.setIsCompleted(true);
				task.notifyAll();
			}
		}
	}
	
	public boolean isOff()
	{
		return off;
	}
	
	public ArrayList<TaskExecutorThread> getWorkerThreads()
	{
		return workerThreads;
	}
}
