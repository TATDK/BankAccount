package dk.earthgame.martin.bukkit.npclib;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityTargetEvent;

public class NPCEntityTargetEvent extends EntityTargetEvent {
	private static final long serialVersionUID = -8103432985035183865L;

	public static enum NPCTargetReason {
		CLOSEST_PLAYER, NPC_RIGHTCLICKED, NPC_BOUNCED
	}
	private NPCTargetReason reason;

	/**
	 * 
	 * @param entity
	 * @param target
	 * @param reason
	 */
	public NPCEntityTargetEvent(Entity entity, Entity target, NPCTargetReason reason) {
		super(entity, target, TargetReason.CUSTOM);
		this.reason = reason;
	}

	/**
	 * Get reason for the event to happen
	 * @return NPCTargetReason
	 */
	public NPCTargetReason getNPCReason() {
		return this.reason;
	}
}