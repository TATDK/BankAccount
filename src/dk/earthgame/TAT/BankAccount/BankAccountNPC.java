package dk.earthgame.TAT.BankAccount;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityTargetEvent;

import org.martin.bukkit.npclib.*;
import org.martin.bukkit.npclib.NPCEntityTargetEvent.NPCTargetReason;

/**
 * NPCs behavior on player interaction
 * @author TAT
 */
public class BankAccountNPC extends EntityListener {
	@SuppressWarnings("unused")
	private BankAccount plugin;
	
	public BankAccountNPC(BankAccount instantiate) {
		plugin = instantiate;
	}

	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof NPCEntity) {
			if (event.getDamager() instanceof Player) {
				NPCEntity npc = (NPCEntity)event.getEntity();
				Player p = (Player)event.getDamager();
				p.sendMessage("<" + npc.getName() + "> Don't hit me so much :P");

				//NpcSpawner.RemoveBasicHumanNpc(npc);
				//plugin.m.despawn(npc.getName());

				event.setCancelled(true);
			}
		}
	}

	@Override
	public void onEntityTarget(EntityTargetEvent event) {
		if (event instanceof NPCEntityTargetEvent) {
			NPCEntityTargetEvent nevent = (NPCEntityTargetEvent)event;

			if (event.getEntity() instanceof NPCEntity) {
				NPCEntity npc = (NPCEntity)event.getEntity(); 
				if (event.getTarget() instanceof Player) {
					if (nevent.getNPCReason() == NPCTargetReason.CLOSEST_PLAYER) {
						Player p = (Player) event.getTarget();
						p.sendMessage("<" + npc.getName() + "> Hello friend, I'm an NPC!");
						event.setCancelled(true);
		
					} else if (nevent.getNPCReason() == NPCTargetReason.NPC_RIGHTCLICKED) {
						Player p = (Player) event.getTarget();
						p.sendMessage("<" + npc.getName() + "> You right-clicked me!");
						event.setCancelled(true);
		
					} else if (nevent.getNPCReason() == NPCTargetReason.NPC_BOUNCED) {
						Player p = (Player) event.getTarget();
						p.sendMessage("<" + npc.getName() + "> Stop bouncing on me!");
						event.setCancelled(true);
					}
				}
			}
		}
	}
}