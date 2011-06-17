package org.martin.bukkit.npclib;

import java.util.logging.Level;
import java.util.logging.Logger;
//import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.InventoryLargeChest;
//import net.minecraft.server.PlayerManager;
import net.minecraft.server.TileEntityChest;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author martin
 */
public class BPlayer {
	private CraftPlayer cPlayer;
	private EntityPlayer ePlayer;
	
	public BPlayer(Player player) {
		try {
			cPlayer = (CraftPlayer) player;
			ePlayer = cPlayer.getHandle();
		} catch (Exception ex) {
			Logger.getLogger("Minecraft").log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Open small virtual chest
	 * 
	 * @param chest - The chest you want to open
	 */
	public void openVirtualChest(TileEntityChest chest) {
		ePlayer.a(chest);
	}

	/**
	 * Open large virtual chest
	 * 
	 * @param lChest - The chest you want to open
	 */
	public void openVirtualChest(InventoryLargeChest lChest) {
		ePlayer.a(lChest);
	}
}