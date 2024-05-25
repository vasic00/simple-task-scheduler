Klase TaskExample i Application su primjer/implementacija za drugu lab. vjezbu.
Klase TaskExample2 i Application2 su primjer/implementacija za trecu lab. vjezbu.

Vidjecete da u klasi Scheduler postoje 3 metode za pauziranje, ponovno pokretanje i zaustavljanje zadatka. (tryPause, tryResume i tryInterrupt)
Pauziranje/zaustavljanje zadatka nece blokirati radnu/sopstenu nit koja izvrsava taj zadatak.

Sta se desava pri pauziranju:
	- posaljemo zahtjev rasporedjivacu da postavi boolean za indikaciju pauze na true
	- korisnik ako je implementirao zadatak tako da provjerava boolean za pauzu (isPaused), dozvolice zadatku da se pauzira
	- tada ce se zadatak ubaciti u poseban HashSet (HashSet sam uzeo zbog brzine rada HashSet.contains(Task) metode - koristi hash tabelu)
	- taj HashSet se u mom kodu naziva pausedTaskSet (privatno polje klase Scheduler)
	- kada neko pozove tryResume metodu, pauzirani zadatak ce se prebaciti iz HashSeta u red (iz njega radne niti uzimaju zadatke za izvrsavanje)
	- ovo ne znaci nuzno da ce se zadatak postaviti na pocetak reda (mozda i hoce, ako ima najveci prioritet i ako imamo prioritetno rasporedjivanje)

Sta se desava pri zaustavljanju (interrupt):
	- mozemo zaustaviti na dva nacina, da silom terminiramo zadatak (da zadatak zavrsi sa radom, bez obzira na njegovo stanje) ili samo da trenutno prekine rad
	- ako smo odlucili da samo trenutno prekine rad, zadatak ce se automatski ponovo ubaciti u red (jer nije zavrsio rad - status isCompleted nije true)

Ovo sve zavisi od toga da li ce korisnik koji implementira zadatak uopste dozvoliti zaustavljanje i pauziranje zadatka.

Za izradu je koristen JDK 17 i Eclipse IDE.

