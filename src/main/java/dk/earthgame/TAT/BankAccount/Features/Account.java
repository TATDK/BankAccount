package dk.earthgame.TAT.BankAccount.Features;

import java.sql.ResultSet;
import java.sql.SQLException;

import dk.earthgame.TAT.BankAccount.BankAccount;
import dk.earthgame.TAT.BankAccount.Enum.FeeModes;
import dk.earthgame.TAT.BankAccount.System.BankAccountException;
import dk.earthgame.nijikokun.register.payment.Method.MethodAccount;

public class Account {
	private BankAccount plugin;
	private String accountname;
	
	public Account(BankAccount instantiate, String accountname) {
		plugin = instantiate;
		this.accountname = accountname;
	}
	
	/**
	 * Get name of account
	 * 
	 * @return Account name
	 */
	public String getName() {
		return accountname;
	}
	
	/**
     * Add user to account
     * 
     * @param player Username of the player
     * @since 0.5
     * @see #addOwner(String player)
     * @return If the user is successfully added
     * @throws BankAccountException 
     */
    public boolean addUser(String player) throws BankAccountException {
        try {
            String newPlayers = player;
            ResultSet rs;
            rs = plugin.settings.stmt.executeQuery("SELECT `users` FROM `" + plugin.settings.SQL_account_table + "` WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
            while(rs.next()) {
                String[] players = rs.getString("users").split(";");
                for (String p : players) {
                    newPlayers += ";" + p;
                }
            }
            plugin.settings.stmt.executeUpdate("UPDATE `" + plugin.settings.SQL_account_table + "` SET `users` = '" + newPlayers + "' WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
            return true;
        } catch(SQLException e) {
            if (!e.getMessage().equalsIgnoreCase(null))
            	plugin.console.warning("Error #04-3: " + e.getMessage());
            else
            	plugin.console.warning("Error #04-2: " + e.getErrorCode() + " - " + e.getSQLState());
            plugin.throwException("SQL error adding user");
        } catch (Exception e) {
        	plugin.console.warning("Error #04-1: " + e.toString());
        	plugin.throwException("Intern error adding user");
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
     * @throws BankAccountException 
     */
    public boolean removeUser(String player) throws BankAccountException {
        try {
            String newPlayers = "";
            ResultSet rs;
            rs = plugin.settings.stmt.executeQuery("SELECT `users` FROM `" + plugin.settings.SQL_account_table + "` WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
            while(rs.next()) {
                String[] players = rs.getString("users").split(";");
                for (String p : players) {
                    if (!p.equalsIgnoreCase(player)) {
                        if (!newPlayers.equalsIgnoreCase("")) {
                            newPlayers += ";";
                        }
                        newPlayers += p;    
                    }
                }
            }
            plugin.settings.stmt.executeUpdate("UPDATE `" + plugin.settings.SQL_account_table + "` SET `users` = '" + newPlayers + "' WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
            return true;
        } catch(SQLException e) {
            if (!e.getMessage().equalsIgnoreCase(null))
            	plugin.console.warning("Error #05-3: " + e.getMessage());
            else
            	plugin.console.warning("Error #05-2: " + e.getErrorCode() + " - " + e.getSQLState());
            plugin.throwException("SQL error removing user");
        } catch (Exception e) {
        	plugin.console.warning("Error #05-1: " + e.toString());
        	plugin.throwException("Intern error removing user");
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
     * @throws BankAccountException 
     */
    public boolean addOwner(String player) throws BankAccountException {
        try {
            String newPlayers = player;
            ResultSet rs;
            rs = plugin.settings.stmt.executeQuery("SELECT `owners` FROM `" + plugin.settings.SQL_account_table + "` WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
            while(rs.next()) {
                String[] players = rs.getString("owners").split(";");
                for (String p : players) {
                    newPlayers += ";" + p;
                }
            }
            return true;
        } catch(SQLException e) {
            if (!e.getMessage().equalsIgnoreCase(null))
            	plugin.console.warning("Error #xx-3: " + e.getMessage());
            else
            	plugin.console.warning("Error #xx-2: " + e.getErrorCode() + " - " + e.getSQLState());
            plugin.throwException("SQL error adding owner");
        } catch (Exception e) {
        	plugin.console.warning("Error #xx-1: " + e.toString());
            plugin.throwException("Intern error adding owner");
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
     * @throws BankAccountException 
     */
    public boolean removeOwner(String player) throws BankAccountException {
        try {
            String newPlayers = "";
            ResultSet rs;
            rs = plugin.settings.stmt.executeQuery("SELECT `owners` FROM `" + plugin.settings.SQL_account_table + "` WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
            while(rs.next()) {
                String[] players = rs.getString("owners").split(";");
                for (String p : players) {
                    if (!p.equalsIgnoreCase(player)) {
                        if (!newPlayers.equalsIgnoreCase("")) {
                            newPlayers += ";";
                        }
                        newPlayers += p;    
                    }
                }
            }
            plugin.settings.stmt.executeUpdate("UPDATE `" + plugin.settings.SQL_account_table + "` SET `owners` = '" + newPlayers + "' WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
            return true;
        } catch(SQLException e) {
            if (!e.getMessage().equalsIgnoreCase(null))
            	plugin.console.warning("Error #xx-3: " + e.getMessage());
            else
            	plugin.console.warning("Error #xx-2: " + e.getErrorCode() + " - " + e.getSQLState());
            plugin.throwException("SQL error removing owner");
        } catch (Exception e) {
        	plugin.console.warning("Error #xx-1: " + e.toString());
            plugin.throwException("Intern error removing owner");
        }
        return false;
    }

    /**
     * Set the password for an account
     * 
     * @param password The new password (Must be encrypted!)
     * @since 0.5
     * @return If the password is successfully set
     * @throws BankAccountException 
     */
    public boolean setPassword(String password) throws BankAccountException {
        try {
        	plugin.settings.stmt.executeUpdate("UPDATE `" + plugin.settings.SQL_account_table + "` SET `password` = '" + password + "' WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
            return true;
        } catch(SQLException e) {
            if (!e.getMessage().equalsIgnoreCase(null))
            	plugin.console.warning("Error #06-3: " + e.getMessage());
            else
            	plugin.console.warning("Error #06-2: " + e.getErrorCode() + " - " + e.getSQLState());
            plugin.throwException("SQL error setting password");
        } catch (Exception e) {
        	plugin.console.warning("Error #06-1: " + e.toString());
            plugin.throwException("Intern error setting password");
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
     * @throws BankAccountException 
     */
    public boolean ATM(String player,String type,Double amount,String password) throws BankAccountException {
        try {
            double account = getBalance();
            MethodAccount economyAccount = plugin.Method.getAccount(player);
            if (type == "deposit") {
                if (plugin.settings.MaxAmount > 0 && (account+amount) > plugin.settings.MaxAmount) {
                    //Cancel the transaction
                    return false;
                } else if (economyAccount.hasEnough(amount)) {
                    add(amount);
                    economyAccount.subtract(amount);
                    plugin.BalanceSign.updateSigns(accountname);
                    return true;
                } else {
                    return false;
                }
            } else if (type == "withdraw") {
                if (plugin.PasswordSystem.passwordCheck(accountname, password)) {
                    if ((account - amount) >= 0) {
                        if (plugin.settings.WithdrawFee.getMode() != FeeModes.NONE) {
                            if (plugin.settings.WithdrawFee.CanAfford(account - amount)) {
                                if (!subtract(plugin.settings.WithdrawFee.Fee(account - amount))) {
                                    return false;
                                }
                            } else {
                                return false;
                            }
                        }
                        subtract(amount);
                        economyAccount.add(amount);
                        plugin.BalanceSign.updateSigns(accountname);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else if (type == "transfer") {
                if (plugin.PasswordSystem.passwordCheck(accountname, password)) {
                	Account reciever = plugin.getAccount(player);
                    //Player = receiver account
                    double receiver_account = reciever.getBalance();
                    if (plugin.settings.MaxAmount > 0 && (receiver_account+amount) > plugin.settings.MaxAmount) {
                        //Cancel the transaction
                        return false;
                    } else if ((account - amount) >= 0) {
                        if (plugin.settings.TransferFee.getMode() != FeeModes.NONE) {
                            if (plugin.settings.TransferFee.CanAfford(account - amount)) {
                                if (!subtract(plugin.settings.TransferFee.Fee(account - amount))) {
                                    return false;
                                }
                            } else {
                                return false;
                            }
                        }
                        subtract(amount);
                        reciever.add(amount);
                        plugin.BalanceSign.updateSigns(accountname);
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
        	plugin.throwException("Intern error using ATM");
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
     * @throws BankAccountException 
     */
    public boolean closeAccount(String player,String password) throws BankAccountException {
        if (plugin.PasswordSystem.passwordCheck(accountname, password)) {
            try {
                MethodAccount economyAccount = plugin.Method.getAccount(player);
                double accountBalance = getBalance();
                if (plugin.settings.ClosingFee.getMode() != FeeModes.NONE) {
                    if (plugin.settings.ClosingFee.CanAfford(accountBalance)) {
                        if (!subtract(plugin.settings.ClosingFee.Fee(accountBalance))) {
                            return false;
                        } else {
                            accountBalance -= plugin.settings.ClosingFee.Fee(accountBalance);
                        }
                    } else {
                        return false;
                    }
                }
                plugin.settings.stmt.executeUpdate("DELETE FROM `" + plugin.settings.SQL_account_table + "` WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
                economyAccount.add(accountBalance);
                plugin.BalanceSign.updateSigns(accountname);
                return true;
            } catch(SQLException e) {
                if (!e.getMessage().equalsIgnoreCase(null))
                	plugin.console.warning("Error #08-3: " + e.getMessage());
                else
                	plugin.console.warning("Error #08-2: " + e.getErrorCode() + " - " + e.getSQLState());
                plugin.throwException("SQL error closing account");
            } catch (Exception e) {
            	plugin.console.warning("Error #08-1: " + e.toString());
                plugin.throwException("Intern error closing account");
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
     * @throws BankAccountException 
     */
    public String getUsers() throws BankAccountException {
        try {
            String output = "";
            ResultSet rs;
            rs = plugin.settings.stmt.executeQuery("SELECT `users` FROM `" + plugin.settings.SQL_account_table + "` WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
            while(rs.next()) {
                String[] users = rs.getString("users").split(";");
                for (String user : users) {
                    if (!output.equalsIgnoreCase("")) {
                        output += ", ";
                    }
                    output += user;    
                }
            }
            return output;
        } catch (SQLException e1) {
            if (!e1.getMessage().equalsIgnoreCase(null))
            	plugin.console.warning("Error #09-3: " + e1.getMessage());
            else
            	plugin.console.warning("Error #09-2: " + e1.getErrorCode() + " - " + e1.getSQLState());
            plugin.throwException("SQL error getting users");
        } catch (Exception e) {
        	plugin.console.warning("Error #09-1: " + e.toString());
            plugin.throwException("Intern error getting users");
        }
        return null;
    }
    
    /**
     * Get the owners of an account
     * 
     * @since 0.5
     * @return String of owners (seperated by comma and space(, ))
     * @throws BankAccountException 
     */
    public String getOwners() throws BankAccountException {
        try {
            String output = "";
            ResultSet rs;
            rs = plugin.settings.stmt.executeQuery("SELECT `owners` FROM `" + plugin.settings.SQL_account_table + "` WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
            while(rs.next()) {
                String[] owners = rs.getString("owners").split(";");
                for (String owner : owners) {
                    if (!output.equalsIgnoreCase("")) {
                        output += ", ";
                    }
                    output += owner;    
                }
            }
            return output;
        } catch (SQLException e1) {
            if (!e1.getMessage().equalsIgnoreCase(null))
            	plugin.console.warning("Error #09-3: " + e1.getMessage());
            else
            	plugin.console.warning("Error #09-2: " + e1.getErrorCode() + " - " + e1.getSQLState());
            plugin.throwException("SQL error getting owners");
        } catch (Exception e) {
        	plugin.console.warning("Error #09-1: " + e.toString());
            plugin.throwException("Intern error getting owners");
        }
        return null;
    }
    
    /**
     * Get balance
     * 
     * @since 0.5
     * @return double - Amount of money on account
     * @throws BankAccountException 
     */
    public double getBalance() throws BankAccountException {
        try {
            ResultSet rs;
            rs = plugin.settings.stmt.executeQuery("SELECT `amount` FROM `" + plugin.settings.SQL_account_table + "` WHERE `cleanname` = '" + accountname.toLowerCase() +"'");
            while (rs.next()) {
                return rs.getDouble("amount");
            }
        } catch (SQLException e1) {
            if (!e1.getMessage().equalsIgnoreCase(null))
            	plugin.console.warning("Error #10-3: " + e1.getMessage());
            else
            	plugin.console.warning("Error #10-2: " + e1.getErrorCode() + " - " + e1.getSQLState());
            plugin.throwException("SQL error getting balance");
        } catch (Exception e) {
        	plugin.console.warning("Error #10-1: " + e.toString());
        	plugin.throwException("Intern error getting balance");
        }
        return 0;
    }
    
    /**
     * Set balance
     * @param balance New balance
     * @since 0.5
     * @return If the account balance is successfully changed
     * @throws BankAccountException 
     */
    public boolean setBalance(double balance) throws BankAccountException {
        try {
        	plugin.settings.stmt.executeUpdate("UPDATE `" + plugin.settings.SQL_account_table + "` SET `amount` = '" + balance + "' WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
            plugin.BalanceSign.updateSigns(accountname);
            return true;
        } catch (SQLException e) {
            if (!e.getMessage().equalsIgnoreCase(null))
            	plugin.console.warning("Error #17-3: " + e.getMessage());
            else
            	plugin.console.warning("Error #17-2: " + e.getErrorCode() + " - " + e.getSQLState());
            plugin.throwException("SQL error setting balance");
        } catch (Exception e) {
        	plugin.console.warning("Error #17-1: " + e.toString());
            plugin.throwException("Intern error setting balance");
        }
        return false;
    }
    
    /**
     * Add money
     * 
     * @param amount Amount of money that shall be added
     * @since 0.5
     * @return If the money is successfully added
     * @throws BankAccountException 
     */
    public boolean add(double amount) throws BankAccountException {
        double temp = getBalance();
        temp += amount;
        try {
        	plugin.settings.stmt.executeUpdate("UPDATE `" + plugin.settings.SQL_account_table + "` SET `amount` = '" + temp + "' WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
            plugin.BalanceSign.updateSigns(accountname);
            return true;
        } catch (SQLException e) {
            if (!e.getMessage().equalsIgnoreCase(null))
            	plugin.console.warning("Error #18-2: " + e.getMessage());
            else
            	plugin.console.warning("Error #18-1: " + e.getErrorCode() + " - " + e.getSQLState());
            plugin.throwException("SQL error adding money from account");
        } catch (Exception e) {
        	plugin.throwException("Intern error adding money from account");
        }
        return false;
    }
    
    /**
     * Subtract money
     * 
     * @param amount Amount of money that shall be subtracted
     * @since 0.5
     * @return If the money is successfully subtracted
     * @throws BankAccountException 
     */
    public boolean subtract(double amount) throws BankAccountException {
        double temp = getBalance();
        temp -= amount;
        try {
            plugin.settings.stmt.executeUpdate("UPDATE `" + plugin.settings.SQL_account_table + "` SET `amount` = '" + temp + "' WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
            plugin.BalanceSign.updateSigns(accountname);
            return true;
        } catch (SQLException e) {
            if (!e.getMessage().equalsIgnoreCase(null))
            	plugin.console.warning("Error #19-2: " + e.getMessage());
            else
            	plugin.console.warning("Error #19-1: " + e.getErrorCode() + " - " + e.getSQLState());
            plugin.throwException("SQL error subtracting money from account");
        }
        return false;
    }
}