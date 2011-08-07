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
                    if (args.length == 9) {
                        World w = plugin.getServer().getWorld(args[0]);
                        areas.add(new Area(
                            args[8],
                            new BALocation(w, Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3])),
                            new BALocation(w, Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6])),
                            Integer.parseInt(args[7])));
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
                writer.write(pos1.locOutput() + "," + pos2.locOutput(",",false) + "," + area.getBankID() + "," + area.getName() + "\n");
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
     * Are the position inside an area
     * 
     * @param world Name of world
     * @param pos Position
     * @since 0.5
     * @return If the position is inside an area
     */
    public boolean inside(Location pos) {
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
                return true;
            }
        }
        return false;
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
        if (exists(name))
            return false;
        areas.add(new Area(name,pos1,pos2,0));
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
