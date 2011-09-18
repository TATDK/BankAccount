package dk.earthgame.TAT.BankAccount.Listeners;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

import dk.earthgame.TAT.BankAccount.BankAccount;
import dk.earthgame.TAT.BankAccount.Features.Area;
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
    public void onPlayerMove(PlayerMoveEvent event) {
    	Location from = event.getFrom();
    	Location to = event.getTo();
    	int enter = plugin.BankAreas.entered(from, to);
    	int left = plugin.BankAreas.left(from, to);
    	if (enter > 0) {
    		List<Integer> entered = plugin.BankAreas.enteringAreas(from, to);
    		for (int e : entered) {
	    		Area area = plugin.BankAreas.get(e);
	    		UserSave usersave = plugin.UserSaves.getSaved(event.getPlayer().getName());
	    		if (!usersave.inArea(area)) {
	    			event.getPlayer().sendMessage("BankAccount: You entered " + area.getName());
	    			usersave.enterArea(area);
	    		}
    		}
    	}
    	if (left > 0) {
    		List<Integer> leaving = plugin.BankAreas.leavingAreas(from, to);
    		for (int l : leaving) {
	    		Area area = plugin.BankAreas.get(l);
	    		UserSave usersave = plugin.UserSaves.getSaved(event.getPlayer().getName());
	    		if (usersave.inArea(area)) {
	    			event.getPlayer().sendMessage("BankAccount: You left " + area.getName());
	    			usersave.exitArea(area);
	    		}
    		}
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