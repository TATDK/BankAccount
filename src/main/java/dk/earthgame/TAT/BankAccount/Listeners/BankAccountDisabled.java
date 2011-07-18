package dk.earthgame.TAT.BankAccount.Listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * BankAccount executor for commands when disabled
 * Used to make sure, that no commands works when plugin is disabled
 * @author TAT
 */
public class BankAccountDisabled implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) { return false; }
}
