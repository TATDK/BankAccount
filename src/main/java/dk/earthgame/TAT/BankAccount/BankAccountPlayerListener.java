package dk.earthgame.TAT.BankAccount;

import org.bukkit.Location;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

import dk.earthgame.TAT.BankAccount.System.UserSaves;

public class BankAccountPlayerListener extends PlayerListener {
    private BankAccount plugin;
    public BankAccountPlayerListener(BankAccount instantiate) {
        plugin = instantiate;
    }
    
    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            UserSaves mySave = plugin.getSaved(event.getPlayer().getName());
            if (event.getPlayer().getItemInHand().getTypeId() == plugin.settings.AreaWandId && mySave.isSelecting()) {
                Location pos = event.getClickedBlock().getLocation();
                if (mySave.setPosition(pos) == 2) {
                    event.getPlayer().sendMessage("ATM: Area selected, to confirm: /account setarea <areaname>");
                } else {
                    event.getPlayer().sendMessage("ATM: Position 1 selected, please select position 2");
                }
            }
        }
    }
}
