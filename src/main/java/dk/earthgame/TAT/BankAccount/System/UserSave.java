package dk.earthgame.TAT.BankAccount.System;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

import dk.earthgame.TAT.BankAccount.Features.ATMMachine;
import dk.earthgame.TAT.BankAccount.Features.Area;

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
	private List<Integer> inAreas = new ArrayList<Integer>();
    public ATMMachine usingATM = null;
    
    public UserSave(UserSaves instantiate) {
    	master = instantiate;
    }
    
    public UserSave(UserSaves instantiate, double bounty) {
    	master = instantiate;
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
    
    /**
     * Is the user inside an specific area?
     * @param area The area
     * @since 0.6
     * @return true if the player is in the area; else false
     */
    public boolean inArea(Area area) { return inAreas.contains(area.getID()); }
    
    /**
     * Is the user inside an specific area?
     * @param area ID of area
     * @since 0.6
     * @return true if the player is in the area; else false
     */
    public boolean inArea(int area) { return inAreas.contains(area); }
    
    /**
     * Register the user entering an area
     * @param area The area
     * @since 0.6
     */
    public void enterArea(Area area) {
    	if (master.plugin.BankAreas.exists(area.getName()) && !inArea(area))
    		inAreas.add(area.getID());
    }

    /**
     * Register the user entering an area
     * @param area ID of area
     * @since 0.6
     */
    public void enterArea(int area) {
    	if (master.plugin.BankAreas.exists(area) && !inArea(area))
    		inAreas.add(area);
    }
    
    /**
     * Register the user leaves an area
     * @param area The area
     * @since 0.6
     */
    public void exitArea(Area area) {
    	if (inArea(area))
    		inAreas.remove(inAreas.indexOf(area.getID()));
    }
    
    /**
     * Register the user leaves an area
     * @param area ID of area
     * @since 0.6
     */
    public void exitArea(int area) {
    	if (inArea(area))
    		inAreas.remove(inAreas.indexOf(area));
    }
}
