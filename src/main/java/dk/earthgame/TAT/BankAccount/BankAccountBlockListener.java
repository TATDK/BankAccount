package dk.earthgame.TAT.BankAccount;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.SignChangeEvent;

import dk.earthgame.TAT.BankAccount.System.PermissionNodes;

public class BankAccountBlockListener extends BlockListener {
	private BankAccount plugin;
	
	public BankAccountBlockListener(BankAccount instantiate) {
		plugin = instantiate;
	}

	@Override
	public void onSignChange(SignChangeEvent event) {
		Player p = event.getPlayer();
		if (!(p instanceof Player)) {
			return;
		}
		
		if (event.getLine(0).equalsIgnoreCase("[BankAccount]")) {
			if (plugin.playerPermission(p, PermissionNodes.SIGN)) {
				if (event.getLine(1) != null) {
					if (plugin.accountExists(event.getLine(1))) {
						if (plugin.accessAccount(event.getLine(1), p, false)) {
							plugin.addSign(p.getWorld(), event.getBlock().getLocation(), event.getLine(1));
						} else {
							SignError(event,p,"[BankAccount] You don't have access to this account");
						}
					} else {
						SignError(event,p,"[BankAccount] Account doens't exists");
					}
				} else {
					SignError(event,p,"[BankAccount] Please type an accountname");
				}
			} else {
				SignError(event,p,"[BankAccount] You don't have access to create BankAccount signs");
			}
		}
	}
	
	private void SignError(SignChangeEvent event, Player p, String message) {
		event.getBlock().setType(Material.AIR);
		p.getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.SIGN, 1));
		p.sendMessage(message);
	}
}
