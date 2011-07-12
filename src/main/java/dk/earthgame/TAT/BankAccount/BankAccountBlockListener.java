package dk.earthgame.TAT.BankAccount;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.SignChangeEvent;

import dk.earthgame.TAT.BankAccount.Settings.FeeModes;
import dk.earthgame.TAT.BankAccount.System.BankAccountException;
import dk.earthgame.TAT.BankAccount.System.PermissionNodes;
import dk.earthgame.nijikokun.register.payment.Method.MethodAccount;

public class BankAccountBlockListener extends BlockListener {
    private BankAccount plugin;
    
    public BankAccountBlockListener(BankAccount instantiate) {
        plugin = instantiate;
    }
    
    @Override
    public void onBlockBreak(BlockBreakEvent event) {
    	if (event.getBlock().getState() instanceof Sign) {
    		if (plugin.signExists(event.getBlock().getWorld(), event.getBlock().getLocation())) {
    			plugin.removeSign(event.getBlock().getWorld(), event.getBlock().getLocation());
    			event.getPlayer().sendMessage("[BankAccount] Sign removed");
    		} 
    	}
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
                    try {
						if (plugin.accountExists(event.getLine(1))) {
						    if (plugin.accessAccount(event.getLine(1), p, false)) {
						        MethodAccount economyAccount = plugin.Method.getAccount(event.getPlayer().getName());
						        if (plugin.settings.SignFee.getMode() != FeeModes.NONE) {
						            double balance = economyAccount.balance();
						            if (plugin.settings.DepositFee.CanAfford(balance)) {
						                if (!economyAccount.subtract(plugin.settings.DepositFee.Fee(balance))) {
						                    SignError(event,p,"[BankAccount] Couldn't subtract sign creating fee from your account");
						                } else {
						                    p.sendMessage("[BankAccount] Sign created");
						                    plugin.addSign(p.getWorld(), event.getBlock().getLocation(), event.getLine(1));
						                }
						            } else {
						                SignError(event,p,"[BankAccount] You don't have enough money.");
						            }
						        } else {
						            p.sendMessage("[BankAccount] Sign created");
						            plugin.addSign(p.getWorld(), event.getBlock().getLocation(), event.getLine(1));
						        }
						    } else {
						        SignError(event,p,"[BankAccount] You don't have access to this account");
						    }
						} else {
						    SignError(event,p,"[BankAccount] Account doens't exists");
						}
                    } catch (BankAccountException e) {
                        p.sendMessage(e.getMessage());
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
