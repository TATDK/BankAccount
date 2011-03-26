package dk.earthgame.TAT.BankAccount;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;

public class Loan {
	private BankAccount plugin;
	private LoanSystem system;
	private String player;
	private Account account;
	private double totalamount; //Amount + rates
	private int timeleft; //Time to next payment
	private int timepayment; //Time between every payment
	private int part; //Paid times
	private int parts; //Number of times until deadline
	
	Loan(BankAccount plugin, LoanSystem system, String player,double totalamount,int timeleft,int timepayment,int part,int parts) {
		this.plugin = plugin;
		this.system = system;
		this.player = player;
		this.totalamount = totalamount;
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
		
		try {
			plugin.stmt.executeUpdate("UPDATE `" + plugin.SQL_loan_table + "` SET `part` = '" + part + "' WHERE `player` = '" + player + "'");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if (part >= parts) {
			try {
				plugin.stmt.executeUpdate("DELETE FROM `" + plugin.SQL_loan_table + "` WHERE `player` = '" + player + "'");
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if (account.hasOver(totalamount/parts)) {
				account.subtract(totalamount/parts);
			} else {
				addBounty();
			}
		} else {
			account.subtract(totalamount/parts);
		}
	}
	
	void addBounty() {
		FileReader fr;
		try {
			fr = new FileReader("server.properties");
			BufferedReader br = new BufferedReader(fr);
			String s;
			while((s=br.readLine()) .indexOf("pvp")==-1);
			if (s.split("=")[1].equalsIgnoreCase("true")) {
				plugin.getSaved(player).bounty = totalamount/parts;
			} else {
				account.subtract(totalamount/parts);
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
