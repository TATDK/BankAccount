package dk.earthgame.TAT.BankAccount.System;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class SignLocation {
    World w;
    Location l;
    
    public SignLocation (World world, Location location) {
        w = world;
        l = location;
    }
    
    public World getWorld() { return w; }
    public Location getLocation() { return l; }
    public Block getBlock() { return w.getBlockAt(l); }
}