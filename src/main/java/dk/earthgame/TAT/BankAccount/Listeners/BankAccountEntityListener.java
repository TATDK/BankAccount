package dk.earthgame.TAT.BankAccount.Listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageByProjectileEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

import dk.earthgame.TAT.BankAccount.BankAccount;
import dk.earthgame.nijikokun.register.payment.Method.MethodAccount;

public class BankAccountEntityListener extends EntityListener {
    private BankAccount plugin;
    public BankAccountEntityListener(BankAccount instantiate) {
        plugin = instantiate;
    }
    
    private void check(EntityDamageByEntityEvent event) {
        Entity attacker = event.getDamager();
        Entity defender = event.getEntity();
        
        if (defender instanceof Player) {
            Player player = (Player)defender;
            if (player.getHealth() - event.getDamage() <= 0) {
                if (attacker != null && attacker instanceof Player) {
                    double bounty = plugin.UserSaves.getSaved(player.getName()).getBounty();
                    if (bounty > 0.00) {
                        MethodAccount attackerAccount = plugin.Method.getAccount(((Player)attacker).getName());
                        attackerAccount.add(bounty);
                        plugin.UserSaves.getSaved(player.getName()).setBounty(0.00);
                    }
                }
            }
        }
    }
    
    private void check(EntityDamageByProjectileEvent event) {
        Entity attacker = event.getDamager();
        Entity defender = event.getEntity();
        
        if (defender instanceof Player) {
            Player player = (Player)defender;
            if (player.getHealth() - event.getDamage() <= 0) {
                if (attacker != null && attacker instanceof Player) {
                    double bounty = plugin.UserSaves.getSaved(player.getName()).getBounty();
                    if (bounty > 0.00) {
                        MethodAccount attackerAccount = plugin.Method.getAccount(((Player)attacker).getName());
                        attackerAccount.add(bounty);
                        plugin.UserSaves.getSaved(player.getName()).setBounty(0.00);
                    }
                }
            }
        }
    }
    
    @Override
    public void onEntityDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByProjectileEvent) {
            check((EntityDamageByProjectileEvent) event);
            return;
        } else if (event instanceof EntityDamageByEntityEvent) {
            check((EntityDamageByEntityEvent) event);
            return;
        }
    }
}
