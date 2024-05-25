package simpletaskscheduler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import application.TaskExample;

import java.util.Queue;

// thread that starts threads for tasks that have isSelfThread == true

public class ThreadStarter extends AbstractExecutorThread {
	
	private final static int SLEEP_TIME = 300;
	
	ThreadStarter(Object taskLock, TaskCounterWrapper taskCounter, Queue<Task> taskQueue, HashMap<Task, Thread> taskMap, HashSet<Task> pausedTaskSet)
	{
		super(taskLock, taskCounter, taskQueue, taskMap, pausedTaskSet);
	}
	@Override
	public void run()
	{
		// map for all the tasks that are executing on their own threads
		// using this map instead of taskMap to traverse faster
		HashMap<Task, Thread> forkedTaskMap = new HashMap<>();
		
		while(true)
		{		
			synchronized(taskLock)
			{
				if (getStopExecuting())
					return;
				
				Task task = getTaskQueue().peek();
				
				if (executableElsewhere(task))
					taskLock.notify();
				
				if (forkedTaskMap.isEmpty() && !executableHere(task))
				{
					try
					{
						taskLock.wait();
					}
					catch (InterruptedException e){}
					
					task = getTaskQueue().peek();
					
					if (executableElsewhere(task))
						taskLock.notify();
				}
				
				for (; executableHere(task); task = getTaskQueue().peek())
				{
					task = getTaskQueue().poll();
					Thread thread = new Thread(task);
					forkedTaskMap.put(task, thread);
					getTaskMap().put(task, thread);
					TaskExample taskExample = (TaskExample)task;
					System.out.println("Sopstvena nit uzima " + "zadatak " + taskExample.ID + " iz reda (njegov prioritet: " + task.getPriority() + ")");
					thread.start();
					incrementNumberOfConcurrentTasks();
				}
			}
			
			// are there any dead threads? (threads with completed tasks)
			// if yes, remove them from the map
			// decrement the number of executing tasks to allow scheduler to execute more tasks
			// notify scheduler's worker thread
			Set<Entry<Task, Thread>> entrySet = forkedTaskMap.entrySet();
			
			for (Iterator<Entry<Task, Thread>> it = entrySet.iterator(); it.hasNext();)
			{
				Entry<Task, Thread> entry = it.next();
				
				if (!entry.getValue().isAlive())
				{
					Task task = entry.getKey();
					
					synchronized(taskLock)
					{
						removeSingleTaskFromTaskMap(task);
						taskPausedOrStopped(task);
						
						taskLock.notify();
					}
					
					it.remove();
				}
			}
			
			// repeat everything every SLEEP_TIME number of ms
			try
			{
				Thread.sleep(SLEEP_TIME);
			}
			catch(InterruptedException e){}
		}
	}
	
	
	@Override
	boolean executableHere(Task task)
	{
		return executableNow(task) && task.getIsSelfThread();
	}
	
	@Override
	boolean executableElsewhere(Task task)
	{
		return executableNow(task) && !task.getIsSelfThread();
	}
}
