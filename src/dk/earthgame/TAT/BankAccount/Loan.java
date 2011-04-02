package dk.earthgame.TAT.BankAccount;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;

import dk.earthgame.TAT.BankAccount.System.TransactionTypes;


public class Loan {
	private BankAccount plugin;
	private LoanSystem system;
	private String player;
	private Account account;
	public double totalamount; //Amount + rates
	public double remaining; //Amount that needs to be paid
	private int timeleft; //Time to next payment
	private int timepayment; //Time between every payment
	private int part; //Paid times
	private int parts; //Number of times until deadline
	
	Loan(BankAccount plugin, LoanSystem system, String player,double totalamount,double remaining,int timeleft,int timepayment,int part,int parts) {
		this.plugin = plugin;
		this.system = system;
		this.player = player;
		this.totalamount = totalamount;
		this.remaining = remaining;
		this.timeleft = timeleft;
		this.timepayment = timepayment;
		this.part = part;
		this.parts = parts;
		
		this.account = iConomy.getBank().getAccount(player);
	}
	
	void runLoan() {
		timeleft -= (system.runTime*60);
		if (timeleft <= 0) {
			runPayment();
		}
		try {
			plugin.stmt.executeUpdate("UPDATE `" + plugin.SQL_loan_table + "` SET `timeleft` = '" + timeleft + "' WHERE `player` = '" + player + "'");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	void runPayment() {
		part++;
		timeleft += timepayment;
		double subtract = 0.00;
		
		if (remaining <= (totalamount/parts)) {
			subtract = remaining;
		} else {
			subtract = (totalamount/parts);
		}
		
		try {
			plugin.stmt.executeUpdate("UPDATE `" + plugin.SQL_loan_table + "` SET `part` = '" + part + "' WHERE `player` = '" + player + "'");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//Check if player is online
		boolean online = false;
		Player messageTo = null;
		if (plugin.getServer().getPlayer(player) != null) {
			messageTo = plugin.getServer().getPlayer(player);
			online = true;
		}
		
		if (part >= parts) {
			try {
				plugin.stmt.executeUpdate("DELETE FROM `" + plugin.SQL_loan_table + "` WHERE `player` = '" + player + "'");
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if (account.hasOver(subtract)) {
				account.subtract(subtract);
			} else {
				addBounty(subtract);
			}
		} else {
			account.subtract(subtract);
		}
		
		remaining -= subtract;
		
		if (online) {
			messageTo.sendMessage("ATM: " + ChatColor.GREEN + iConomy.getBank().format(subtract) + " paid off your loan.");
			if (remaining <= 0) {
				messageTo.sendMessage("ATM: " + ChatColor.GREEN + "Your loan is fully paid off.");
			}
		}
		plugin.addTransaction(player, null, TransactionTypes.LOAN_PAYMENT, subtract);
	}
	
	void manualPayment(double amount) {
		remaining -= amount;
	}
	
	void addBounty(double amount) {
		FileReader fr;
		try {
			fr = new FileReader("server.properties");
			BufferedReader br = new BufferedReader(fr);
			String s;
			while((s=br.readLine()) .indexOf("pvp")==-1);
			if (s.split("=")[1].equalsIgnoreCase("true")) {
				plugin.getSaved(player).bounty = amount;
			} else {
				account.subtract(amount);
			}
			fr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	int partsLeft() {
		return parts-part;
	}
}
