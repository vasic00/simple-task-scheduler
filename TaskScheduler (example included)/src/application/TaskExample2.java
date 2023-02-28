package application;

import java.util.Date;

/**
 * Priprema za trecu lab vjezbu:
 * Primjer implementacije zadatka gdje se moze vidjeti zaustavljanje, pauziranje i nastavak rada zadatka.
 * Pokazan je i postupak prelaska iz stanja u stanje, ali i vracanje na odgovarajuce mjesto (stanje) izvrsavanja ako je zadatak prethodno bio zaustavljen.
 * 
 * @author Mihailo Vasić
 */
public class TaskExample2 extends TaskExample {
	
	private static final int SLEEP_TIME = 200;

	public TaskExample2()
	{
		super();
	}
	
	public TaskExample2(Priority priority, boolean isSelfThread)
	{
		super(priority, isSelfThread);
	}
	
	public TaskExample2(Priority priority, boolean isSelfThread, long allowedTime, Date startDate, Date endDate)
	{
		super(priority, isSelfThread, allowedTime, startDate, endDate);
	}
	
	@Override
	public void run()
	{
		// prikazana je linearna tranzicija po rednim brojevima stanja (cisto radi jednostavnosti)
		// u nekim stanjima se provjerava da li je zadatak pauziran, a u nekima ne
		// u svakom stanju se provjerava da li je zadatak zaustavljen (interrupted)
		// ako je zadatak interrupted, on ce se ponovo postaviti u red, pod uslovom da terminate status nije true
		switch(getState()) {
		case START_STATE:
			System.out.println("Zadatak " + ID + " se nalazi u stanju " + State.START_STATE);
			System.out.println("Zadatak " + ID + " radi na tranziciji u stanje " + State.INTERM_STATE_1);
			if (sleep())
				return;
			transitionInNaturalOrder();
		case INTERM_STATE_1:
			System.out.println("Zadatak " + ID + " se nalazi u stanju " + State.INTERM_STATE_1);
			System.out.println("Zadatak " + ID + " radi na tranziciji u stanje " + State.INTERM_STATE_2);
			if (sleep())
				return;
			waitIfPaused();
			transitionInNaturalOrder();
		case INTERM_STATE_2:
			System.out.println("Zadatak " + ID + " se nalazi u stanju " + State.INTERM_STATE_2);
			System.out.println("Zadatak " + ID + " radi na tranziciji u stanje " + State.INTERM_STATE_3);
			if (sleep())
				return;
			transitionInNaturalOrder();
		case INTERM_STATE_3:
			System.out.println("Zadatak " + ID + " se nalazi u stanju " + State.INTERM_STATE_3);
			System.out.println("Zadatak " + ID + " radi na tranziciji u stanje " + State.INTERM_STATE_4);
			if (sleep())
				return;
			waitIfPaused();
			transitionInNaturalOrder();
		case INTERM_STATE_4:
			System.out.println("Zadatak " + ID + " se nalazi u stanju " + State.INTERM_STATE_4);
			System.out.println("Zadatak " + ID + " radi na tranziciji u stanje " + State.INTERM_STATE_5);
			if (sleep())
				return;
			transitionInNaturalOrder();
		case INTERM_STATE_5:
			System.out.println("Zadatak " + ID + " se nalazi u stanju " + State.INTERM_STATE_5);
			System.out.println("Zadatak " + ID + " radi na tranziciji u stanje " + State.FINAL_STATE);
			if (sleep())
				return;
			waitIfPaused();
			transitionInNaturalOrder();
		case FINAL_STATE:
			System.out.println("Zadatak " + ID + " se nalazi u stanju " + State.FINAL_STATE);
			System.out.println("Zadatak " + ID + " je u finalnom stanju");
		default:
			break;
		}
	}
	
	private boolean sleep()
	{
		try
		{
			Thread.sleep(SLEEP_TIME);
		}
		catch (InterruptedException e)
		{}
		if (getIsStopped())
			return true;
		return false;
	}
}
