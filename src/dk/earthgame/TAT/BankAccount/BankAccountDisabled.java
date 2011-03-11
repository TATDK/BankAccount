package dk.earthgame.TAT.BankAccount;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BankAccountDisabled implements CommandExecutor {
	public BankAccountDisabled(BankAccount instance) {
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		return false;
    }
}
