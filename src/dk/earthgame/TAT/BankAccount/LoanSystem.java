package dk.earthgame.TAT.BankAccount;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.nijiko.coelho.iConomy.system.Account;

public class LoanSystem {
	public boolean LoanActive;
	HashMap<String,Loan> Loans = new HashMap<String,Loan>();
	double Fixed_rate;
    Map<Double, Double> Rates = new HashMap<Double, Double>();
	double Max_amount;
	int PaymentTime;
	int PaymentParts;
	private int JobId;
	private BankAccount plugin;
	
	public LoanSystem(BankAccount instantiate) {
		plugin = instantiate;
	}

	public void startupRunner() {
		if (JobId > 0) {
			((Plugin)plugin).getServer().getScheduler().cancelTask(JobId);
			JobId = 0;
		}
		JobId = ((Plugin)plugin).getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)plugin, new Runnable() {
			public void run() {
				ResultSet rs;
				try {
					rs = plugin.stmt.executeQuery("SELECT `player`,`totalamount`,`part`,`parts`,`timeleft` FROM `" + plugin.SQL_loan_table + "`");
					while (rs.next()) {
						String player = rs.getString("player");
						double amount = rs.getDouble("totalamount");
						int part = rs.getInt("part");
						int parts = rs.getInt("parts");
						int timeleft = rs.getInt("timeleft");
						Loans.put(player, new Loan(player, amount, part, parts, timeleft));
					}
				} catch (SQLException e) {
					if (!e.getMessage().equalsIgnoreCase(null))
						plugin.consoleWarning("Error #11-2: " + e.getMessage());
					else
						plugin.consoleWarning("Error #11-1: " + e.getErrorCode() + " - " + e.getSQLState());
				}
			}
		}, 20*60, 0);
	}
	
	public void shutdownRunner() {
		if (JobId > 0) {
			((Plugin)plugin).getServer().getScheduler().cancelTask(JobId);
		}
		JobId = 0;
	}
	
	public boolean haveLoan(Player player) {
		if (Loans.containsKey(player.getName())) {
			return true;
		}
		return false;
	}
	
	Loan getLoan(Player player) {
		if (Loans.containsKey(player.getName())) {
			return Loans.get(player.getName());
		}
		return null;
	}
	
	public boolean addLoan(String player,Double amount) {
		Account iConomyAccount = com.nijiko.coelho.iConomy.iConomy.getBank().getAccount(player);
		if (!iConomyAccount.isNegative() && amount <= Max_amount) {
			try {
				Double rate = 0.00;
				if (Fixed_rate == 0.00) {
					for (Double RateAmount : Rates.keySet()) {
						if (RateAmount <= amount) {
							rate = Rates.get(RateAmount);
						}
					}
				} else {
					rate = Fixed_rate;
				}
				amount *= (1+rate);
				plugin.stmt.executeUpdate("INSERT INTO `" + plugin.SQL_loan_table + "` (`player`,`amount`) VALUES ('" + player + "','" + amount + "')");
				iConomyAccount.add(amount);
				Loans.put(player, new Loan(player,amount,(PaymentTime/PaymentParts)*60, 0, PaymentParts));
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