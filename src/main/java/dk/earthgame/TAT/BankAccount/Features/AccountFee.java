package dk.earthgame.TAT.BankAccount.Features;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import dk.earthgame.TAT.BankAccount.BankAccount;
import dk.earthgame.TAT.BankAccount.Enum.FeeModes;
import dk.earthgame.TAT.BankAccount.Enum.TransactionTypes;
import dk.earthgame.TAT.BankAccount.System.Fee;

public class AccountFee extends Fee {
	private BankAccount plugin;

	public AccountFee(FeeModes Mode, double Percentage, double Static, BankAccount instantiate) {
		super(Mode, Percentage, Static);
		plugin = instantiate;
	}
	
	public double PayFee(double balance,String accountname) {
		return PayFee(balance,balance,accountname);
	}
	
	public double PayFee(double amount,double balance,String accountname) {
		return PayFee(balance,balance,accountname,"");
	}
	
	public double PayFee(double amount,double balance,String accountname,String playername) {
		Player player = null;
		if (playername.equalsIgnoreCase("") && plugin.getServer().getPlayer(playername) != null)
			player = plugin.getServer().getPlayer(playername);
		Account account = plugin.getAccount(accountname);
		if (amount == balance) {
			if (CanAfford(balance)) {
				double fee = CalculateFee(balance);
				account.subtract(fee);
				if (player != null)
					player.sendMessage("ATM: " + ChatColor.GREEN + "The bank withdrawed " + plugin.Method.format(fee) + " in fee!");
				plugin.SQLWorker.addTransaction(playername, accountname, TransactionTypes.FEE_ACCOUNT, fee);
				return balance-fee;
			} else if (player != null)
				player.sendMessage("ATM: " + ChatColor.RED + "The account don't have enough money to pay fee!");
		} else {
			if (CanAfford(amount,balance)) {
				double fee = CalculateFee(amount);
				if (CalculateFee(amount)+amount <= balance) {
					account.subtract(fee);
					if (player != null)
						player.sendMessage("ATM: " + ChatColor.GREEN + "The bank withdrawed " + plugin.Method.format(fee) + " in fee!");
					plugin.SQLWorker.addTransaction(playername, accountname, TransactionTypes.FEE_ACCOUNT, fee);
					return amount;
				} else {
					account.subtract(fee);
					if (player != null)
						player.sendMessage("ATM: " + ChatColor.GREEN + "The bank withdrawed " + plugin.Method.format(fee) + " in fee!");
					plugin.SQLWorker.addTransaction(playername, accountname, TransactionTypes.FEE_ACCOUNT, fee);
					return balance-fee;
				}
			} else if (player != null)
				player.sendMessage("ATM: " + ChatColor.RED + "The account don't have enough money to pay fee!");
		}
		return 0;
	}
}