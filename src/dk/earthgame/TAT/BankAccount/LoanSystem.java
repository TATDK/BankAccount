package dk.earthgame.TAT.BankAccount;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.plugin.Plugin;

import com.nijiko.coelho.iConomy.system.Account;

import dk.earthgame.TAT.BankAccount.System.TransactionTypes;


public class LoanSystem {
	private BankAccount plugin;
	public boolean LoanActive;
	boolean running;
	HashMap<String,Loan> Loans = new HashMap<String,Loan>();
	double Fixed_rate;
    Map<Double, Double> Rates = new HashMap<Double, Double>();
	double Max_amount;
	int PaymentTime;
	int PaymentParts;
	int runTime = 1;
	private int JobId;
	
	public LoanSystem(BankAccount instantiate) {
		plugin = instantiate;
	}

	/**
	 * Start the automatic loan system
	 */
	public void startupRunner() {
		ResultSet rs;
		try {
			rs = plugin.stmt.executeQuery("SELECT `player`,`totalamount`,`remaining`,`part`,`parts`,`timeleft`,`timepayment` FROM `" + plugin.SQL_loan_table + "`");
			while (rs.next()) {
				String player = rs.getString("player");
				double totalamount = rs.getDouble("totalamount");
				double remaining = rs.getDouble("remaining");
				int part = rs.getInt("part");
				int parts = rs.getInt("parts");
				int timeleft = rs.getInt("timeleft");
				int timepayment = rs.getInt("timepayment");
				Loans.put(player, new Loan(plugin,this,player,totalamount,remaining,timeleft,timepayment,part,parts));
			}
			running = true;
		} catch (SQLException e) {
			if (!e.getMessage().equalsIgnoreCase(null))
				plugin.consoleWarning("Error #11-2: " + e.getMessage());
			else
				plugin.consoleWarning("Error #11-1: " + e.getErrorCode() + " - " + e.getSQLState());
		}
		
		if (JobId > 0) {
			((Plugin)plugin).getServer().getScheduler().cancelTask(JobId);
			JobId = 0;
		}
		JobId = ((Plugin)plugin).getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)plugin, new Runnable() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public void run() {
				plugin.consoleInfo("Loan start");
				Iterator it = Loans.entrySet().iterator();
				while (it.hasNext()) {
			        Map.Entry<String,Loan> pairs = (Map.Entry<String,Loan>)it.next();
			        pairs.getValue().runLoan();
			        if (pairs.getValue().remaining <= 0) {
			        	Loans.remove(pairs.getKey());
			        	if (plugin.getSaved(pairs.getKey()).bounty > 0.00) {
			        		plugin.addTransaction(pairs.getKey(), "", TransactionTypes.LOAN_MISSING, pairs.getValue().remaining);
			        	} else {
			        		plugin.consoleInfo(pairs.getKey() + " paid a part of the loan back");
			        		plugin.addTransaction(pairs.getKey(), "", TransactionTypes.LOAN_PAID, 0.00);
			        	}
			        }
			    }
				plugin.consoleInfo("Loan stop");
			}
		}, 0, runTime*20*60);
	}
	
	/**
	 * Shutdown the automatic loan runner
	 */
	public void shutdownRunner() {
		if (JobId > 0) {
			((Plugin)plugin).getServer().getScheduler().cancelTask(JobId);
		}
		JobId = 0;
		Loans.clear();
		running = false;
	}
	
	/**
	 * Check if the player have a loan
	 * 
	 * @param player - The username of the player
	 * @return boolean
	 */
	public boolean haveLoan(String player) {
		if (Loans.containsKey(player)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Get the loan of a player, returns null of none loan is found
	 * 
	 * @param player - The username of the player
	 * @return Loan
	 */
	public Loan getLoan(String player) {
		if (Loans.containsKey(player)) {
			return Loans.get(player);
		}
		return null;
	}
	
	/**
	 * Run a payment of a loan
	 * 
	 * @param player - The username of the player
	 * @param amount - Amount money
	 * @return double - The amount that are removed from the loan
	 */
	public double payment(String player,double amount) {
		if (haveLoan(player)) {
			Loan playerLoan = getLoan(player);
			Account iConomyAccount = com.nijiko.coelho.iConomy.iConomy.getBank().getAccount(player);
			if (playerLoan.remaining < amount) {
				amount = playerLoan.remaining;
			}
			if (iConomyAccount.hasEnough(amount)) {
				iConomyAccount.subtract(amount);
				playerLoan.manualPayment(amount);
				if (playerLoan.remaining <= 0.00) {
					Loans.remove(player);
				}
				return amount;
			}
		}
		return 0.00;
	}
	
	/**
	 * 
	 * @param player
	 * @param amount
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean addLoan(String player,double amount) {
		if (!running) {
			return false;
		}
		Account iConomyAccount = com.nijiko.coelho.iConomy.iConomy.getBank().getAccount(player);
		if (!iConomyAccount.isNegative() && amount <= Max_amount) {
			try {
				double rate = 0.00;
				if (Fixed_rate == 0.00) {
					Iterator it = Rates.entrySet().iterator();
					while (it.hasNext()) {
				        Map.Entry<Double,Double> pairs = (Map.Entry<Double,Double>)it.next();
				        if (pairs.getKey() < amount) {
				        	rate = pairs.getValue();
				        }
				    }
				} else {
					rate = Fixed_rate;
				}
				double totalamount = amount*(1+rate);
				plugin.stmt.executeUpdate("INSERT INTO `" + plugin.SQL_loan_table + "` (`player`,`totalamount`,`remaining`,`parts`,`part`,`timeleft`,`timepayment`) VALUES ('" + player + "','" + totalamount + "','" + totalamount + "','" + PaymentParts + "','0','" + (PaymentTime/PaymentParts) + "','" + PaymentTime + "')");
				Loans.put(player, new Loan(plugin, this, player,totalamount,totalamount,(PaymentTime/PaymentParts)*60,(PaymentTime/PaymentParts)*60, 0, PaymentParts));
				iConomyAccount.add(amount);
				return true;
			} catch(SQLException e) {
				if (!e.getMessage().equalsIgnoreCase(null))
					plugin.consoleWarning("Error #02-2: " + e.getMessage());
				else
					plugin.consoleWarning("Error #02-1: " + e.getErrorCode() + " - " + e.getSQLState());
			}
			return false;
		}
		return false;
	}
}