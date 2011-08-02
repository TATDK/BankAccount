package dk.earthgame.TAT.BankAccount.Features;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;

import dk.earthgame.TAT.BankAccount.BankAccount;
import dk.earthgame.TAT.BankAccount.System.SignLocation;

public class ATMSign {
	private BankAccount plugin;
    private HashMap<SignLocation,Integer> signs = new HashMap<SignLocation, Integer>();
    
    public ATMSign(BankAccount instantiate) {
    	plugin = instantiate;
    }
    
    /**
     * Load signs from dat file
     * 
     * @since 0.6
     */
    public void loadSigns() {
        File signFile = new File(plugin.getDataFolder(), "ATMSigns.dat");
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
                        signs.put(new SignLocation(plugin.getServer().getWorld(args[0]), new Location(plugin.getServer().getWorld(args[0]),Double.parseDouble(args[1]),Double.parseDouble(args[2]),Double.parseDouble(args[3]))),0);
                    } else {
                        plugin.console.warning("Sign.dat contains errors on line " + line);
                    }
                }
                fr.close();
            } catch (Exception e) {
                plugin.console.warning("Error loading signs");
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Save signs to dat file
     * 
     * @since 0.6
     */
    public void saveSigns() {
        try {
            File signFile = new File(plugin.getDataFolder(), "ATMSigns.dat");
            FileWriter writer = new FileWriter(signFile);
            for (Map.Entry<SignLocation, Integer> sign: signs.entrySet()) {
                SignLocation location = sign.getKey();
                writer.write(location.getWorld().getName() + "," + location.locOutput() + "\n");
            }
            writer.flush();
            writer.close();
        } catch (Exception e) {
            plugin.console.warning("Can't save signs");
            e.printStackTrace();
        }
    }
    
    /**
     * Add a ATM sign
     * 
     * @param w World
     * @param l Location
     * @since 0.6
     * @throws BankAccountException 
     * @throws IndexOutOfBoundsException 
     */
    public void add(World w,Location l) {
        ((Sign)w.getBlockAt(l).getState()).setLine(0, "[BankAccount]");
        signs.put(new SignLocation(w, l),0);
        saveSigns();
    }
    
    /**
     * Remove a ATM sign
     * 
     * @param w World
     * @param l Location
     * @since 0.6
     */
    public void removeSign(World w,Location l) {
        for (Map.Entry<SignLocation, Integer> sign: signs.entrySet()) {
            if (sign.getKey().getWorld().equals(w) && sign.getKey().getLocation().equals(l)) {
                signs.remove(sign.getKey());
            }
        }
        saveSigns();
    }
    
    /**
     * Check if ATM sign exists
     * 
     * @param w World
     * @param l Location
     * @since 0.6
     * @return true if the sign exists, otherwise false
     */
    public boolean signExists(World w,Location l) {
        for (Map.Entry<SignLocation, Integer> sign: signs.entrySet()) {
            if (sign.getKey().getWorld().equals(w) && sign.getKey().getLocation().equals(l)) {
                return true;
            }
        }
        return false;
    }
}