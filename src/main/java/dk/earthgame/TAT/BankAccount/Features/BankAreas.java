package dk.earthgame.TAT.BankAccount.Features;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import dk.earthgame.TAT.BankAccount.BankAccount;
import dk.earthgame.TAT.BankAccount.System.BALocation;

public class BankAreas {
    private BankAccount plugin;
    private String filename = "Areas.dat";
    private List<Area> areas = new ArrayList<Area>();

    public BankAreas(dk.earthgame.TAT.BankAccount.BankAccount instantiate) {
        plugin = instantiate;
    }

    /**
     * Load bank areas from .dat file
     * @since 0.6
     */
    public void load() {
        File signFile = new File(plugin.getDataFolder(), filename);
        if (signFile.exists()) {
            try {
                FileReader fr = new FileReader(signFile);
                BufferedReader reader = new BufferedReader(fr);
                String s;
                int line = 0;
                areas.clear();
                while ((s = reader.readLine()) != null) {
                    line++;
                    String[] args = s.split(",");
                    if (args.length == 10) {
                        World w = plugin.getServer().getWorld(args[1]);
                        areas.add(new Area(
                        	Integer.parseInt(args[0]),
                            args[9],
                            new BALocation(w, Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4])),
                            new BALocation(w, Integer.parseInt(args[5]), Integer.parseInt(args[6]), Integer.parseInt(args[7])),
                            Integer.parseInt(args[8])));
                    } else {
                        plugin.console.warning(filename + " contains errors on line " + line);
                    }
                }
                fr.close();
            } catch (Exception e) {
                plugin.console.warning("Error loading " + filename);
                e.printStackTrace();
            }
        }
    }

    /**
     * Save bank areas to .dat file
     * @since 0.6
     */
    public void save() {
        try {
            File signFile = new File(plugin.getDataFolder(), filename);
            FileWriter writer = new FileWriter(signFile);
            for (Area area: areas) {
                BALocation pos1 = (BALocation)area.getLocation(1);
                BALocation pos2 = (BALocation)area.getLocation(2);
                writer.write(area.getID() + "," + pos1.locOutput() + "," + pos2.locOutput(",",false) + "," + area.getBankID() + "," + area.getName() + "\n");
            }
            writer.flush();
            writer.close();
        } catch (Exception e) {
            plugin.console.warning("Can't save to " + filename);
            e.printStackTrace();
        }
    }

    /**
     * Does an area exists
     * 
     * @param name Name of area
     * @since 0.5
     * @return If the area exists
     */
    public boolean exists(String name) {
        for (Area area: areas) {
            if (area.getName().equalsIgnoreCase(name))
                return true;
        }
        return false;
    }

    /**
     * Does an area exists
     * 
     * @param ID ID of area
     * @since 0.6
     * @return If the area exists
     */
    public boolean exists(int ID) {
        for (Area area: areas) {
            if (area.getID() == ID)
                return true;
        }
        return false;
    }

    /**
     * Are the position inside an area
     * 
     * @param pos Position
     * @since 0.5
     * @return If the position is inside an area
     */
    public boolean inside(Location pos) {
        if (insideGet(pos) != null)
            return true;
        return false;
    }
    
    /**
     * Get the area that the pos is inside
     * @param pos Position
     * @since 0.6
     * @return Area if found; else null
     */
    public Area insideGet(Location pos) {
        for (Area area: areas) {
            Vector min = new Vector(
                Math.min(area.getLocation(1).getBlockX(), area.getLocation(2).getBlockX()),
                Math.min(area.getLocation(1).getBlockY(), area.getLocation(2).getBlockY()),
                Math.min(area.getLocation(1).getBlockZ(), area.getLocation(2).getBlockZ())
            );
            Vector max = new Vector(
                Math.max(area.getLocation(1).getBlockX(), area.getLocation(2).getBlockX()),
                Math.max(area.getLocation(1).getBlockY(), area.getLocation(2).getBlockY()),
                Math.max(area.getLocation(1).getBlockZ(), area.getLocation(2).getBlockZ())
            );

            if (pos.getBlockX() >= min.getBlockX() && pos.getBlockX() <= max.getBlockX() &&
                pos.getBlockY() >= min.getBlockY() && pos.getBlockY() <= max.getBlockY() &&
                pos.getBlockZ() >= min.getBlockZ() && pos.getBlockZ() <= max.getBlockZ()) {
                return area;
            }
        }
        return null;
    }
    
    /**
     * Get all areas that contains a specific location
     * @param pos The location
     * @since 0.6
     * @return List of areas
     */
    public List<Integer> getAreas(Location pos) {
    	List<Integer> foundareas = new ArrayList<Integer>();
    	for (Area area: areas) {
            Vector min = new Vector(
                Math.min(area.getLocation(1).getBlockX(), area.getLocation(2).getBlockX()),
                Math.min(area.getLocation(1).getBlockY(), area.getLocation(2).getBlockY()),
                Math.min(area.getLocation(1).getBlockZ(), area.getLocation(2).getBlockZ())
            );
            Vector max = new Vector(
                Math.max(area.getLocation(1).getBlockX(), area.getLocation(2).getBlockX()),
                Math.max(area.getLocation(1).getBlockY(), area.getLocation(2).getBlockY()),
                Math.max(area.getLocation(1).getBlockZ(), area.getLocation(2).getBlockZ())
            );

            if (pos.getBlockX() >= min.getBlockX() && pos.getBlockX() <= max.getBlockX() &&
                pos.getBlockY() >= min.getBlockY() && pos.getBlockY() <= max.getBlockY() &&
                pos.getBlockZ() >= min.getBlockZ() && pos.getBlockZ() <= max.getBlockZ()) {
                foundareas.add(area.getID());
            }
        }
        return foundareas;
    }
    
    /**
     * Get a specific area
     * @param ID ID of area
     * @since 0.6
     * @return Area if found; else null
     */
    public Area get(int ID) {
    	for (Area area: areas) {
            if (area.getID() == ID)
                return area;
        }
        return null;
    }
    
    public int entered(Location from,Location to) {
    	List<Integer> fromAreas = getAreas(from);
    	List<Integer> toAreas = getAreas(to);
    	for (int a : fromAreas) {
    		if (toAreas.contains(a))
    			toAreas.remove(toAreas.indexOf(a));
    	}
    	return toAreas.size();
    }
    
    public int left(Location from,Location to) {
    	List<Integer> fromAreas = getAreas(from);
    	List<Integer> toAreas = getAreas(to);
    	for (int a : toAreas) {
    		if (fromAreas.contains(a))
    			fromAreas.remove(fromAreas.indexOf(a));
    	}
    	return fromAreas.size();
    }
    
    /**
     * Check if you enter an area from position 1 to position 2
     * @param from Position before
     * @param to Position now
     * @since 0.6
     * @return Areas the player have entered
     */
    public List<Integer> enteringAreas(Location from,Location to) {
    	List<Integer> fromAreas = getAreas(from);
    	List<Integer> toAreas = getAreas(to);
    	for (int a : fromAreas) {
    		if (toAreas.contains(a))
    			toAreas.remove(toAreas.indexOf(a));
    	}
    	return toAreas;
    }
    
    /**
     * Check if you left an area from position 1 to position 2
     * @param from Position before
     * @param to Position now
     * @since 0.6
     * @return Areas the player have left
     */
    public List<Integer> leavingAreas(Location from,Location to) {
    	List<Integer> fromAreas = getAreas(from);
    	List<Integer> toAreas = getAreas(to);
    	for (int a : toAreas) {
    		if (fromAreas.contains(a))
    			fromAreas.remove(fromAreas.indexOf(a));
    	}
    	return fromAreas;
    }

    /**
     * Add an area
     * 
     * @param name Name of area
     * @param pos1 Position 1
     * @param pos2 Position 2
     * @since 0.5
     * @return If the area is successfully added
     */
    public boolean add(String name,Location pos1,Location pos2) {
    	int newID;
    	if (areas.size() > 0)
    		newID = areas.get(areas.size()-1).getID()+1;
    	else
    		newID = 1;
    	return add(newID,name,pos1,pos2);
    }

    /**
     * Add an area
     * 
     * @param id ID of area
     * @param name Name of area
     * @param pos1 Position 1
     * @param pos2 Position 2
     * @since 0.6
     * @return If the area is successfully added
     */
    public boolean add(int id,String name,Location pos1,Location pos2) {
        if (exists(name))
            return false;
        areas.add(new Area(id,name,pos1,pos2,0));
        save();
        return true;
    }

    /**
     * Remove an area
     * 
     * @param name Name of area
     * @since 0.5
     * @return If the area is successfully removed
     */
    public boolean remove(String name) {
        for (Area a : areas) {
            if (a.getName().equalsIgnoreCase(name)) {
                areas.remove(a);
                save();
                return true;
            }
        }
        return false;
    }
}
