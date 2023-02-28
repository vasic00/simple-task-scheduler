package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import simpletaskscheduler.*;

import org.junit.jupiter.api.Test;

import application.TaskExample;
import application.TaskExample2;

class SchedulerTests {

	// NYI
	// TODO
	
	@Test
	void taskShouldBeCompleted()
	{
		Scheduler scheduler = new Scheduler(Scheduler.Scheduling.PRIORITY, 1, 1);
		TaskExample task = new TaskExample(Task.Priority.MEDIUM, false);
		TaskWaiter taskWaiter = scheduler.scheduleForExecution(task);
		taskWaiter.waitForCompletion();
		
		assert task.getDone();
		
		scheduler.shutdown();
	}
	
	@Test
	void priorityQueueShouldBeRespected()
	{
		Scheduler scheduler = new Scheduler(Scheduler.Scheduling.PRIORITY, 1, 1);
		TaskExample task1 = new TaskExample(Task.Priority.MEDIUM, false);
		TaskExample task2 = new TaskExample(Task.Priority.LOW, false);
		TaskExample task3 = new TaskExample(Task.Priority.HIGH, false);
		
		task1.setShouldWait(true);
		
		scheduler.scheduleForExecution(task1);
		task1.waitIfTaskIsNotWaiting();
		TaskWaiter taskWaiter2 = scheduler.scheduleForExecution(task2);
		TaskWaiter taskWaiter3 = scheduler.scheduleForExecution(task3);
		
		task1.setShouldWait(false);
		
		taskWaiter2.waitForCompletion();
		taskWaiter3.waitForCompletion();
		
		assert task2.getDoneTime() > task3.getDoneTime();
		
		scheduler.shutdown();
	}
	
	@Test
	void fcfsQueueShouldBeRespected()
	{
		Scheduler scheduler = new Scheduler(Scheduler.Scheduling.FCFS, 1, 1);
		TaskExample task1 = new TaskExample();
		TaskExample task2 = new TaskExample();
		TaskExample task3 = new TaskExample();
		
		scheduler.scheduleForExecution(task1);
		TaskWaiter taskWaiter2 = scheduler.scheduleForExecution(task2);
		TaskWaiter taskWaiter3 = scheduler.scheduleForExecution(task3);
		taskWaiter2.waitForCompletion();
		taskWaiter3.waitForCompletion();
		
		assert task2.getDoneTime() < task3.getDoneTime();
		
		scheduler.shutdown();
	}
	
	@Test
	void taskShouldExecuteOnWorkerThread()
	{
		Scheduler scheduler = new Scheduler(Scheduler.Scheduling.PRIORITY, 1, 1);
		TaskExample task = new TaskExample(Task.Priority.MEDIUM, false);
		TaskWaiter taskWaiter = scheduler.scheduleForExecution(task);
		taskWaiter.waitForCompletion();
		
		assert task.getThread() instanceof TaskExecutorThread;
		
		scheduler.shutdown();
	}
	
	@Test
	void taskShouldExecuteOnSelfThread()
	{
		Scheduler scheduler = new Scheduler(Scheduler.Scheduling.PRIORITY, 1, 1);
		TaskExample task = new TaskExample(Task.Priority.MEDIUM, true);
		TaskWaiter taskWaiter = scheduler.scheduleForExecution(task);
		taskWaiter.waitForCompletion();
		
		assert !(task.getThread() instanceof TaskExecutorThread);
		
		scheduler.shutdown();
	}
	
	@Test
	void taskShouldBePaused()
	{
		Scheduler scheduler = new Scheduler(Scheduler.Scheduling.PRIORITY, 1, 1);
		TaskExample2 task = new TaskExample2();
		TaskWaiter taskWaiter = scheduler.scheduleForExecution(task);
		
		scheduler.tryPause(task);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assert task.getState().equals(Task.State.INTERM_STATE_1) || task.getState().equals(Task.State.INTERM_STATE_3)
			|| task.getState().equals(Task.State.INTERM_STATE_5);
		
		scheduler.tryResume(task);
		taskWaiter.waitForCompletion();
		
		scheduler.shutdown();
	}
	
	@Test
	void taskShouldBeStopped()
	{
		Scheduler scheduler = new Scheduler(Scheduler.Scheduling.PRIORITY, 1, 1);
		TaskExample2 task = new TaskExample2();
		scheduler.scheduleForExecution(task);
		
		scheduler.tryStop(task);
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assert task.getState().equals(Task.State.STOP_STATE);
		
		scheduler.shutdown();
	}
	
	@Test
	void taskShouldBeStoppedOnReachingMaxAllowedTime()
	{
		Scheduler scheduler = new Scheduler(Scheduler.Scheduling.PRIORITY, 1, 1);
		TaskExample2 task = new TaskExample2(Task.Priority.MEDIUM, false, 200, null, null);
		scheduler.scheduleForExecution(task);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assert task.getState().equals(Task.State.STOP_STATE);
		
		scheduler.shutdown();
	}
	
	@Test
	void taskShouldBeStoppedOnReachingEndDate()
	{
		Scheduler scheduler = new Scheduler(Scheduler.Scheduling.PRIORITY, 1, 1);
		TaskExample2 task = new TaskExample2(Task.Priority.MEDIUM, false, 0, null, new Date(System.currentTimeMillis() + 200));
		scheduler.scheduleForExecution(task);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assert task.getState().equals(Task.State.STOP_STATE);
		
		scheduler.shutdown();
	}
}
