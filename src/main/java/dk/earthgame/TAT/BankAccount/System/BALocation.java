package dk.earthgame.TAT.BankAccount.System;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * Easy save and load locations
 * Used by BankAccount to manage locations
 * @author TAT
 * @since 0.6
 */
public class BALocation extends Location {
    public BALocation(World world, double x, double y, double z) {
		super(world, x, y, z);
	}
    
    public BALocation(Location l) {
    	super(l.getWorld(),l.getX(),l.getY(),l.getZ());
    }
    
    /**
     * Get location in output separated simply by comma (,)
     * @return Location of sign (x,y,z)
     * @see #locOutput(String)
     */
    public String locOutput() {
    	return locOutput(",",true);
    }
    
    /**
     * Get location in output
     * @param sep Separator
     * @return Location of sign (world{sep}x{sep}y{sep}z)
     * If sep is a space the return will be: world,x,y,z
     * If sep is a slash(/) the return will be: world/x/y/z
     */
    public String locOutput(String sep,boolean includeWorld) {
    	if (includeWorld)
    		return getWorld().getName() + sep + getBlockX() + sep + getBlockY() + sep + getBlockZ();
		return getBlockX() + sep + getBlockY() + sep + getBlockZ();
    }
}