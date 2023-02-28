package simpletaskscheduler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.locks.Lock;

import application.TaskExample;

public class TaskExecutorThread extends AbstractExecutorThread {
	
	private final ThreadStarter threadStarter;
	private final DateManagerThread dateManagerThread;
	
	private boolean needsHelp;

	TaskExecutorThread(Object taskLock, TaskCounterWrapper taskCounter, Queue<Task> taskQueue, HashMap<Task, Thread> taskMap,
			HashSet<Task> pausedTaskSet, ThreadStarter threadStarter, DateManagerThread dateManagerThread)
	{
		super(taskLock, taskCounter, taskQueue, taskMap, pausedTaskSet);
		this.threadStarter = threadStarter;
		this.dateManagerThread = dateManagerThread;
	}
	
	@Override
	public void run()
	{
		Task task = null;
		while(true)
		{
			
			synchronized(taskLock)
			{
				for (task = getTaskQueue().peek(); !executableHere(task); task = getTaskQueue().peek())
				{
					if (getStopExecuting())
						return;
					
					if (executableElsewhere(task))
						threadStarter.interrupt();
					
					try
					{
						taskLock.wait();
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
				
				task = getTaskQueue().poll();
				
				TaskExample taskExample = (TaskExample)task;
				System.out.println("Radna nit uzima " + "zadatak " + taskExample.ID + " iz reda (njegov prioritet: " + task.getPriority() + ")");
				
				incrementNumberOfConcurrentTasks();
				
				if (executableElsewhere(getTaskQueue().peek()));
					threadStarter.interrupt();
				
				getTaskMap().put(task, this);
			}
			
			synchronized(dateManagerThread)
			{
				while (task.getStartDate() != null && task.getStartDate().getTime() > System.currentTimeMillis())
					try {
						dateManagerThread.addTask(task);
						dateManagerThread.notifyAll();
						dateManagerThread.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
				dateManagerThread.removeTask(task);
				
				if (task.getAllowedTime() > 0 || task.getEndDate() != null)
				{
					dateManagerThread.addTask(task);
					dateManagerThread.notifyAll();
					task.updateStartTime();
				}
			}
			
				try
				{
					// execute the task
					task.run();
				}
				catch (RuntimeException | Error e) // run method can potentially throw unchecked exceptions, we don't want it crashing our worker thread
				{
					e.printStackTrace();
				}
				finally
				{
					synchronized(taskLock)
					{
						removeSingleTaskFromTaskMap(task);
						// minor deadlock protection?
						if (task.getIsStopped())
						{
							// our task got interrupted
							// we unlock its resource and queue it up for execution again
							try
							{
								Lock lock = task.getTaskResource().getLock();
								while(true)
								{
									// this will eventually throw IllegalMonitorStateException and break the loop
									// if this thread doesn't hold any locks on lock, and tries to unlock it - exception is thrown
									// keep unlocking until it runs out of locks
									// stack trace isn't big so this should be affordable
									lock.unlock();
								}
							}
							catch (IllegalMonitorStateException e)
							{
								// expected exception, do nothing
							}
						}
						
						taskPausedOrStopped(task);
					}
				}
		}
	}
	
	@Override
	boolean executableHere(Task task)
	{
		return executableNow(task) && !task.getIsSelfThread();
	}
	
	@Override
	boolean executableElsewhere(Task task)
	{
		return executableNow(task) && task.getIsSelfThread();
	}
	
	public boolean getNeedsHelp()
	{
		return needsHelp;
	}
	
	public void setNeedsHelp(boolean status)
	{
		needsHelp = status;
	}
	
}
