package dk.earthgame.TAT.BankAccount.System;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import dk.earthgame.TAT.BankAccount.BankAccount;

/**
 * Manager for UserSave
 * @author TAT
 * @since 0.6
 */
public class UserSaves {
    private BankAccount plugin;
	private HashMap<String,UserSave> UserSaves = new HashMap<String,UserSave>();
    
	public UserSaves(BankAccount instantiate) {
		plugin = instantiate;
	}
	
	/**
     * Get the saved data of a player
     * 
     * @param player The username of the player
     * @since 0.5
     * @return UserSaves - the saved data
     */
    public UserSave getSaved(String player) {
        if (UserSaves.containsKey(player)) {
            return UserSaves.get(player);
        }
        
        UserSave save = new UserSave(this);
        UserSaves.put(player, save);
        save();
        return save;
    }
    
    /**
     * Load signs from dat file
     * 
     * @since 0.6
     */
    public void load() {
        File signFile = new File(plugin.getDataFolder(), "UserSaves.dat");
        if (signFile.exists()) {
            try {
                FileReader fr = new FileReader(signFile);
                BufferedReader reader = new BufferedReader(fr);
                String s;
                int line = 0;
                UserSaves.clear();
                while ((s = reader.readLine()) != null) {
                    line++;
                    String[] args = s.split(",");
                    if (args.length == 2) {
                    	UserSaves.put(args[0],new UserSave(this, Double.parseDouble(args[1])));
                    } else {
                        plugin.console.warning("UserSaves.dat contains errors on line " + line);
                    }
                }
                fr.close();
            } catch (Exception e) {
                plugin.console.warning("Error loading UserSaves");
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Save saves to dat file
     * 
     * @since 0.6
     */
    public void save() {
        try {
            File signFile = new File(plugin.getDataFolder(), "UserSaves.dat");
            FileWriter writer = new FileWriter(signFile);
            for (Map.Entry<String, UserSave> usersave: UserSaves.entrySet()) {
                String p = usersave.getKey();
                UserSave s = usersave.getValue();
                writer.write(p + "," + s.getBounty() + "\n");
            }
            writer.flush();
            writer.close();
        } catch (Exception e) {
            plugin.console.warning("Can't save UserSaves");
            e.printStackTrace();
        }
    }
}
