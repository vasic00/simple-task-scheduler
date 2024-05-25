package application;

import simpletaskscheduler.*;

/**
 * Klasa koja sadrzi samo main metodu, radi testiranja pripreme za trecu laboratorijsku vjezbu.
 * 
 * @author Mihailo Vasić
 */
public class Application2 {

	public static void main(String[] args)
	{
		Scheduler scheduler = new Scheduler(Scheduler.Scheduling.FCFS, 1, 1);
		Task task1 = new TaskExample2();
		Task task2 = new TaskExample2();
		Task task3 = new TaskExample2();
		Task task4 = new TaskExample2();
		
		scheduler.scheduleForExecution(task1);
		scheduler.scheduleForExecution(task2);
		scheduler.scheduleForExecution(task3);
		TaskWaiter taskWaiter4 = scheduler.scheduleForExecution(task4);
	
		/*
		sleep(500); 
		scheduler.tryStop(task4);
		taskWaiter4.waitForCompletion();
		scheduler.tryStop(task1);
		scheduler.tryPause(task2);
		sleep(300);
		scheduler.tryPause(task3);
		scheduler.tryResume(task2);
		sleep(5000);
		System.out.println("Main spavao 5 sekundi, sada odpauzira zadatak 3");
		scheduler.tryResume(task3);
		*/
		sleep(700);
		scheduler.tryPause(task1);
		scheduler.tryPause(task2);
		scheduler.tryStop(task3);
		sleep(3000);
		System.out.println("Nastavljamo zadatak 1 i zadatak 2");
		scheduler.tryResume(task1);
		sleep(1000);
		scheduler.tryResume(task2);
		
	}
	
	private static void sleep(int sleepTime)
	{
		try
		{
			Thread.sleep(sleepTime);
		}
		catch (InterruptedException e)
		{}
	}
}
