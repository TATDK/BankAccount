package dk.earthgame.TAT.BankAccount.Features;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.block.Sign;

import dk.earthgame.TAT.BankAccount.BankAccount;
import dk.earthgame.TAT.BankAccount.System.BALocation;
import dk.earthgame.TAT.SignUpdater.UpdaterPriority;

public class BalanceSign {
    public boolean enabled;
    private BankAccount plugin;
    private String filename = "BalanceSigns.dat";
    private ConcurrentHashMap<BALocation,String> signs = new ConcurrentHashMap<BALocation, String>();
    
    public BalanceSign(BankAccount instantiate) {
    	plugin = instantiate;
    }

    /**
     * Load signs from .dat file
     * 
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
                    if (args.length == 5) {
                        signs.put(new BALocation(plugin.getServer().getWorld(args[0]),Double.parseDouble(args[1]),Double.parseDouble(args[2]),Double.parseDouble(args[3])),args[4]);
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
     * Save signs to .dat file
     * 
     * @since 0.6
     */
    public void save() {
        try {
            File signFile = new File(plugin.getDataFolder(), filename);
            FileWriter writer = new FileWriter(signFile);
            for (Map.Entry<BALocation, String> sign: signs.entrySet()) {
                BALocation location = sign.getKey();
                String account = sign.getValue();
                writer.write(location.locOutput() + "," + account + "\n");
            }
            writer.flush();
            writer.close();
        } catch (Exception e) {
            plugin.console.warning("Can't save to " + filename);
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
     */
    public void add(Location l,String accountname) {
        signs.put((BALocation)l, accountname);
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
    public void remove(Location l) {
        for (Map.Entry<BALocation, String> sign: signs.entrySet())
            if (sign.getKey().getWorld().equals(l.getWorld()) && sign.getKey().equals(l))
                signs.remove(sign.getKey());
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
    public boolean exists(Location l) {
        for (Map.Entry<BALocation, String> sign: signs.entrySet())
            if (sign.getKey().getWorld().equals(l.getWorld()) && sign.getKey().equals(l))
                return true;
        return false;
    }

    /**
     * Get account on sign on location
     * @param w World
     * @param l Location
     * @since 0.6
     * @return Accountname if fould, else null
     */
    public String getAccount(Location l) {
        for (Map.Entry<BALocation, String> sign: signs.entrySet())
            if (sign.getKey().getWorld().equals(l.getWorld()) && sign.getKey().equals(l))
                return sign.getValue();
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
     */
    public void update() {
        checkSignUpdater();
        HashMap<String, Double> balances = new HashMap<String, Double>();
        for (Map.Entry<BALocation, String> sign: signs.entrySet()) {
            double balance = 0;
            if (balances.containsKey(sign.getValue())) {
                balance = balances.get(sign.getValue());
            } else {
            	if (plugin.getAccount(sign.getValue()) != null)
            		balance = plugin.getAccount(sign.getValue()).getBalance();
            	else
            		balance = 0;
            	balances.put(sign.getValue(), balance);
            }
            BALocation sl = sign.getKey();
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
                    remove(sl);
                    plugin.console.warning("Sign in world " + sl.getWorld().getName() + " at " + sl.locOutput(", ",false) + " can't be found - removed");
                }
            } else {
                remove(sl);
                plugin.console.warning("Sign in world " + sl.getWorld().getName() + " at " + sl.locOutput(", ",false) + " can't be found - removed");
            }
        }
    }

    /**
     * Update all signs that showing defined account
     * 
     * @since 0.6
     * @param accountname Name of account
     */
    public void update(String accountname) {
        checkSignUpdater();
        double balance = plugin.getAccount(accountname).getBalance();
        for (Map.Entry<BALocation, String> sign: signs.entrySet()) {
            if (sign.getValue().equalsIgnoreCase(accountname)) {
                BALocation sl = sign.getKey();
                if (sl.getBlock().getState() instanceof Sign) {
                    Sign foundSign = (Sign)sl.getBlock().getState();
                    if (plugin.accountExists(sign.getValue())) {
                        String[] lines = {"[BankAccount]", sign.getValue(), plugin.Method.format(balance), ""};
                        plugin.signupdater.AddSignUpdate(UpdaterPriority.NORMAL, foundSign, lines);
                    } else {
                        String[] lines = {"[BankAccount]", sign.getValue(), "Account closed", ""};
                        plugin.signupdater.AddSignUpdate(UpdaterPriority.NORMAL, foundSign, lines);
                    }
                } else {
                    remove(sl);
                    plugin.console.warning("Sign in world " + sl.getWorld().getName() + " at " + sl.locOutput(", ",false) + " can't be found - removed");
                }
            }
        }
    }
}