package simpletaskscheduler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;

public abstract class AbstractExecutorThread extends Thread {
	
	final Object taskLock;
	private final TaskCounterWrapper taskCounter;
	private final Queue<Task> taskQueue;
	private final HashMap<Task, Thread> taskMap;
	private final HashSet<Task> pausedTaskSet;
	
	AbstractExecutorThread(Object taskLock, TaskCounterWrapper taskCounter, Queue<Task> taskQueue, HashMap<Task, Thread> taskMap, HashSet<Task> pausedTaskSet)
	{
		this.taskLock = taskLock;
		this.taskCounter = taskCounter;
		this.taskQueue = taskQueue;
		this.taskMap = taskMap;
		this.pausedTaskSet = pausedTaskSet;
	}
	
	Queue<Task> getTaskQueue()
	{
		return taskQueue;
	}
	
	HashMap<Task, Thread> getTaskMap()
	{
		return taskMap;
	}
	
	HashSet<Task> getPausedTaskSet()
	{
		return pausedTaskSet;
	}
	int getMaxNumberOfConcurrentTasks()
	{
		return taskCounter.getMaxNumberOfConcurrentTasks();
	}
	
	int getNumberOfConcurrentTasks()
	{
		return taskCounter.getNumberOfConcurrentTasks();
	}
	
	boolean reachedMaxNumberOfConcurrentTasks()
	{
		return taskCounter.reachedMaxNumberOfConcurrentTasks();
	}
	
	void incrementNumberOfConcurrentTasks()
	{
		taskCounter.incrementNumberOfConcurrentTasks();
	}
	
	void decrementNumberOfConcurrentTasks()
	{
		taskCounter.decrementNumberOfConcurrentTasks();
	}
	
	private boolean stopExecuting;
	
	void setStopExecuting(boolean status)
	{
		stopExecuting = status;
	}
	
	boolean getStopExecuting()
	{
		return stopExecuting;
	}

	boolean executableNow(Task task)
	{
		return task != null && !reachedMaxNumberOfConcurrentTasks();
	}
	
	void signalTaskCompletion(Task task)
	{
		synchronized(task)
		{
			task.setIsScheduled(false);
			task.setIsCompleted(true);
			task.notifyAll();
		}
	}
	
	void taskPausedOrStopped(Task task)
	{
		
		if (taskStopped(task))
			return;
		taskPaused(task);
	}
	
	private boolean taskStopped(Task task)
	{
		if (task.getIsStopped())
		{
			task.setIsPaused(false);
			pausedTaskSet.remove(task);
			signalTaskCompletion(task);
			
			if (task.getState() != Task.State.FINAL_STATE)
				task.setState(Task.State.STOP_STATE);
			return true;
		}
		return false;
	}
	
	private void taskPaused(Task task)
	{
		// ensure it reaches final state if there were no pauses/stops
		if (!task.getIsPaused() && !pausedTaskSet.contains(task))
			task.setState(Task.State.FINAL_STATE);
		
		if (task.getState() == Task.State.FINAL_STATE)
		{
			signalTaskCompletion(task);
			pausedTaskSet.remove(task);
			task.setIsPaused(false);
		}
		else if (!task.getIsPaused() && pausedTaskSet.contains(task))
		{
			pausedTaskSet.remove(task);
			taskQueue.offer(task);
		}
	}
	
	void removeSingleTaskFromTaskMap(Task task)
	{
		taskMap.remove(task);
		decrementNumberOfConcurrentTasks();
	}
	
	abstract boolean executableHere(Task task);
	abstract boolean executableElsewhere(Task task);
}
