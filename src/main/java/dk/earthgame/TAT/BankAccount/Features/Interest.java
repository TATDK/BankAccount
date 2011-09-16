package dk.earthgame.TAT.BankAccount.Features;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import dk.earthgame.TAT.BankAccount.BankAccount;

public class Interest {
    private BankAccount plugin;
    private int jobID;

    public Interest(BankAccount instantiate) {
        plugin = instantiate;
    }

    public boolean isOnline(String[] owners,String[] users,int neededOnline) {
        int online = 0;
        //Owners
        for (String o : owners) {
            if (plugin.getServer().getPlayer(o) != null) {
                if (plugin.getServer().getPlayer(o).isOnline()) {
                    online++;
                    if (online > neededOnline) {
                        return true;
                    }
                }
            }
        }

        //Users
        for (String u : users) {
            if (plugin.getServer().getPlayer(u) != null) {
                if (plugin.getServer().getPlayer(u).isOnline()) {
                    online++;
                    if (online > neededOnline) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void startupInterest() {
        if (jobID > 0)
            return;

        jobID = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() {
                if (plugin.settings.debug_Interest)
                    plugin.console.info("Running interest system");

                double totalGiven = 0.00;
                try {
                    if (plugin.settings.useMySQL) {
                        //MySQL
                        ResultSet accounts = plugin.settings.stmt.executeQuery("SELECT `id`, `amount`, `owners`, `users` FROM `" + plugin.settings.SQL_account_table + "`");
                        while (accounts.next()) {
                            double accountbalance = accounts.getDouble("amount");
                            String[] owners = accounts.getString("owners").split(";");
                            String[] users = accounts.getString("users").split(";");
                            int neededOnline = (owners.length+users.length)/plugin.settings.interestNeededOnline;

                            //Is account online
                            boolean accountOnline = false;
                            if (plugin.settings.interestOnlineAmount == plugin.settings.interestOfflineAmount) {
                                accountOnline = true;
                            } else {
                            	accountOnline = isOnline(owners, users, neededOnline);
                            }

                            double interest;
                            if (accountOnline)
                                interest = plugin.settings.interestOnlineAmount;
                            else
                                interest = plugin.settings.interestOfflineAmount;

                            if (plugin.settings.maxAmount > 0 && ((accountbalance *= 1+(interest/100)) > plugin.settings.maxAmount)) {
                                totalGiven += (plugin.settings.maxAmount-accountbalance);
                                accountbalance = plugin.settings.maxAmount;
                            } else {
                                totalGiven += accountbalance*(interest/100);
                                accountbalance *= 1+(interest/100);
                            }
                            accounts.updateDouble("amount", accountbalance);
                            accounts.updateRow();
                        }
                        accounts.close();
                    } else {
                        //SQLite
                        PreparedStatement prep = plugin.settings.con.prepareStatement("UPDATE `" + plugin.settings.SQL_account_table + "` SET `amount` = ? WHERE `cleanname` = ?");
                        ResultSet accounts = plugin.settings.updateStmt.executeQuery("SELECT `cleanname`, `amount`, `owners`, `users` FROM `" + plugin.settings.SQL_account_table + "`");
                        while (accounts.next()) {
                            String accountname = accounts.getString("cleanname");
                            double accountbalance = accounts.getDouble("amount");
                            String[] owners = accounts.getString("owners").split(";");
                            String[] users = accounts.getString("users").split(";");
                            int neededOnline = (owners.length+users.length)/plugin.settings.interestNeededOnline;

                            //Is account online
                            boolean accountOnline = false;
                            if (plugin.settings.interestOnlineAmount == plugin.settings.interestOfflineAmount) {
                                accountOnline = true;
                            } else {
                                accountOnline = isOnline(owners, users, neededOnline);
                            }

                            double interest;
                            if (accountOnline)
                                interest = plugin.settings.interestOnlineAmount;
                            else
                                interest = plugin.settings.interestOfflineAmount;

                            if (plugin.settings.maxAmount > 0 && ((accountbalance *= 1+(interest/100)) > plugin.settings.maxAmount)) {
                                totalGiven += (plugin.settings.maxAmount-accountbalance);
                                accountbalance = plugin.settings.maxAmount;
                            } else {
                                totalGiven += accountbalance*(interest/100);
                                accountbalance *= 1+(interest/100);
                            }

                            prep.setDouble(1, accountbalance);
                            prep.setString(2, accountname);
                            prep.addBatch();
                        }
                        accounts.close();
                        plugin.settings.con.setAutoCommit(false);
                        prep.executeBatch();
                        plugin.settings.con.commit();
                        plugin.settings.con.setAutoCommit(true);
                    }
                } catch (SQLException e) {
                    plugin.console.warning("Couldn't execute interest");
                    plugin.console.info(e.toString());
                }
                if (plugin.settings.debug_Interest)
                    plugin.console.info("Total given " + plugin.Method.format(totalGiven) + " in interest");
            }
        }, plugin.settings.interestTime*20*60, plugin.settings.interestTime*20*60);
        plugin.console.info("Running interest every " + plugin.settings.interestTime + " minutes by " + plugin.settings.interestOnlineAmount + "%");
    }
    
    public void shutdownInterest() {
        if (jobID > 0) {
            plugin.getServer().getScheduler().cancelTask(jobID);
            jobID = 0;
        }
    }
}