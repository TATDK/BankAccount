package dk.earthgame.TAT.BankAccount.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.SignChangeEvent;

import dk.earthgame.TAT.BankAccount.BankAccount;
import dk.earthgame.TAT.BankAccount.Enum.FeeModes;
import dk.earthgame.TAT.BankAccount.Enum.PermissionNodes;
import dk.earthgame.nijikokun.register.payment.Method.MethodAccount;

public class BankAccountBlockListener extends BlockListener {
    private BankAccount plugin;
    
    public BankAccountBlockListener(BankAccount instantiate) {
        plugin = instantiate;
    }
    
    @Override
    public void onBlockBreak(BlockBreakEvent event) {
    	if (event.getBlock().getState() instanceof Sign) {
    		World w = event.getBlock().getWorld();
    		Location l = event.getBlock().getLocation();
    		Player p = event.getPlayer();
    		if (plugin.ATMSign.exists(w, l)) {
    			if (plugin.playerPermission(p, PermissionNodes.ATMSIGN)) {
	    			plugin.ATMSign.remove(w, l);
	    			p.sendMessage("[BankAccount] " + ChatColor.GREEN + "ATMSign removed");
    			} else {
	    			p.sendMessage("[BankAccount] " + ChatColor.RED + "You don't have permission to remove ATM signs!");
        			event.setCancelled(true);
    			}
    		} 
    		if (plugin.BalanceSign.exists(w, l)) {
    			if (plugin.accessAccount(plugin.BalanceSign.getAccount(w, l), p, false) && plugin.playerPermission(p, PermissionNodes.BALANCESIGN)) {
    				plugin.BalanceSign.remove(w, l);
        			p.sendMessage("[BankAccount] " + ChatColor.GREEN + "Balancesign removed");
    			} else {
	    			p.sendMessage("[BankAccount] " + ChatColor.RED + "You don't have permission to remove balancesigns!");
        			event.setCancelled(true);
    			}
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
            if (event.getLine(1) != null) {
            	if (event.getLine(1).equalsIgnoreCase("atm")) {
            		if (plugin.playerPermission(p, PermissionNodes.ATMSIGN)) {
                		if (plugin.ATMSign.enabled) {
					        MethodAccount economyAccount = plugin.Method.getAccount(event.getPlayer().getName());
					        if (plugin.settings.SignFee.getMode() != FeeModes.NONE) {
					            double balance = economyAccount.balance();
					            if (plugin.settings.SignFee.PayFee(balance, event.getPlayer().getName()) == 0)
					                return;
					        }
					        p.sendMessage("ATMsign created");
			                plugin.ATMSign.add(p.getWorld(), event.getBlock().getLocation());
					    } else {
                			SignError(event,p,"ATMsign not enabled");
                		}
            		} else {
            			SignError(event,p,"You don't have access to create ATM signs");
            		}
            	} else if (event.getLine(1).equalsIgnoreCase("balance")) {
            		if (plugin.BalanceSign.enabled) {
                		if (plugin.playerPermission(p, PermissionNodes.BALANCESIGN)) {
							if (plugin.accountExists(event.getLine(2))) {
							    if (plugin.accessAccount(event.getLine(2), p, false)) {
							        MethodAccount economyAccount = plugin.Method.getAccount(event.getPlayer().getName());
							        if (plugin.settings.SignFee.getMode() != FeeModes.NONE) {
							            double balance = economyAccount.balance();
							            if (plugin.settings.DepositFee.CanAfford(balance)) {
							                if (!economyAccount.subtract(plugin.settings.DepositFee.CalculateFee(balance))) {
							                    SignError(event,p,"Couldn't subtract sign creating fee from your account");
							                } else {
							                    p.sendMessage("Balancesign created");
							                    plugin.BalanceSign.add(p.getWorld(), event.getBlock().getLocation(), event.getLine(1));
							                }
							            } else {
							                SignError(event,p,"You don't have enough money.");
							            }
							        } else {
							            p.sendMessage(ChatColor.GREEN + "Balancesign created");
							            plugin.BalanceSign.add(p.getWorld(), event.getBlock().getLocation(), event.getLine(1));
							        }
							    } else {
							        SignError(event,p,"You don't have access to this account");
							    }
							} else {
							    SignError(event,p,"Account doens't exists");
							}
                		} else {
                			SignError(event,p,"You don't have access to create balancesigns");
                		}
            		} else {
            			SignError(event,p,"Balancesigns not enabled");
            		}
            	}
            } else {
                SignError(event,p,"Please type an accountname");
            }
        }
    }
    
    private void SignError(SignChangeEvent event, Player p, String message) {
        event.getBlock().setType(Material.AIR);
        p.getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.SIGN, 1));
        p.sendMessage("[BankAccount] " + ChatColor.RED + message);
    }
}
