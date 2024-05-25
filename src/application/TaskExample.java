package application;

import java.util.Date;
import java.util.Random;

import simpletaskscheduler.Task;
import simpletaskscheduler.TaskExecutorThread;
import simpletaskscheduler.TaskResource;


/**
 * Priprema za drugu lab vjezbu: 
 * Primjer implementacije zadatka gdje se moze vidjeti izvrsavanje zadatka na radnoj i sopstvenoj niti.
 * 
 * @author Mihailo Vasić
 */
public class TaskExample extends Task
{
	private static int counter = 1;
	public int ID;
	private volatile Thread thread;
	
	private volatile boolean done = false;
	
	private volatile long doneTime;
	
	private boolean shouldWait = false;
	private final Object shouldWaitLock = new Object();
	private boolean isWaiting = false;
	
	public TaskExample()
	{
		super(new TaskResource() {}, Priority.values()[new Random().nextInt(6)], new Random().nextBoolean());
		updateCounter();
	}
	
	public TaskExample(Priority priority, boolean isSelfThread)
	{
		super(new TaskResource() {}, priority, isSelfThread);
		updateCounter();
	}
	
	public TaskExample(Priority priority, boolean isSelfThread, long allowedTime, Date startDate, Date endDate)
	{
		super(new TaskResource() {}, priority, isSelfThread, allowedTime, startDate, endDate);
		updateCounter();
	}
	
	private void updateCounter()
	{
		ID = counter;
		counter++;
	}
	
	// koristio sam new Random().nextInt(2000) za vrijeme spavanja
	// cisto da ne izgleda da sam "namjestio" vrijeme
	// 100 je gornja granica (iskljuceno), 0 je donja granica (ukljuceno) za int koji dobijemo
	private static final int SLEEP_TIME_EXAMPLE = 100;
	
	@Override
	public void run()
	{
		thread = Thread.currentThread();
		
		synchronized(shouldWaitLock)
		{
			if (shouldWait)
				try {
					isWaiting = true;
					shouldWaitLock.notify();
					shouldWaitLock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		for (int i = 0; i < 5; i++)
		{
			System.out.println("*Zadatak " + ID + ", iteracija broj " + String.valueOf(i+1) + "*");
			try {
				Thread.sleep(new Random().nextInt(SLEEP_TIME_EXAMPLE));
			} catch (InterruptedException e) {
				System.out.println("Zadatak " + ID + " prekinut");
				return;
			}
		}
		System.out.println("Zadatak " + ID + " zavrsio");
		
		done = true;
		doneTime = System.currentTimeMillis();
	}
	
	public Thread getThread()
	{
		return thread;
	}
	
	public boolean getDone()
	{
		return done;
	}
	
	public long getDoneTime()
	{
		return doneTime;
	}
	
	public void setShouldWait(boolean status)
	{
		synchronized(shouldWaitLock)
		{
			shouldWait = status;
			if (!shouldWait)
				shouldWaitLock.notify();
		}
	}
	
	public void waitIfTaskIsNotWaiting()
	{
		synchronized(shouldWaitLock)
		{
			if (!isWaiting)
				try {
					shouldWaitLock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
}
