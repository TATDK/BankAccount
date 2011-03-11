package dk.earthgame.TAT.BankAccount;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityTargetEvent;

import redecouverte.npcspawner.BasicHumanNpc;
import redecouverte.npcspawner.BasicHumanNpcList;
import redecouverte.npcspawner.NpcEntityTargetEvent;
import redecouverte.npcspawner.NpcEntityTargetEvent.NpcTargetReason;
import redecouverte.npcspawner.NpcSpawner;

public class BankAccountNPC extends EntityListener {
	@SuppressWarnings("unused")
	private BankAccount plugin;
	public BasicHumanNpcList HumanNPCList;
	
	public BankAccountNPC(BankAccount instantiate) {
		plugin = instantiate;
		HumanNPCList = new BasicHumanNpcList();
	}

	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

		if (event.getEntity() instanceof HumanEntity) {
			BasicHumanNpc npc = HumanNPCList.getBasicHumanNpc(event.getEntity());

			if (npc != null && event.getDamager() instanceof Player) {

				Player p = (Player) event.getDamager();
				p.sendMessage("<" + npc.getName() + "> Don't hit me so much :P");

				NpcSpawner.RemoveBasicHumanNpc(npc);
				HumanNPCList.remove(npc.getUniqueId());

				event.setCancelled(true);

			}

		}
	}

	@Override
	public void onEntityTarget(EntityTargetEvent event) {

		if (event instanceof NpcEntityTargetEvent) {
			NpcEntityTargetEvent nevent = (NpcEntityTargetEvent)event;

			BasicHumanNpc npc = HumanNPCList.getBasicHumanNpc(event.getEntity());

			if (npc != null && event.getTarget() instanceof Player) {
				if (nevent.getNpcReason() == NpcTargetReason.CLOSEST_PLAYER) {
					Player p = (Player) event.getTarget();
					p.sendMessage("<" + npc.getName() + "> Hello friend, I'm an NPC!");
					event.setCancelled(true);

				} else if (nevent.getNpcReason() == NpcTargetReason.NPC_RIGHTCLICKED) {
					Player p = (Player) event.getTarget();
					p.sendMessage("<" + npc.getName() + "> You right-clicked me!");
					event.setCancelled(true);

				} else if (nevent.getNpcReason() == NpcTargetReason.NPC_BOUNCED) {
					Player p = (Player) event.getTarget();
					p.sendMessage("<" + npc.getName() + "> Stop bouncing on me!");
					event.setCancelled(true);
				}
			}
		}

	}
}
