package simpletaskscheduler;

public class TaskWaiter {

	private Task task;
	
	public TaskWaiter(Task task)
	{
		this.task = task;
	}
	
	public void waitForCompletion()
	{
		synchronized(task)
		{
			if (!task.getIsCompleted() && task.getIsScheduled())
			{
				try
				{
					task.wait();
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
}
