package dk.earthgame.TAT.BankAccount.System;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * 
 * @author TAT
 * @since 0.6
 */
public class SignLocation {
    private World w;
    private Location l;
    
    /**
     * Create new SignLocation
     * @param world World, where the sign is located
     * @param location Location of sign in world
     */
    public SignLocation (World world, Location location) {
        w = world;
        l = location;
    }
    
    /**
     * Get World of the sign
     * @since 0.6
     * @return World
     */
    public World getWorld() { return w; }
    
    /**
     * Get Location of the sign
     * @since 0.6
     * @return Location
     */
    public Location getLocation() { return l; }
    
    /**
     * Get Block on the sign's location
     * @since 0.6
     * @return Block
     */
    public Block getBlock() { return w.getBlockAt(l); }
    
    /**
     * Get location in output separated simply by comma (,)
     * @return Location of sign (x,y,z)
     * @see locOutput(String sep)
     */
    public String locOutput() {
    	return locOutput("");
    }
    
    /**
     * Get location in output
     * @param sep Separator after ,
     * @return Location of sign (x,{sep}y,{sep}z)
     */
    public String locOutput(String sep) {
    	return l.getBlockX() + "," + sep + l.getBlockY() + "," + sep + l.getBlockZ();
    }
}