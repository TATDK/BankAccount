package dk.earthgame.TAT.BankAccount;

import org.bukkit.plugin.Plugin;

@SuppressWarnings("unused")
public class Loans {
	private BankAccount plugin;
	private double amount;
	private double rate;
	private float timeleft;
	private float run;
	private int part;
	private int parts;
	
	Loans(BankAccount plugin, double amount,double rate,float timeleft, int part) {
		this.plugin = plugin;
		this.amount = amount;
		this.rate = rate;
		this.timeleft = timeleft;
		run = timeleft/parts;
		
		if (run > 0) {
			createRun();
		}
	}

	void createRun() {
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)plugin, new Runnable() {
			public void run() {
				
			}
		}, ((long)run)*20*60, ((long)run)*20*60);
	}
}