package dk.earthgame.TAT.BankAccount.Features;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.block.Sign;

import dk.earthgame.TAT.BankAccount.BankAccount;
import dk.earthgame.TAT.BankAccount.System.BALocation;

public class ATMSign {
    public boolean enabled;
    private String filename = "ATMSigns.dat";
    private BankAccount plugin;
    private ConcurrentHashMap<BALocation,ATMMachine> signs = new ConcurrentHashMap<BALocation, ATMMachine>();

    public ATMSign(BankAccount instantiate) {
        plugin = instantiate;
    }

    /**
     * Load signs from .dat file
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
                signs.clear();
                while ((s = reader.readLine()) != null) {
                    line++;
                    String[] args = s.split(",");
                    if (args.length == 4) {
                        signs.put(new BALocation(plugin.getServer().getWorld(args[0]),Double.parseDouble(args[1]),Double.parseDouble(args[2]),Double.parseDouble(args[3])),new ATMMachine(plugin, (Sign)new Location(plugin.getServer().getWorld(args[0]),Double.parseDouble(args[1]),Double.parseDouble(args[2]),Double.parseDouble(args[3])).getBlock().getState()));
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
     * Reset all signs to default
     */
    public void resetAll() {
        for (Entry<BALocation,ATMMachine> entry : signs.entrySet())
        	entry.getValue().reset();
    }

    /**
     * Save signs to .dat file
     * @since 0.6
     */
    public void save() {
        try {
            File signFile = new File(plugin.getDataFolder(), filename);
            FileWriter writer = new FileWriter(signFile);
            for (Entry<BALocation, ATMMachine> sign: signs.entrySet()) {
                BALocation location = sign.getKey();
                writer.write(location.locOutput() + "\n");
            }
            writer.flush();
            writer.close();
        } catch (Exception e) {
            plugin.console.warning("Can't save to " + filename);
            e.printStackTrace();
        }
    }

    public ATMMachine get(BALocation l) {
        if (exists(l)) {
            return signs.get(l);
        }
        return null;
    }

    /**
     * Add a ATM sign
     * 
     * @param w World
     * @param l Location
     * @since 0.6
     */
    public void add(BALocation l) {
        signs.put(l,new ATMMachine(plugin, (Sign)l.getBlock().getState()));
        save();
    }

    /**
     * Remove a ATM sign
     * 
     * @param w World
     * @param l Location
     * @since 0.6
     */
    public void remove(BALocation l) {
        for (Entry<BALocation, ATMMachine> sign: signs.entrySet())
            if (sign.getKey().locOutput().equalsIgnoreCase(l.locOutput()))
                signs.remove(sign.getKey());
        save();
    }

    /**
     * Check if ATM sign exists
     * 
     * @param w World
     * @param l Location
     * @since 0.6
     * @return true if the sign exists, otherwise false
     */
    public boolean exists(BALocation l) {
        for (Entry<BALocation, ATMMachine> sign: signs.entrySet())
            if (sign.getKey().locOutput().equalsIgnoreCase(l.locOutput()))
                return true;
        return false;
    }
}