package dk.earthgame.TAT.BankAccount;

public class Loans {
	private double amount;
	private double rate;
	private float timeleft;
	private float run;
	
	Loans(double amount,double rate,float timeleft, float run) {
		this.amount = amount;
		this.rate = rate;
		this.timeleft = timeleft;
		this.run = run;
		
		if (run > 0) {
			createRun();
		}
	}

	void createRun() {
		
	}
}