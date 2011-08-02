package dk.earthgame.TAT.BankAccount.Features;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import dk.earthgame.TAT.BankAccount.BankAccount;
import dk.earthgame.TAT.BankAccount.Enum.FeeModes;
import dk.earthgame.TAT.BankAccount.Enum.TransactionTypes;
import dk.earthgame.TAT.BankAccount.System.Fee;
import dk.earthgame.nijikokun.register.payment.Method.MethodAccount;

public class PlayerFee extends Fee {
	private BankAccount plugin;

	public PlayerFee(FeeModes Mode, double Percentage, double Static, BankAccount instantiate) {
		super(Mode, Percentage, Static);
		plugin = instantiate;
	}
	
	public double PayFee(double balance,String playername) {
		return PayFee(balance,balance,playername);
	}
	
	public double PayFee(double amount,double balance,String playername) {
		Player player = null;
		if (plugin.getServer().getPlayer(playername) != null)
			player = plugin.getServer().getPlayer(playername);
		MethodAccount account = plugin.Method.getAccount(playername);
		if (amount == balance) {
			if (CanAfford(balance)) {
				double fee = CalculateFee(balance);
				account.subtract(fee);
				if (player != null)
					player.sendMessage("ATM: " + ChatColor.GREEN + "You paid " + plugin.Method.format(fee) + " in fee!");
				plugin.SQLWorker.addTransaction(playername, "", TransactionTypes.FEE_PLAYER, fee);
				return balance-fee;
			} else if (player != null)
				player.sendMessage("ATM: " + ChatColor.RED + "You don't have enough money to pay fee!");
		} else {
			if (CanAfford(amount,balance)) {
				double fee = CalculateFee(amount);
				if (CalculateFee(amount)+amount <= balance) {
					account.subtract(fee);
					if (player != null)
						player.sendMessage("ATM: " + ChatColor.GREEN + "You paid " + plugin.Method.format(fee) + " in fee!");
					plugin.SQLWorker.addTransaction(playername, "", TransactionTypes.FEE_PLAYER, fee);
					return amount;
				} else {
					account.subtract(fee);
					if (player != null)
						player.sendMessage("ATM: " + ChatColor.GREEN + "You paid " + plugin.Method.format(fee) + " in fee!");
					plugin.SQLWorker.addTransaction(playername, "", TransactionTypes.FEE_PLAYER, fee);
					return balance-fee;
				}
			} else if (player != null)
				player.sendMessage("ATM: " + ChatColor.RED + "You don't have enough money to pay fee!");
		}
		return 0;
	}
}