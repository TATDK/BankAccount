package dk.earthgame.TAT.BankAccount.Features;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;

import dk.earthgame.TAT.BankAccount.BankAccount;

/**
 * Banks, that can contain accounts
 * @author TAT
 * @since 0.6
 */
public class Bank {
    private BankAccount plugin;
    private String name;
    private int id;
    private int area;
    private List<String> knownBankers = new ArrayList<String>();
    
    public Bank(BankAccount instantiate, String bankname) {
        plugin = instantiate;
        name = bankname;
        if (bankname.equalsIgnoreCase("Global"))
            id = 0;
        else {
            try {
                id = plugin.SQLWorker.getInt("id", plugin.settings.SQL_banks_table, "`name` = " + name);
                area = plugin.SQLWorker.getInt("area", plugin.settings.SQL_banks_table, "`name` = " + name);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    public Bank(BankAccount instantiate, int bankid) {
        plugin = instantiate;
        id = bankid;
        if (id == 0)
            name = "Global";
        else {
            try {
                name = plugin.SQLWorker.getString("name", plugin.settings.SQL_banks_table, "`id` = " + id);
                area = plugin.SQLWorker.getInt("area", plugin.settings.SQL_banks_table, "`name` = " + name);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Get name of bank
     * @since 0.6
     * @return Name of bank
     */
    public String getName() { return name; }
    
    /**
     * Get id of bank
     * @since 0.6
     * @return Id of Bank
     */
    public int getId() { return id; }
    
    /**
     * Add amount money to all accounts inside bank
     * @param amount Amount money
     * @since 0.6
     * @return 
     */
    public boolean addAll(double amount) {
        try {
            plugin.SQLWorker.executeUpdate("UPDATE `" + plugin.settings.SQL_account_table + "` SET `amount` = `amount`+" + amount + " WHERE `bank` = " + id);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Subtract amount money to all accounts inside bank
     * @param amount Amount money
     * @since 0.6
     * @return true on success; else false
     */
    public boolean subtractAll(double amount) {
        try {
            plugin.SQLWorker.executeUpdate("UPDATE `" + plugin.settings.SQL_account_table + "` SET `amount` = `amount`-" + amount + " WHERE `bank` = " + id);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Add banker to bank
     * @param player Player object of new banker
     * @since 0.6
     * @return true on success; else false
     */
    public boolean addBanker(Player player) { return addBanker(player.getName()); }
    
    /**
     * Add banker to bank
     * @param player Name of new banker
     * @since 0.6
     * @return true on success; else false
     */
    public boolean addBanker(String player) {
        try {
            String newBankers = player;
            String[] bankers = plugin.SQLWorker.getString("bankers", plugin.settings.SQL_banks_table, "`cleanname` = '" + name.toLowerCase() + "'").split(";");
            for (String b : bankers) {
                newBankers += ";" + b;
            }
            plugin.SQLWorker.executeUpdate("UPDATE `" + plugin.settings.SQL_banks_table + "` SET `bankers` = '" + newBankers + "' WHERE `cleanname` = '" + name.toLowerCase() + "'");
            knownBankers.add(player);
            return true;
        } catch(SQLException e) {
            if (!e.getMessage().equalsIgnoreCase(null))
                plugin.console.warning("Error #xx-3: " + e.getMessage());
            else
                plugin.console.warning("Error #xx-2: " + e.getErrorCode() + " - " + e.getSQLState());
        } catch (Exception e) {
            plugin.console.warning("Error #xx-1: " + e.toString());
        }
        return false;
    }
    
    /**
     * Remove banker from bank
     * @param player Player object of banker
     * @since 0.6
     * @return true on success; else false
     */
    public boolean removeBanker(Player player) { return removeBanker(player.getName()); }
    
    /**
     * Remove banker from bank
     * @param player Name of banker
     * @since 0.6
     * @return true on success; else false
     */
    public boolean removeBanker(String player) {
        try {
            String newBankers = player;
            String[] bankers = plugin.SQLWorker.getString("bankers", plugin.settings.SQL_banks_table, "`cleanname` = '" + name.toLowerCase() + "'").split(";");
            for (String b : bankers) {
                if (!b.equalsIgnoreCase(player)) {
                    if (!newBankers.equalsIgnoreCase(""))
                        newBankers += ";";
                    newBankers += b;
                }
            }
            plugin.SQLWorker.executeUpdate("UPDATE `" + plugin.settings.SQL_banks_table + "` SET `bankers` = '" + newBankers + "' WHERE `cleanname` = '" + name.toLowerCase() + "'");
        	knownBankers.remove(player);
            return true;
        } catch(SQLException e) {
            if (!e.getMessage().equalsIgnoreCase(null))
                plugin.console.warning("Error #xx-3: " + e.getMessage());
            else
                plugin.console.warning("Error #xx-2: " + e.getErrorCode() + " - " + e.getSQLState());
        } catch (Exception e) {
            plugin.console.warning("Error #xx-1: " + e.toString());
        }
        return false;
    }
    
    /**
     * Change interest for bank
     * @param online_interest Interest percentage for online accounts
     * @param offline_interest Interest percentage for offline accounts
     * @param online_amount Percentage of owners and users that needs to be online, to definite an account as online
     * @return true on success; else false
     */
    public boolean changeInterest(double online_interest,double offline_interest,int online_amount) {
    	if (!plugin.settings.multiInterests)
    		return false;
    	try {
    		plugin.SQLWorker.executeUpdate("UPDATE `" + plugin.settings.SQL_banks_table + "` SET `online-interest` = '" + online_interest + "', `offline-interest` = '" + offline_interest + "', `online-amount` = '" + online_amount + "' WHERE `cleanname` = '" + name.toLowerCase() + "'");
            return true;
    	} catch(SQLException e) {
            if (!e.getMessage().equalsIgnoreCase(null))
                plugin.console.warning("Error #xx-3: " + e.getMessage());
            else
                plugin.console.warning("Error #xx-2: " + e.getErrorCode() + " - " + e.getSQLState());
        } catch (Exception e) {
            plugin.console.warning("Error #xx-1: " + e.toString());
        }
        return false;
    }

    /**
     * Is the player a banker for this bank?
     * @param player Player object of the player
     * @since 0.6
     * @return true, if the player is a banker; else false
     */
	public boolean isBanker(Player player) { return isBanker(player.getName());	}

    /**
     * Is the player a banker for this bank?
     * @param player Username of the player
     * @since 0.6
     * @return true, if the player is a banker; else false
     */
	public boolean isBanker(String player) {
		if (knownBankers.contains(player))
			return true;
		else {
			try {
				String[] bankers = plugin.SQLWorker.getString("`bankers`", plugin.settings.SQL_banks_table, "`id` = 0").split(";");
				for (String b : bankers) {
					if (b.equalsIgnoreCase(player))
						return true;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public int getArea() {
		return area;
	}
	
	public void setArea(String areaname) {
		try {
			this.area = plugin.SQLWorker.getInt("`id`", plugin.settings.SQL_area_table, "`name` = '" + areaname + "'");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void setArea(int area) {
		this.area = area;
	}
    
    /**
     * Remove bank and move all accounts to global
     * @since 0.6
     */
    public void remove() { remove("Global"); }
    
    /**
     * Remove bank and move all accounts to another bank
     * If new bank doesn't exists, all accounts are moved to Global
     * @param newBank New bankname
     * @since 0.6
     */
    public void remove(String newBank) {
        int BankID;
        if (newBank.equalsIgnoreCase("Global"))
            BankID = 0;
        else {
            if (plugin.getBank(newBank) != null)
                BankID = plugin.getBank(newBank).id;
            else
                return;
        }
            
        try {
            plugin.SQLWorker.executeDelete("DELETE FROM `" + plugin.settings.SQL_banks_table + "` WHERE `id` = " + id);
            plugin.settings.updateStmt.execute("UPDATE `" + plugin.settings.SQL_account_table + "` SET `bank` = '" + BankID + "' WHERE `bank` = '" + id + "'");
            plugin.knownBanks.remove(newBank);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}