package dk.earthgame.TAT.BankAccount.System;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
        
        //Upgrade 0.5
        File Upgrade051 = new File(plugin.getDataFolder(), "SQLUpgrade051");
        if (Upgrade051.exists()) {
            runUpgrade051(plugin, UseMySQL, Upgrade051);
        }
    }
    
    //UPGRADES
//0.3c
    private void runUpgrade03c(BankAccount plugin, boolean UseMySQL, File file) {
        try {
            if (UseMySQL) {
                plugin.settings.stmt.execute("ALTER TABLE `" + plugin.settings.SQL_account_table + "` CHANGE  `amount`  `amount` DOUBLE( 255, 2 ) NOT NULL DEFAULT  '0.00'");
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
//0.5
    private void runUpgrade05(BankAccount plugin, boolean UseMySQL, File file) {
        try {
            if (UseMySQL) {
                //MySQL
                plugin.settings.stmt.execute("ALTER TABLE `" + plugin.settings.SQL_account_table + "` CHANGE  `players`  `owners` LONGTEXT NOT NULL");
                plugin.settings.stmt.execute("ALTER TABLE `" + plugin.settings.SQL_account_table + "` ADD  `users` LONGTEXT NOT NULL AFTER  `owners`");
                plugin.console.info("MySQL Tables upgraded to v.0.5");
            } else {
                //SQLite
                plugin.settings.stmt.execute("ALTER TABLE `" + plugin.settings.SQL_account_table + "` ADD  `owners` LONGTEXT NOT NULL DEFAULT ''");
                plugin.settings.stmt.execute("ALTER TABLE `" + plugin.settings.SQL_account_table + "` ADD  `users` LONGTEXT NOT NULL DEFAULT ''");
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
//0.5.1
    private void runUpgrade051(BankAccount plugin, boolean UseMySQL, File file) {
        try {
            
            if (UseMySQL) {
                //MySQL
                plugin.settings.stmt.execute("ALTER TABLE  `" + plugin.settings.SQL_account_table + "` ADD  `cleanname` LONGTEXT NOT NULL AFTER  `accountname`");
                ResultSet rs = plugin.settings.updateStmt.executeQuery("SELECT `accountname` FROM `" + plugin.settings.SQL_account_table + "`");
                while (rs.next()) {
                    String accountname = rs.getString("accountname");
                    rs.updateString("cleanname", accountname.toLowerCase());
                    rs.updateRow();
                }
                rs.close();
                plugin.console.info("MySQL Tables upgraded to v.0.5.1");
            } else {
                //SQLite
                plugin.settings.stmt.execute("ALTER TABLE  `" + plugin.settings.SQL_account_table + "` ADD  `cleanname` LONGTEXT NOT NULL DEFAULT ''");
                PreparedStatement prep = plugin.settings.con.prepareStatement("UPDATE `" + plugin.settings.SQL_account_table + "` SET `cleanname` = ? WHERE `accountname` = ?");
                ResultSet rs = plugin.settings.updateStmt.executeQuery("SELECT `accountname` FROM `" + plugin.settings.SQL_account_table + "`");
                while (rs.next()) {
                    String accountname = rs.getString("accountname");
                    prep.setString(1, accountname.toLowerCase());
                    prep.setString(2, accountname);
                    prep.addBatch();
                }
                rs.close();
                plugin.settings.con.setAutoCommit(false);
                prep.executeBatch();
                plugin.settings.con.commit();
                plugin.settings.con.setAutoCommit(true);
                plugin.console.info("SQLite Tables upgraded to v.0.5.1");
            }
            if (file != null) {
                if (file.delete()) {
                    plugin.console.info("SQLUpgrade051 deleted");
                } else {
                    plugin.console.warning("SQLUpgrade051 could not be deleted, please remove it yourself");
                }
            }
        } catch (SQLException e) {
            plugin.console.warning("Could not upgrade tables to v.0.5.1");
            plugin.console.warning(e.toString());
        }
    }
}