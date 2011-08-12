package dk.earthgame.TAT.BankAccount.System;

import org.bukkit.Location;

import dk.earthgame.TAT.BankAccount.Features.ATMMachine;

/**
 * Saved informations of an user
 * @author TAT
 * @since 0.5
 */
public class UserSave {
	private UserSaves master;
    private double bounty;
    private boolean selecting;
    private static Location pos1;
    private static Location pos2;
    public ATMMachine usingATM = null;
    
    public UserSave(UserSaves instantiate) {}
    
    public UserSave(UserSaves instantiate, double bounty) {
    	this.bounty = bounty;
    }
    
    /**
     * Change the bounty 
     * 
     * @param bounty New bounty on the user's head
     * @since 0.5
     */
    public void setBounty(double bounty) {
        this.bounty = bounty;
        master.save();
    }
    
    /**
     * Get the bounty
     * 
     * @since 0.5
     * @return double - Bounty on the user's head
     */
    public double getBounty() {
        return bounty;
    }
    
    /**
     * Get if the user is selecting
     * 
     * @since 0.5
     * @return boolean
     */
    public boolean isSelecting() {
        return selecting;
    }
    
    /**
     * Set if the user is selecting
     * 
     * @param isSelecting Is the user selecting
     * @since 0.5
     */
    public void isSelecting(boolean isSelecting) {
        selecting = isSelecting;
    }
    
    /**
     * Clear the saved positions
     * 
     * @since 0.5
     */
    public void clearPositions() {
        pos1 = null;
        pos2 = null;
    }
    
    /**
     * Set the next available position
     * 
     * @param pos Location of the next position
     * @since 0.5
     * @return position set - 1: Position 1 set - 2: Position 2 set
     */
    public int setPosition(Location pos) {
        if (pos1 == null) {
            pos1 = pos;
            master.save();
            return 1;
        } else if (pos2 == null) {
            pos2 = pos;
            master.save();
            return 2;
        } else {
            pos2 = null;
            pos1 = pos;
            master.save();
            return 1;
        }
    }
    
    /**
     * Get the set position
     * 
     * @param position Position 1 or 2
     * @since 0.5
     * @return Location of the position - Returns null if position isn't set
     */
    public Location getPosition(int position) {
        if (position == 1 && pos1 != null) {
            return pos1;
        } else if (position == 2 && pos2 != null) {
            return pos2;
        } else {
            return null;
        }
    }
}
