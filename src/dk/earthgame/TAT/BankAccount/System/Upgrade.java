package dk.earthgame.TAT.BankAccount.System;

import java.io.File;
import java.sql.SQLException;

import dk.earthgame.TAT.BankAccount.BankAccount;

public class Upgrade {
	/**
	 * SHOULD ONLY BE USED BY BANKACCOUNT
	 * @since 0.5
	 */
	public Upgrade(BankAccount plugin,boolean UseMySQL) {
		//Upgrade 0.3c
		File Upgrade03c = new File(plugin.getDataFolder(), "SQLUpgrade03c");
		if (Upgrade03c.exists()) {
			try {
				if (UseMySQL) {
					String query = "ALTER TABLE `" + plugin.SQL_account_table + "` CHANGE  `amount`  `amount` DOUBLE( 255, 2 ) NOT NULL DEFAULT  '0.00'";
					plugin.stmt.execute(query);
					plugin.consoleInfo("Tables upgraded to v.0.3c");
					if (Upgrade03c.delete()) {
						plugin.consoleInfo("SQLUpgrade03c deleted");
					} else {
						plugin.consoleWarning("SQLUpgrade03c could not be deleted, please remove it yourself");
					}
				} else {
					plugin.consoleInfo("SQLUpgrade03c is not for SQLite");
					if (Upgrade03c.delete()) {
						plugin.consoleInfo("SQLUpgrade03c deleted");
					} else {
						plugin.consoleWarning("SQLUpgrade03c could not be deleted, please remove it yourself");
					}
				}
			} catch (SQLException e4) {
				plugin.consoleWarning("Could not upgrade tables to v.0.3c");
				plugin.consoleWarning(e4.toString());
			}
		}
		
		//Upgrade 0.5
		File Upgrade05 = new File(plugin.getDataFolder(), "SQLUpgrade05");
		if (Upgrade05.exists()) {
			try {
				if (UseMySQL) {
					String query = "ALTER TABLE `" + plugin.SQL_account_table + "` CHANGE  `players`  `owners` LONGTEXT NOT NULL";
					plugin.stmt.execute(query);
					plugin.consoleInfo("Tables upgraded to v.0.5");
				} else {
					//TODO: Setup SQLite Update
					plugin.consoleInfo("SQLUpgrade05 is not ready for SQLite");
				}
				if (Upgrade05.delete()) {
					plugin.consoleInfo("SQLUpgrade05 deleted");
				} else {
					plugin.consoleWarning("SQLUpgrade05 could not be deleted, please remove it yourself");
				}
			} catch (SQLException e) {
				plugin.consoleWarning("Could not upgrade tables to v.0.5");
				plugin.consoleWarning(e.toString());
			}
		}
	}
}