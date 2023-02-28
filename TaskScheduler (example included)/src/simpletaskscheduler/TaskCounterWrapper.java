package simpletaskscheduler;

public class TaskCounterWrapper {

	private int numberOfConcurrentTasks;
	private final int maxNumberOfConcurrentTasks;
	
	TaskCounterWrapper(int numberOfConcurrentTasks, int maxNumberOfConcurrentTasks)
	{
		this.numberOfConcurrentTasks = numberOfConcurrentTasks;
		this.maxNumberOfConcurrentTasks = maxNumberOfConcurrentTasks;
	}
	
	int getMaxNumberOfConcurrentTasks()
	{
		return maxNumberOfConcurrentTasks;
	}
	
	int getNumberOfConcurrentTasks()
	{
		return numberOfConcurrentTasks;
	}
	
	boolean reachedMaxNumberOfConcurrentTasks()
	{
		return numberOfConcurrentTasks == maxNumberOfConcurrentTasks;
	}
	
	void incrementNumberOfConcurrentTasks()
	{
		numberOfConcurrentTasks++;
	}
	
	void decrementNumberOfConcurrentTasks()
	{
		numberOfConcurrentTasks--;
	}
}
