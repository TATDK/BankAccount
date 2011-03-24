package dk.earthgame.TAT.BankAccount;

public class Loan {
	private String player;
	private double totalamount; //Amount + rates
	private int timeleft; //Time to next part
	private int part; //Paid times
	private int parts; //Number of times until deadline
	
	Loan(String player,double totalamount,int timeleft,int part,int parts) {
		this.player = player;
		this.totalamount = totalamount;
		this.timeleft = timeleft;
		this.part = part;
		this.parts = parts;
	}
}
