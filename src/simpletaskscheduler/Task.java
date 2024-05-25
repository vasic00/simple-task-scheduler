package simpletaskscheduler;

import java.util.Date;

abstract public class Task implements Runnable, Comparable<Task> {

	public enum Priority
	{
		VERY_LOW,
		LOW,
		MEDIUM,
		HIGH,
		VERY_HIGH,
		CRITICAL;
	}
	
	public enum State
	{
		START_STATE,
		INTERM_STATE_1, // intermediate states
		INTERM_STATE_2,
		INTERM_STATE_3,
		INTERM_STATE_4,
		INTERM_STATE_5,
		FINAL_STATE,
		STOP_STATE
	}
	
	private Priority priority;
	private volatile State state = State.START_STATE;
	
	private volatile boolean isPaused;
	
	private final TaskResource taskResource;
	
	private boolean isScheduled;
	private boolean isCompleted;
	
	private volatile boolean isStopped;
	
	private final boolean isSelfThread;
	
	private final long allowedTime;
	private long startTime;
	
	private final Date startDate;
	private final Date endDate;
	
	
	public Task(TaskResource taskResource, Priority priority, boolean isSelfThread)
	{
		this.taskResource = taskResource;
		this.priority = priority;
		this.isSelfThread = isSelfThread;
		
		allowedTime = 0;
		startDate = null;
		endDate = null;
	}
	
	public Task(TaskResource taskResource, Priority priority, boolean isSelfThread, long allowedTime, Date startDate, Date endDate)
	{
		this.taskResource = taskResource;
		this.priority = priority;
		this.isSelfThread = isSelfThread;
		this.allowedTime = allowedTime;
		this.startDate = startDate;
		this.endDate = endDate;
	}
	
	// execution starts in this method
	// up to the user to implement
	@Override
	abstract public void run();
	
	@Override
	public int compareTo(Task other)
	{
		return other.getPriority().compareTo(this.getPriority());
	}
	
	public TaskResource getTaskResource()
	{
		return taskResource;
	}
	
	boolean getIsSelfThread()
	{
		return isSelfThread;
	}
	
	void setPriority(Priority priority)
	{
		this.priority = priority;
	}
	
	Priority getPriority()
	{
		return priority;
	}

	boolean getIsScheduled()
	{
		return isScheduled;
	}
	
	void setIsScheduled(boolean status)
	{
		isScheduled = status;
	}
	
	boolean getIsCompleted()
	{
		return isCompleted;
	}
	
	void setIsCompleted(boolean status)
	{
		isCompleted = status;
	}
	
	
	void setIsPaused(boolean status)
	{
		isPaused = status;
	}
	
	
	private final Object pauseLock = new Object();
	
	protected void waitIfPaused()
	{
		synchronized(pauseLock)
		{
			if (isPaused)
			{
				try
				{
					pauseLock.wait();
				}
				catch (InterruptedException e)
				{}
			}
		}
	}
	
	Object getPauseLock()
	{
		return pauseLock;
	}
	
	public boolean getIsPaused()
	{
		return isPaused;
	}
	
	protected void setState(State state)
	{
		this.state = state;
	}
	
	public State getState()
	{
		return state;
	}
	
	protected boolean transitionInNaturalOrder()
	{
		if (state != State.FINAL_STATE && state != State.STOP_STATE)
		{
			state = State.values()[state.ordinal()+1];
			return true;
		}
		return false;
	}
	
	void setIsStopped(boolean status)
	{
		if (!isStopped)
		{
			isStopped = status;
		}
	}
	
	public boolean getIsStopped()
	{
		return isStopped;
	}
	
	public long getAllowedTime()
	{
		return allowedTime;
	}
	
	public Date getStartDate()
	{
		return startDate;
	}
	
	public Date getEndDate()
	{
		return endDate;
	}
	
	public void updateStartTime()
	{
		startTime = System.currentTimeMillis();
	}
	
	public long getStartTime()
	{
		return startTime;
	}
}
