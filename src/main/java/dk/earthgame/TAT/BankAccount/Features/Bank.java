package dk.earthgame.TAT.BankAccount.Features;

import dk.earthgame.TAT.BankAccount.BankAccount;

public class Bank {
	private BankAccount plugin;
	private String name;
	private int id;
	
	public Bank(BankAccount instantiate, String bankname) {
		plugin = instantiate;
		this.name = bankname;
	}
	
	public String getname() {
		return name;
	}
	
	public int getid() {
		return id;
	}
}