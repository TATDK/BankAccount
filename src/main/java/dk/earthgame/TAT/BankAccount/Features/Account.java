package dk.earthgame.TAT.BankAccount.Features;

import java.sql.SQLException;

import dk.earthgame.TAT.BankAccount.BankAccount;
import dk.earthgame.TAT.BankAccount.Enum.ATMTypes;
import dk.earthgame.TAT.BankAccount.Enum.FeeModes;
import dk.earthgame.nijikokun.register.payment.Method.MethodAccount;

/**
 * Bank Account
 * @since 0.6
 * @author TAT
 */
public class Account {
    private BankAccount plugin;
    private String name;
    private Bank bank;
    
    public Account(BankAccount instantiate, String accountname) {
        plugin = instantiate;
        this.name = accountname;
    }
    
    /**
     * Get name of account
     * @since 0.6
     * @return Account name
     */
    public String getName() { return name; }
    
    /**
     * Get bank of account
     * @since 0.6
     * @return Bank object
     */
    public Bank getBank() {
        if (bank == null) {
            if (plugin.settings.areas) {
                try {
                    bank = new Bank(plugin,plugin.SQLWorker.getInt("bank", plugin.settings.SQL_account_table, "`accountname` = " + name));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                bank = new Bank(plugin,"Global");
            }
        }
        return bank;
    }
    
    /**
     * Add user to account
     * 
     * @param player Username of the player
     * @since 0.5
     * @see #addOwner(String player)
     * @return If the user is successfully added
     */
    public boolean addUser(String player) {
        try {
            String newUsers = player;
            String[] users = plugin.SQLWorker.getString("users", plugin.settings.SQL_account_table, "`cleanname` = '" + name.toLowerCase() + "'").split(";");
            for (String u : users) {
                newUsers += ";" + u;
            }
            plugin.SQLWorker.executeUpdate("UPDATE `" + plugin.settings.SQL_account_table + "` SET `users` = '" + newUsers + "' WHERE `cleanname` = '" + name.toLowerCase() + "'");
            return true;
        } catch(SQLException e) {
            if (!e.getMessage().equalsIgnoreCase(null))
                plugin.console.warning("Error #04-3: " + e.getMessage());
            else
                plugin.console.warning("Error #04-2: " + e.getErrorCode() + " - " + e.getSQLState());
        } catch (Exception e) {
            plugin.console.warning("Error #04-1: " + e.toString());
        }
        return false;
    }
    
    /**
     * Remove an user from an account
     * 
     * @param player Username of the player
     * @since 0.5
     * @see #removeOwner(String player)
     * @return If the user is successfully removed
     */
    public boolean removeUser(String player) {
        try {
            String newUsers = "";
            String[] users = plugin.SQLWorker.getString("users", plugin.settings.SQL_account_table, "`cleanname` = '" + name.toLowerCase() + "'").split(";");
            for (String u : users) {
                if (!u.equalsIgnoreCase(player)) {
                    if (!newUsers.equalsIgnoreCase(""))
                        newUsers += ";";
                    newUsers += u;    
                }
            }
            plugin.SQLWorker.executeUpdate("UPDATE `" + plugin.settings.SQL_account_table + "` SET `users` = '" + newUsers + "' WHERE `cleanname` = '" + name.toLowerCase() + "'");
            return true;
        } catch(SQLException e) {
            if (!e.getMessage().equalsIgnoreCase(null))
                plugin.console.warning("Error #05-3: " + e.getMessage());
            else
                plugin.console.warning("Error #05-2: " + e.getErrorCode() + " - " + e.getSQLState());
        } catch (Exception e) {
            plugin.console.warning("Error #05-1: " + e.toString());
        }
        return false;
    }
    
    /**
     * Add owner to account
     * 
     * @param player Username of the player
     * @since 0.5
     * @see #addUser(String player)
     * @return If the owner is successfully added
     */
    public boolean addOwner(String player) {
        try {
            String newOwners = player;
            String[] owners = plugin.SQLWorker.getString("owners", plugin.settings.SQL_account_table, "`cleanname` = '" + name.toLowerCase() + "'").split(";");
            for (String o : owners) {
                newOwners += ";" + o;
            }
            plugin.SQLWorker.executeUpdate("UPDATE `" + plugin.settings.SQL_account_table + "` SET `owners` = '" + newOwners + "' WHERE `cleanname` = '" + name.toLowerCase() + "'");
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
     * Remove an owner from an account
     * 
     * @param player Username of the player
     * @since 0.5
     * @see #removeUser(String player)
     * @return If the owner is successfully removed
     */
    public boolean removeOwner(String player) {
        try {
            String newOwners = "";
            String[] owners = plugin.SQLWorker.getString("owners", plugin.settings.SQL_account_table, "`cleanname` = '" + name.toLowerCase() + "'").split(";");
            for (String o : owners) {
                if (!o.equalsIgnoreCase(player)) {
                    if (!newOwners.equalsIgnoreCase("")) {
                        newOwners += ";";
                    }
                    newOwners += o;    
                }
            }
            plugin.SQLWorker.executeUpdate("UPDATE `" + plugin.settings.SQL_account_table + "` SET `owners` = '" + newOwners + "' WHERE `cleanname` = '" + name.toLowerCase() + "'");
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
     * Set the password for an account
     * 
     * @param password The new password (Must be encrypted!)
     * @since 0.5
     * @return If the password is successfully set
     */
    public boolean setPassword(String password) {
        try {
            plugin.SQLWorker.executeUpdate("UPDATE `" + plugin.settings.SQL_account_table + "` SET `password` = '" + password + "' WHERE `cleanname` = '" + name.toLowerCase() + "'");
            return true;
        } catch(SQLException e) {
            if (!e.getMessage().equalsIgnoreCase(null))
                plugin.console.warning("Error #06-3: " + e.getMessage());
            else
                plugin.console.warning("Error #06-2: " + e.getErrorCode() + " - " + e.getSQLState());
        } catch (Exception e) {
            plugin.console.warning("Error #06-1: " + e.toString());
        }
        return false;
    }

    /**
     * Send action to the ATM
     * 
     * @param player Username of the player
     * @param type Type of action
     * @param amount Amount money
     * @param password Password
     * @since 0.5
     * @return If the action is run successfully
     */
    public boolean ATM(String player,ATMTypes type,Double amount,String password) {
        try {
            double accountBalance = getBalance();
            MethodAccount economyAccount = plugin.Method.getAccount(player);
            double playerBalance = economyAccount.balance();
            if (type == ATMTypes.DEPOSIT) {
                if (plugin.settings.maxAmount > 0 && (accountBalance + amount) > plugin.settings.maxAmount) {
                    //Cancel the transaction
                    return false;
                } else if (economyAccount.hasEnough(amount)) {
                    if (plugin.settings.DepositFee.getMode() != FeeModes.NONE) {
                        amount = plugin.settings.DepositFee.PayFee(amount, playerBalance, player);
                        if (amount == 0)
                            return true;
                    }
                    add(amount);
                    economyAccount.subtract(amount);
                    plugin.BalanceSign.update(name);
                    return true;
                } else {
                    return false;
                }
            } else if (type == ATMTypes.WITHDRAW) {
                if (plugin.PasswordSystem.passwordCheck(name, password)) {
                    if ((accountBalance - amount) >= 0) {
                        if (plugin.settings.WithdrawFee.getMode() != FeeModes.NONE) {
                            amount = plugin.settings.WithdrawFee.PayFee(amount,accountBalance,name);
                            if (amount == 0)
                                return true;
                        }
                        subtract(amount);
                        economyAccount.add(amount);
                        plugin.BalanceSign.update(name);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else if (type == ATMTypes.TRANSFER) {
                if (plugin.PasswordSystem.passwordCheck(name, password)) {
                    Account reciever = plugin.getAccount(player);
                    //Player = receiver account
                    double receiver_account = reciever.getBalance();
                    if (plugin.settings.maxAmount > 0 && (receiver_account+amount) > plugin.settings.maxAmount) {
                        //Cancel the transaction
                        return false;
                    } else if ((accountBalance - amount) >= 0) {
                        if (plugin.settings.TransferFee.getMode() != FeeModes.NONE) {
                            amount = plugin.settings.TransferFee.PayFee(amount,accountBalance,name);
                            if (amount == 0)
                                return true;
                        }
                        subtract(amount);
                        reciever.add(amount);
                        plugin.BalanceSign.update(name);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        } catch(Exception e) {
            plugin.console.warning("Error #07-1: " + e.toString());
        }
        return false;
    }
    
    /**
     * Close an account
     * 
     * @param player Username of the player
     * @param password Password
     * @since 0.5
     * @return If the account is successfully closed
     */
    public boolean close(String player,String password) {
        if (plugin.PasswordSystem.passwordCheck(name, password)) {
            try {
                MethodAccount economyAccount = plugin.Method.getAccount(player);
                double accountBalance = getBalance();
                if (plugin.settings.ClosingFee.getMode() != FeeModes.NONE && accountBalance > 0) {
                    if (plugin.settings.ClosingFee.Fee_Percentage == 100) {
                        accountBalance = 0;
                    } else {
                        accountBalance = plugin.settings.ClosingFee.PayFee(accountBalance,name);
                        if (accountBalance == 0)
                            return true;
                    }
                }
                plugin.SQLWorker.executeDelete("DELETE FROM `" + plugin.settings.SQL_account_table + "` WHERE `cleanname` = '" + name.toLowerCase() + "'");
                economyAccount.add(accountBalance);
                plugin.BalanceSign.update(name);
                plugin.knownAccounts.remove(name);
                return true;
            } catch(SQLException e) {
                if (!e.getMessage().equalsIgnoreCase(null))
                    plugin.console.warning("Error #08-3: " + e.getMessage());
                else
                    plugin.console.warning("Error #08-2: " + e.getErrorCode() + " - " + e.getSQLState());
            } catch (Exception e) {
                plugin.console.warning("Error #08-1: " + e.toString());
            }
        } else {
            return false;
        }
        return false;
    }
    
    /**
     * Get the users of an account
     * 
     * @since 0.5
     * @return String of users (seperated by comma and space(, ))
     */
    public String getUsers() {
        try {
            String output = "";
            String[] users = plugin.SQLWorker.getString("users", plugin.settings.SQL_account_table, "`cleanname` = '" + name.toLowerCase() + "'").split(";");
            for (String u : users) {
                if (!output.equalsIgnoreCase("")) {
                    output += ", ";
                }
                output += u;    
            }
            return output;
        } catch (SQLException e1) {
            if (!e1.getMessage().equalsIgnoreCase(null))
                plugin.console.warning("Error #09-3: " + e1.getMessage());
            else
                plugin.console.warning("Error #09-2: " + e1.getErrorCode() + " - " + e1.getSQLState());
        } catch (Exception e) {
            plugin.console.warning("Error #09-1: " + e.toString());
        }
        return null;
    }
    
    /**
     * Get the owners of an account
     * 
     * @since 0.5
     * @return String of owners (seperated by comma and space(, ))
     */
    public String getOwners() {
        try {
            String output = "";
            String[] owners = plugin.SQLWorker.getString("owners", plugin.settings.SQL_account_table, "`cleanname` = '" + name.toLowerCase() + "'").split(";");
            for (String o : owners) {
                if (!output.equalsIgnoreCase("")) {
                    output += ", ";
                }
                output += o;    
            }
            return output;
        } catch (SQLException e1) {
            if (!e1.getMessage().equalsIgnoreCase(null))
                plugin.console.warning("Error #09-3: " + e1.getMessage());
            else
                plugin.console.warning("Error #09-2: " + e1.getErrorCode() + " - " + e1.getSQLState());
        } catch (Exception e) {
            plugin.console.warning("Error #09-1: " + e.toString());
        }
        return null;
    }
    
    /**
     * Get balance
     * 
     * @since 0.5
     * @return double - Amount of money on account
     */
    public double getBalance() {
        try {
            return plugin.SQLWorker.getDouble("amount", plugin.settings.SQL_account_table, "`cleanname` = '" + name.toLowerCase() +"'");
        } catch (SQLException e1) {
            if (!e1.getMessage().equalsIgnoreCase(null))
                plugin.console.warning("Error #10-3: " + e1.getMessage());
            else
                plugin.console.warning("Error #10-2: " + e1.getErrorCode() + " - " + e1.getSQLState());
        } catch (Exception e) {
            plugin.console.warning("Error #10-1: " + e.toString());
        }
        return 0;
    }
    
    /**
     * Set balance
     * @param balance New balance
     * @since 0.5
     * @return If the account balance is successfully changed
     */
    public boolean setBalance(double balance) {
        try {
            plugin.SQLWorker.executeUpdate("UPDATE `" + plugin.settings.SQL_account_table + "` SET `amount` = '" + balance + "' WHERE `cleanname` = '" + name.toLowerCase() + "'");
            plugin.BalanceSign.update(name);
            return true;
        } catch (SQLException e) {
            if (!e.getMessage().equalsIgnoreCase(null))
                plugin.console.warning("Error #17-3: " + e.getMessage());
            else
                plugin.console.warning("Error #17-2: " + e.getErrorCode() + " - " + e.getSQLState());
        } catch (Exception e) {
            plugin.console.warning("Error #17-1: " + e.toString());
        }
        return false;
    }
    
    /**
     * Add money
     * 
     * @param amount Amount of money that shall be added
     * @since 0.5
     * @return If the money is successfully added
     */
    public boolean add(double amount) {
        double temp = getBalance();
        temp += amount;
        try {
            plugin.SQLWorker.executeUpdate("UPDATE `" + plugin.settings.SQL_account_table + "` SET `amount` = '" + temp + "' WHERE `cleanname` = '" + name.toLowerCase() + "'");
            plugin.BalanceSign.update(name);
            return true;
        } catch (SQLException e) {
            if (!e.getMessage().equalsIgnoreCase(null))
                plugin.console.warning("Error #18-2: " + e.getMessage());
            else
                plugin.console.warning("Error #18-1: " + e.getErrorCode() + " - " + e.getSQLState());
        }
        return false;
    }
    
    /**
     * Subtract money
     * 
     * @param amount Amount of money that shall be subtracted
     * @since 0.5
     * @return If the money is successfully subtracted
     */
    public boolean subtract(double amount) {
        double temp = getBalance();
        temp -= amount;
        try {
            plugin.SQLWorker.executeUpdate("UPDATE `" + plugin.settings.SQL_account_table + "` SET `amount` = '" + temp + "' WHERE `cleanname` = '" + name.toLowerCase() + "'");
            plugin.BalanceSign.update(name);
            return true;
        } catch (SQLException e) {
            if (!e.getMessage().equalsIgnoreCase(null))
                plugin.console.warning("Error #19-2: " + e.getMessage());
            else
                plugin.console.warning("Error #19-1: " + e.getErrorCode() + " - " + e.getSQLState());
        }
        return false;
    }
    
    /**
     * Check if the account have password protection
     * @since 0.6
     * @return true if protected; else false
     */
    public boolean havePassword() {
    	if (plugin.PasswordSystem.passwordCheck(name, "")) {
    		return false;
    	}
    	return true;
    }
}