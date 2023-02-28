package simpletaskscheduler;

import java.util.HashSet;

public class DateManagerThread extends Thread {

	private final Scheduler scheduler;
	private final HashSet<Task> tasks = new HashSet<>();
	
	DateManagerThread(Scheduler scheduler)
	{
		this.scheduler = scheduler;
	}
	
	@Override
	public void run()
	{
		while(!scheduler.isOff())
		{
			synchronized(this)
			{
				if (tasks.isEmpty())
					try {
						this.wait();
					} catch (InterruptedException e) {}
				
				for (Task t : tasks)
				{	
				
					if (t.getStartDate() != null &&  t.getStartDate().getTime() <= System.currentTimeMillis())
						this.notifyAll();
					
					if (t.getEndDate() != null && t.getEndDate().getTime() <= System.currentTimeMillis())
					{
						scheduler.tryStop(t);
						removeTask(t);
					}
					else if (t.getAllowedTime() > 0 && t.getStartTime() > 0 && t.getAllowedTime() <= System.currentTimeMillis() - t.getStartTime())
					{
						scheduler.tryStop(t);
						removeTask(t);
					}
				}
			}
			
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {}
		}
	}
	
	public void addTask(Task t)
	{
		tasks.add(t);
	}
	
	public void removeTask(Task t)
	{
		tasks.remove(t);
	}
}
