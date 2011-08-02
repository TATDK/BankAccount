package dk.earthgame.TAT.BankAccount.Features;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import dk.earthgame.TAT.BankAccount.BankAccount;

public class Bank {
	private BankAccount plugin;
	private String name;
	private int id;
	
	public Bank(BankAccount instantiate, String bankname) {
		plugin = instantiate;
		name = bankname;
		if (bankname.equalsIgnoreCase("Global")) {
			id = 0;
		} else {
			try {
				id = plugin.SQLWorker.getInt("id", plugin.settings.SQL_banks_table, "`name` = " + name);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public Bank(BankAccount instantiate, int bankid) {
		plugin = instantiate;
		id = bankid;
		if (id == 0) {
			name = "Global";
		} else {
			try {
				name = plugin.SQLWorker.getString("name", plugin.settings.SQL_banks_table, "`id` = " + id);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Get name of bank
	 * @return Name of bank
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Get id of bank
	 * @return Id of Bank
	 */
	public int getId() {
		return id;
	}
	
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
	 * @return 
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
	
	public void remove() { remove("Global"); }
	
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
			if (plugin.settings.UseMySQL) {
                //MySQL
				ResultSet rs = plugin.settings.updateStmt.executeQuery("SELECT `bank` FROM `" + plugin.settings.SQL_account_table + "` WHERE `bank` = " + id);
	            while (rs.next()) {
	                rs.updateInt("bank", BankID);
	                rs.updateRow();
	            }
	            rs.close();
			} else {
				//SQLite
				PreparedStatement prep = plugin.settings.con.prepareStatement("UPDATE `" + plugin.settings.SQL_account_table + "` SET `bank` = ? WHERE `bank` = ?");
                ResultSet rs = plugin.settings.updateStmt.executeQuery("SELECT `bank` FROM `" + plugin.settings.SQL_account_table + "` WHERE `bank` = " + id);
                while (rs.next()) {
                    prep.setInt(1, BankID);
                    prep.setInt(2, id);
                    prep.addBatch();
                }
                rs.close();
                plugin.settings.con.setAutoCommit(false);
                prep.executeBatch();
                plugin.settings.con.commit();
                plugin.settings.con.setAutoCommit(true);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}