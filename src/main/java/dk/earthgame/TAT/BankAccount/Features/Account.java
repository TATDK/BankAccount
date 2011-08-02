package dk.earthgame.TAT.BankAccount.Features;

import java.sql.SQLException;

import dk.earthgame.TAT.BankAccount.BankAccount;
import dk.earthgame.TAT.BankAccount.Enum.FeeModes;
import dk.earthgame.nijikokun.register.payment.Method.MethodAccount;

/**
 * 
 * @author TAT
 *
 */
public class Account {
	private BankAccount plugin;
	private String accountname;
	private Bank bank;
	
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
	
	public Bank getBank() {
		if (bank == null) {
			if (plugin.settings.Areas) {
				try {
					bank = new Bank(plugin,plugin.SQLWorker.getInt("bank", plugin.settings.SQL_account_table, "`accountname` = " + accountname));
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
            String[] users = plugin.SQLWorker.getString("users", plugin.settings.SQL_account_table, "`cleanname` = '" + accountname.toLowerCase() + "'").split(";");
            for (String u : users) {
                newUsers += ";" + u;
            }
            plugin.SQLWorker.executeUpdate("UPDATE `" + plugin.settings.SQL_account_table + "` SET `users` = '" + newUsers + "' WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
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
            String[] users = plugin.SQLWorker.getString("users", plugin.settings.SQL_account_table, "`cleanname` = '" + accountname.toLowerCase() + "'").split(";");
            for (String u : users) {
                if (!u.equalsIgnoreCase(player)) {
                    if (!newUsers.equalsIgnoreCase("")) {
                        newUsers += ";";
                    }
                    newUsers += u;    
                }
            }
            plugin.SQLWorker.executeUpdate("UPDATE `" + plugin.settings.SQL_account_table + "` SET `users` = '" + newUsers + "' WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
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
            String[] owners = plugin.SQLWorker.getString("owners", plugin.settings.SQL_account_table, "`cleanname` = '" + accountname.toLowerCase() + "'").split(";");
            for (String o : owners) {
                newOwners += ";" + o;
            }
            plugin.SQLWorker.executeUpdate("UPDATE `" + plugin.settings.SQL_account_table + "` SET `owners` = '" + newOwners + "' WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
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
            String[] owners = plugin.SQLWorker.getString("owners", plugin.settings.SQL_account_table, "`cleanname` = '" + accountname.toLowerCase() + "'").split(";");
            for (String o : owners) {
                if (!o.equalsIgnoreCase(player)) {
                    if (!newOwners.equalsIgnoreCase("")) {
                        newOwners += ";";
                    }
                    newOwners += o;    
                }
            }
            plugin.SQLWorker.executeUpdate("UPDATE `" + plugin.settings.SQL_account_table + "` SET `owners` = '" + newOwners + "' WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
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
        	plugin.SQLWorker.executeUpdate("UPDATE `" + plugin.settings.SQL_account_table + "` SET `password` = '" + password + "' WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
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
    public boolean ATM(String player,String type,Double amount,String password) {
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
                    plugin.BalanceSign.update(accountname);
                    return true;
                } else {
                    return false;
                }
            } else if (type == "withdraw") {
                if (plugin.PasswordSystem.passwordCheck(accountname, password)) {
                    if ((account - amount) >= 0) {
                        if (plugin.settings.WithdrawFee.getMode() != FeeModes.NONE) {
                            if (plugin.settings.WithdrawFee.CanAfford(account - amount)) {
                                if (!subtract(plugin.settings.WithdrawFee.CalculateFee(account - amount))) {
                                    return false;
                                }
                            } else {
                                return false;
                            }
                        }
                        subtract(amount);
                        economyAccount.add(amount);
                        plugin.BalanceSign.update(accountname);
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
                                if (!subtract(plugin.settings.TransferFee.CalculateFee(account - amount))) {
                                    return false;
                                }
                            } else {
                                return false;
                            }
                        }
                        subtract(amount);
                        reciever.add(amount);
                        plugin.BalanceSign.update(accountname);
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
     * @throws BankAccountException 
     */
    public boolean close(String player,String password) {
        if (plugin.PasswordSystem.passwordCheck(accountname, password)) {
            try {
                MethodAccount economyAccount = plugin.Method.getAccount(player);
                double accountBalance = getBalance();
                if (plugin.settings.ClosingFee.getMode() != FeeModes.NONE) {
                    if (plugin.settings.ClosingFee.CanAfford(accountBalance)) {
                        if (!subtract(plugin.settings.ClosingFee.CalculateFee(accountBalance))) {
                            return false;
                        } else {
                            accountBalance -= plugin.settings.ClosingFee.CalculateFee(accountBalance);
                        }
                    } else {
                        return false;
                    }
                }
                plugin.SQLWorker.executeDelete("DELETE FROM `" + plugin.settings.SQL_account_table + "` WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
                economyAccount.add(accountBalance);
                plugin.BalanceSign.update(accountname);
                plugin.knownAccounts.remove(accountname);
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
     * @throws BankAccountException 
     */
    public String getUsers() {
        try {
            String output = "";
            String[] users = plugin.SQLWorker.getString("users", plugin.settings.SQL_account_table, "`cleanname` = '" + accountname.toLowerCase() + "'").split(";");
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
     * @throws BankAccountException 
     */
    public String getOwners() {
        try {
            String output = "";
            String[] owners = plugin.SQLWorker.getString("owners", plugin.settings.SQL_account_table, "`cleanname` = '" + accountname.toLowerCase() + "'").split(";");
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
     * @throws BankAccountException 
     */
    public double getBalance() {
        try {
            return plugin.SQLWorker.getDouble("amount", plugin.settings.SQL_account_table, "`cleanname` = '" + accountname.toLowerCase() +"'");
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
     * @throws BankAccountException 
     */
    public boolean setBalance(double balance) {
        try {
        	plugin.SQLWorker.executeUpdate("UPDATE `" + plugin.settings.SQL_account_table + "` SET `amount` = '" + balance + "' WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
            plugin.BalanceSign.update(accountname);
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
     * @throws BankAccountException 
     */
    public boolean add(double amount) {
        double temp = getBalance();
        temp += amount;
        try {
        	plugin.SQLWorker.executeUpdate("UPDATE `" + plugin.settings.SQL_account_table + "` SET `amount` = '" + temp + "' WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
            plugin.BalanceSign.update(accountname);
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
     * @throws BankAccountException 
     */
    public boolean subtract(double amount) {
        double temp = getBalance();
        temp -= amount;
        try {
            plugin.SQLWorker.executeUpdate("UPDATE `" + plugin.settings.SQL_account_table + "` SET `amount` = '" + temp + "' WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
            plugin.BalanceSign.update(accountname);
            return true;
        } catch (SQLException e) {
            if (!e.getMessage().equalsIgnoreCase(null))
            	plugin.console.warning("Error #19-2: " + e.getMessage());
            else
            	plugin.console.warning("Error #19-1: " + e.getErrorCode() + " - " + e.getSQLState());
        }
        return false;
    }
}