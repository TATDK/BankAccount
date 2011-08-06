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
import dk.earthgame.TAT.SignUpdater.UpdaterPriority;

public class BalanceSign {
	public boolean enabled;
	private BankAccount plugin;
    private HashMap<SignLocation,String> signs = new HashMap<SignLocation, String>();
    
    public BalanceSign(BankAccount instantiate) {
    	plugin = instantiate;
    }
    
    /**
     * Load signs from dat file
     * 
     * @since 0.6
     */
    public void load() {
        File signFile = new File(plugin.getDataFolder(), "BalanceSigns.dat");
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
                    if (args.length == 5) {
                        signs.put(new SignLocation(plugin.getServer().getWorld(args[0]), new Location(plugin.getServer().getWorld(args[0]),Double.parseDouble(args[1]),Double.parseDouble(args[2]),Double.parseDouble(args[3]))),args[4]);
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
    public void save() {
        try {
            File signFile = new File(plugin.getDataFolder(), "BalanceSigns.dat");
            FileWriter writer = new FileWriter(signFile);
            for (Map.Entry<SignLocation, String> sign: signs.entrySet()) {
                SignLocation location = sign.getKey();
                String account = sign.getValue();
                writer.write(location.getWorld().getName() + "," + location.locOutput() + "," + account + "\n");
            }
            writer.flush();
            writer.close();
        } catch (Exception e) {
            plugin.console.warning("Can't save signs");
            e.printStackTrace();
        }
    }
    
    /**
     * Add a sign
     * 
     * @param w World
     * @param l Location
     * @param accountname Name of account
     * @since 0.6
     * @throws BankAccountException 
     * @throws IndexOutOfBoundsException 
     */
    public void add(World w,Location l,String accountname) {
        ((Sign)w.getBlockAt(l).getState()).setLine(0, "[BankAccount]");
        signs.put(new SignLocation(w, l), accountname);
        update(accountname);
        save();
    }
    
    /**
     * Remove a sign
     * 
     * @param w World
     * @param l Location
     * @since 0.6
     */
    public void remove(World w,Location l) {
        for (Map.Entry<SignLocation, String> sign: signs.entrySet()) {
            if (sign.getKey().getWorld().equals(w) && sign.getKey().getLocation().equals(l)) {
                signs.remove(sign.getKey());
            }
        }
        save();
    }
    
    /**
     * Check if balance sign exists
     * 
     * @param w World
     * @param l Location
     * @since 0.6
     * @return true if the sign exists, otherwise false
     */
    public boolean exists(World w,Location l) {
        for (Map.Entry<SignLocation, String> sign: signs.entrySet()) {
            if (sign.getKey().getWorld().equals(w) && sign.getKey().getLocation().equals(l)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get account on sign on location
     * @param w World
     * @param l Location
     * @since 0.6
     * @return Accountname if fould, else null
     */
    public String getAccount(World w,Location l) {
    	for (Map.Entry<SignLocation, String> sign: signs.entrySet()) {
            if (sign.getKey().getWorld().equals(w) && sign.getKey().getLocation().equals(l)) {
                return sign.getValue();
            }
        }
        return null;
    }
    
    /**
     * Check if SignUpdater is set
     * 
     * @since 0.6
     * @return true if SignUpdater is set, otherwise false + console warning
     */
    public boolean checkSignUpdater() {
        if (plugin.signupdater == null) {
            plugin.console.warning("Missing plugin -> SignUpdater");
            return false;
        }
        return true;
    }
    
    /**
     * Update all signs
     * 
     * @since 0.6
     * @throws BankAccountException 
     * @throws IndexOutOfBoundsException 
     */
    public void update() {
        checkSignUpdater();
        HashMap<String, Double> balances = new HashMap<String, Double>();
        for (Map.Entry<SignLocation, String> sign: signs.entrySet()) {
            double balance = 0;
            if (balances.containsKey(sign.getValue())) {
                balance = balances.get(sign.getValue());
            } else {
                balance = plugin.getAccount(sign.getValue()).getBalance();
                balances.put(sign.getValue(), balance);
            }
            SignLocation sl = sign.getKey();
            if (sl.getBlock().getState() instanceof Sign) {
                Sign foundSign = (Sign) sl.getBlock().getState();
                if (foundSign.getLine(0).equalsIgnoreCase("[BankAccount]")) {
                    if (plugin.accountExists(sign.getValue())) {
                        String[] lines = {foundSign.getLine(0), sign.getValue(), plugin.Method.format(balance), ""};
                        plugin.signupdater.AddSignUpdate(UpdaterPriority.NORMAL, foundSign, lines);
                    } else {
                        String[] lines = {foundSign.getLine(0), sign.getValue(), "Account closed", ""};
                        plugin.signupdater.AddSignUpdate(UpdaterPriority.NORMAL, foundSign, lines);
                    }
                } else {
                    remove(sl.getWorld(), sl.getLocation());
                    plugin.console.warning("Sign in world " + sl.getWorld().getName() + " at " + sl.locOutput(" ") + " can't be found - removed");
                }
            } else {
                remove(sl.getWorld(), sl.getLocation());
                plugin.console.warning("Sign in world " + sl.getWorld().getName() + " at " + sl.locOutput(" ") + " can't be found - removed");
            }
        }
    }
    
    /**
     * Update all signs that showing defined account
     * 
     * @since 0.6
     * @param accountname Name of account
     * @throws BankAccountException 
     * @throws IndexOutOfBoundsException 
     */
    public void update(String accountname) {
        checkSignUpdater();
        double balance = plugin.getAccount(accountname).getBalance();
        for (Map.Entry<SignLocation, String> sign: signs.entrySet()) {
            if (sign.getValue().equalsIgnoreCase(accountname)) {
                SignLocation sl = sign.getKey();
                if (sl.getBlock().getState() instanceof Sign) {
                    Sign foundSign = (Sign) sl.getBlock().getState();
                    if (foundSign.getLine(0).equalsIgnoreCase("[BankAccount]")) {
                        if (plugin.accountExists(foundSign.getLine(1))) {
                            String[] lines = {foundSign.getLine(0), sign.getValue(), plugin.Method.format(balance), ""};
                            plugin.signupdater.AddSignUpdate(UpdaterPriority.NORMAL, foundSign, lines);
                        } else {
                            String[] lines = {foundSign.getLine(0), sign.getValue(), "Account closed", ""};
                            plugin.signupdater.AddSignUpdate(UpdaterPriority.NORMAL, foundSign, lines);
                        }
                    } else {
                        remove(sl.getWorld(), sl.getLocation());
                        plugin.console.warning("Sign in world " + sl.getWorld().getName() + " at " + sl.locOutput(" ") + " can't be found - removed");
                    }
                } else {
                    remove(sl.getWorld(), sl.getLocation());
                    plugin.console.warning("Sign in world " + sl.getWorld().getName() + " at " + sl.locOutput(" ") + " can't be found - removed");
                }
            }
        }
    }
}