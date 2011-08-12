package dk.earthgame.TAT.BankAccount.Features;

import org.bukkit.Location;

import dk.earthgame.TAT.BankAccount.System.BALocation;

/**
 * Container for area informations
 * @author TAT
 * @since 0.6
 */
public class Area {
    private String name;
    private BALocation pos1,pos2;
    private int bankID;
    
    public Area(String name,Location pos1,Location pos2,int bankID) {
        this.name = name;
        this.pos1 = new BALocation(pos1);
        this.pos2 = new BALocation(pos2);
        this.bankID = bankID;
    }
    
    /**
     * Get location
     * @param pos Location 1 or 2
     * @return Location
     */
    public Location getLocation(int pos) {
        if (pos == 1)
            return pos1;
        return pos2;
    }
    
    /**
     * Get bank ID of area
     * @return Bank ID
     */
    public int getBankID() { return bankID; }
    
    /**
     * Get area name
     * @return Name of area
     */
    public String getName() { return name; }
}
