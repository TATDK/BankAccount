package dk.earthgame.TAT.BankAccount.System;

import java.io.File;
import java.sql.SQLException;

import dk.earthgame.TAT.BankAccount.BankAccount;

/**
 * Upgrade functions of BankAccount
 * @author TAT
 */
public class Upgrade {
	/**
	 * SHOULD ONLY BE USED BY BANKACCOUNT
	 * @since 0.5
	 */
	public Upgrade(BankAccount plugin,boolean UseMySQL) {
		//Upgrade 0.3c
		File Upgrade03c = new File(plugin.getDataFolder(), "SQLUpgrade03c");
		if (Upgrade03c.exists()) {
			runUpgrade03c(plugin,UseMySQL,Upgrade03c);
		}
		
		//Upgrade 0.5
		File Upgrade05 = new File(plugin.getDataFolder(), "SQLUpgrade05");
		if (Upgrade05.exists()) {
			runUpgrade05(plugin, UseMySQL, Upgrade05);
		}
	}
	
	//UPGRADES
	private void runUpgrade03c(BankAccount plugin, boolean UseMySQL,File file) {
		try {
			if (UseMySQL) {
				String query = "ALTER TABLE `" + plugin.settings.SQL_account_table + "` CHANGE  `amount`  `amount` DOUBLE( 255, 2 ) NOT NULL DEFAULT  '0.00'";
				plugin.settings.stmt.execute(query);
				plugin.console.info("Tables upgraded to v.0.3c");
				if (file.delete()) {
					plugin.console.info("SQLUpgrade03c deleted");
				} else {
					plugin.console.warning("SQLUpgrade03c could not be deleted, please remove it yourself");
				}
			} else {
				plugin.console.info("SQLUpgrade03c is not for SQLite");
				if (file != null) {
					if (file.delete()) {
						plugin.console.info("SQLUpgrade03c deleted");
					} else {
						plugin.console.warning("SQLUpgrade03c could not be deleted, please remove it yourself");
					}
				}
			}
		} catch (SQLException e4) {
			plugin.console.warning("Could not upgrade tables to v.0.3c");
			plugin.console.warning(e4.toString());
		}
	}
	private void runUpgrade05(BankAccount plugin, boolean UseMySQL,File file) {
		try {
			if (UseMySQL) {
				//MySQL
				String query = "ALTER TABLE `" + plugin.settings.SQL_account_table + "` CHANGE  `players`  `owners` LONGTEXT NOT NULL";
				plugin.settings.stmt.execute(query);
				query = "ALTER TABLE  `" + plugin.settings.SQL_account_table + "` ADD  `users` LONGTEXT NOT NULL AFTER  `owners`";
				plugin.settings.stmt.execute(query);
				plugin.console.info("MySQL Tables upgraded to v.0.5");
			} else {
				//SQLite
				String query = "ALTER TABLE `" + plugin.settings.SQL_account_table + "` ADD  `owners` LONGTEXT NOT NULL DEFAULT ''";
				plugin.settings.stmt.execute(query);
				query = "ALTER TABLE  `" + plugin.settings.SQL_account_table + "` ADD  `users` LONGTEXT NOT NULL DEFAULT ''";
				plugin.settings.stmt.execute(query);
				plugin.console.info("SQLite Tables upgraded to v.0.5");
			}
			if (file != null) {
				if (file.delete()) {
					plugin.console.info("SQLUpgrade05 deleted");
				} else {
					plugin.console.warning("SQLUpgrade05 could not be deleted, please remove it yourself");
				}
			}
		} catch (SQLException e) {
			plugin.console.warning("Could not upgrade tables to v.0.5");
			plugin.console.warning(e.toString());
		}
	}
}