package org.martin.bukkit.npclib;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import net.minecraft.server.Entity;
import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.WorldServer;

import org.bukkit.Location;
import dk.earthgame.TAT.BankAccount.BankAccount;

/**
 *
 * @author martin
 */
public class NPCManager {

	private HashMap<String, NPCEntity> npcs = new HashMap<String, NPCEntity>();
	private BServer server;
	private BankAccount plugin;

	public NPCManager(BankAccount plugin) {
		this.plugin = plugin;
		server = BServer.getInstance(plugin);
	}

	/**
	 * Spawn NPC
	 * 
	 * @param NPCname - Name of new NPC
	 * @param l - Location to spawn
	 * @return NPCEntity of the new NPC
	 */
	public NPCEntity spawnNPC(String NPCname, Location l) {
		int i = 0;
		String id = NPCname;
		while (npcs.containsKey(id)) {
			id = NPCname + i;
			i++;
		}
		return spawnNPC(NPCname, l, id);
	}
	
	private NPCEntity spawnNPC(String NPCname, Location l, String id) {
		if (npcs.containsKey(id)) {
			plugin.consoleWarning("NPC with that id already exists, existing NPC returned");
			return npcs.get(id);
		} else {
			if (NPCname.length() > 16) { // Check and nag if name is too long, spawn NPC anyway with shortened name.
				String tmp = NPCname.substring(0, 16);
				plugin.consoleWarning("NPCs can't have names longer than 16 characters,");
				plugin.consoleWarning(NPCname + " has been shortened to " + tmp);
				NPCname = tmp;
			}
			BWorld world = new BWorld(l.getWorld());
			NPCEntity npcEntity = new NPCEntity(server.getMCServer(), world.getWorldServer(), NPCname, new ItemInWorldManager(world.getWorldServer()));
			npcEntity.setPositionRotation(l.getBlockX(), l.getBlockY(), l.getBlockZ(), l.getYaw(), l.getPitch());
			world.getWorldServer().getChunkAt(l.getWorld().getChunkAt(l).getX(), l.getWorld().getChunkAt(l).getZ()).a(npcEntity);
			world.getWorldServer().addEntity(npcEntity);
			npcs.put(id, npcEntity);
			return npcEntity;
		}
	}

	/**
	 * Despawn a NPC by ID
	 * 
	 * @param id - ID of NPC
	 * @since 0.5
	 */
	public void despawnById(String id) {
		NPCEntity npc = npcs.get(id);
		if (npc != null) {
			npcs.remove(id);
			try {
				npc.world.removeEntity(npc);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Despawn a NPC by name
	 * 
	 * @param NPCname - Name of NPC
	 * @since 0.5
	 */
	public void despawnByName(String NPCname) {
		if (NPCname.length() > 16) {
			NPCname = NPCname.substring(0, 16); //Ensure you can still despawn
		}
		HashSet<String> toRemove = new HashSet<String>();
		for (String n : npcs.keySet()) {
			NPCEntity npc = npcs.get(n);
			if (npc != null && npc.name.equals(NPCname)) {
				toRemove.add(n);
				try {
					npc.world.removeEntity(npc);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		for (String n : toRemove) {
			npcs.remove(n);
		}
	}

	/**
	 * Move NPC to new location with new rotation
	 * 
	 * @param NPCname - ID of NPC
	 * @param l - New location
	 * @see moveNPCStatic(String NPCname, Location l);
	 */
	public void moveNPC(String id, Location l) {
		NPCEntity npc = npcs.get(id);
		if (npc != null) {
			npc.setPositionRotation(l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
		}
	}
	
	/**
	 * Move NPC to new location
	 * 
	 * @param NPCname - Name of NPC
	 * @param l - New location
	 * @see moveNPC(String NPCname, Location l);
	 */
	public void moveNPCStatic(String NPCname, Location l) {
		NPCEntity npc = npcs.get(NPCname);
		if (npc != null) {
			npc.setPosition(l.getX(), l.getY(), l.getZ());
		}
	}
	
	/**
	 * Get NPCEntity
	 * 
	 * @param id - ID of NPC
	 * @return NPCEntity of NPC - If not found: returns null
	 */
	public NPCEntity getNPC(String id) {
		return npcs.get(id);
	}
	
	/**
	 * Get list of NPCs with the name
	 * 
	 * @param name - Name of NPC(s)
	 * @since 0.5
	 * @return List of NPCs
	 */
	public List<NPCEntity> getNPCsByName(String name) {
		List<NPCEntity> ret = new ArrayList<NPCEntity>();
		Collection<NPCEntity> i = npcs.values();
		for (NPCEntity e : i) {
			if (e.getName().equalsIgnoreCase(name)) {
				ret.add(e);
			}
		}
		return ret;
	}
	
	/**
	 * Get list of all NPCs
	 * 
	 * @return List of NPCs
	 */
	public List<NPCEntity> getNPCs() {
		return new ArrayList<NPCEntity>(npcs.values());
	}
	
	/**
	 * Rename a NPC
	 * 
	 * @param id - ID of NPC
	 * @param name - New name of NPC
	 * @since 0.5
	 */
	public void rename(String id, String name) {
		if (name.length() > 16) { // Check and nag if name is too long, spawn NPC anyway with shortened name.
			String tmp = name.substring(0, 16);
			plugin.consoleWarning("NPCs can't have names longer than 16 characters,");
			plugin.consoleWarning(name + " has been shortened to " + tmp);
			name = tmp;
		}
		NPCEntity npc = getNPC(id);
		npc.setName(name);
		BWorld b = new BWorld(npc.getBukkitEntity().getLocation().getWorld());
		WorldServer s = b.getWorldServer();
		try {
			Method m = s.getClass().getDeclaredMethod("d", new Class[]{Entity.class});
			m.setAccessible(true);
			m.invoke(s, (Entity) npc);
			m = s.getClass().getDeclaredMethod("c", new Class[]{Entity.class});
			m.setAccessible(true);
			m.invoke(s, (Entity) npc);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		s.everyoneSleeping();
	}
}
