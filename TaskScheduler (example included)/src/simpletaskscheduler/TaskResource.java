package simpletaskscheduler;


import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// just an example of a resource
// NYI - not yet implemented (not properly)!

abstract public class TaskResource {

	private final Lock lock;
	
	public TaskResource(Lock lock)
	{
		if (lock != null)
			this.lock = lock;
		else this.lock = new ReentrantLock();
	}
	
	public TaskResource()
	{
		this.lock = new ReentrantLock();
	}
	
	public Lock getLock()
	{
		return lock;
	}
}
