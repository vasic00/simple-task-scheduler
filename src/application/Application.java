package application;

import simpletaskscheduler.*;

/**
 * Klasa koja sadrzi samo main metodu, radi testiranja pripreme za drugu laboratorijsku vjezbu.
 * 
 * @author Mihailo Vasić
 */
public class Application {

	public static void main(String[] args)
	{
		// zadaci se uzimaju iz reda po prioritetu, ne po FIFO (svejedno je, lako se podesi i jedno i drugo)
		// primjer BROJ 1: maksimalan broj radnih niti 2, a zadataka 4
		// sva podesavanja za TaskExample su nasumnicna (koristi se default konstruktor, imamo i parametrizovani konstruktor - tu mozemo podesiti kako hocemo)
		Scheduler scheduler = new Scheduler(Scheduler.Scheduling.PRIORITY, 2, 4);
		Task task1 = new TaskExample();
		Task task2 = new TaskExample();
		Task task3 = new TaskExample();
		Task task4 = new TaskExample();
		Task task5 = new TaskExample();
		Task task6 = new TaskExample();
		Task task7 = new TaskExample();
		Task task8 = new TaskExample();
		
		TaskWaiter taskWaiter1 = scheduler.scheduleForExecution(task1);
		scheduler.scheduleForExecution(task2);
		scheduler.scheduleForExecution(task3);
		taskWaiter1.waitForCompletion();
		// iako je maksimalan broj zadataka 4, iduci zadaci se nece rasporediti jer nam je main thread blokiran cekajuci na zavrsetak zadatka 1
		scheduler.scheduleForExecution(task4);
		TaskWaiter taskWaiter5 = scheduler.scheduleForExecution(task5);
		scheduler.scheduleForExecution(task6);
		// ovdje cekamo da se zavrsi zadatak 5
		taskWaiter5.waitForCompletion();
		scheduler.scheduleForExecution(task7);
		// posto je zadatak 1 vec zavrsen, ovdje main thread nece blokirati
		taskWaiter1.waitForCompletion();
		scheduler.scheduleForExecution(task8);
		
		// moze se testirati jos par slucajeva tako sto zadamo drugacije argumente Scheduler konstruktoru, npr:
		// primjer broj 2: maksimalan broj radnih niti je 1, a zadataka 4 (nema puno smisla, ovo je edge case)
		// primjer broj 3: maksimalan broj radnih niti je 5, a zadataka 1 (opet testiramo edge case)
		// etc.
		
		// potrebno je rucno zaustaviti rad programa, jer radne niti stoje u statusu cekanja na zadatke
		
	}
}
