package dk.earthgame.TAT.BankAccount.Listeners;

import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

import dk.earthgame.TAT.BankAccount.BankAccount;
import dk.earthgame.TAT.BankAccount.System.BALocation;
import dk.earthgame.TAT.BankAccount.System.UserSave;

public class BankAccountPlayerListener extends PlayerListener {
    private BankAccount plugin;
    
    public BankAccountPlayerListener(BankAccount instantiate) {
        plugin = instantiate;
    }
    
    @Override
    public void onPlayerChat(PlayerChatEvent event) {
    	if (plugin.UserSaves.getSaved(event.getPlayer().getName()).usingATM != null) {
    		plugin.UserSaves.getSaved(event.getPlayer().getName()).usingATM.GetChatMsg(event.getMessage());
    		event.setCancelled(true);
    	}
    }
    
    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            UserSave mySave = plugin.UserSaves.getSaved(event.getPlayer().getName());
            if (event.getPlayer().getItemInHand().getTypeId() == plugin.settings.areaWandId && mySave.isSelecting()) {
                Location pos = event.getClickedBlock().getLocation();
                if (mySave.setPosition(pos) == 2)
                    event.getPlayer().sendMessage("ATM: Area selected, to confirm: /account setarea <areaname>");
                else
                    event.getPlayer().sendMessage("ATM: Position 1 selected, please select position 2");
            }
            if (event.getClickedBlock().getState() instanceof Sign)
            	if (plugin.ATMSign.exists(new BALocation(event.getClickedBlock().getLocation())))
            		plugin.ATMSign.get(new BALocation(event.getClickedBlock().getLocation())).Scroll(event.getPlayer());
        }
        if (event.getAction() == Action.LEFT_CLICK_BLOCK)
            if (event.getClickedBlock().getState() instanceof Sign)
            	if (plugin.ATMSign.exists(new BALocation(event.getClickedBlock().getLocation())))
            		plugin.ATMSign.get(new BALocation(event.getClickedBlock().getLocation())).Select(event.getPlayer());
    }
}