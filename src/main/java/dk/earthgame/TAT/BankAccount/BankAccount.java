package dk.earthgame.TAT.BankAccount;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.anjocaido.groupmanager.GroupManager;

import com.nijikokun.bukkit.Permissions.Permissions;

import dk.earthgame.TAT.BankAccount.Enum.PermissionNodes;
import dk.earthgame.TAT.BankAccount.Enum.TransactionTypes;
import dk.earthgame.TAT.BankAccount.Features.ATMSign;
import dk.earthgame.TAT.BankAccount.Features.Account;
import dk.earthgame.TAT.BankAccount.Features.BalanceSign;
import dk.earthgame.TAT.BankAccount.Features.BankAreas;
import dk.earthgame.TAT.BankAccount.Features.LoanSystem;
import dk.earthgame.TAT.BankAccount.Features.Interest;
import dk.earthgame.TAT.BankAccount.Listeners.BankAccountBlockListener;
import dk.earthgame.TAT.BankAccount.Listeners.BankAccountDisabled;
import dk.earthgame.TAT.BankAccount.Listeners.BankAccountEntityListener;
import dk.earthgame.TAT.BankAccount.Listeners.BankAccountPlayerListener;
import dk.earthgame.TAT.BankAccount.Listeners.BankAccountPluginListener;
import dk.earthgame.TAT.BankAccount.System.BankAccountException;
import dk.earthgame.TAT.BankAccount.System.Font;
import dk.earthgame.TAT.BankAccount.System.Password;
import dk.earthgame.TAT.BankAccount.System.UserSaves;
import dk.earthgame.TAT.BankAccount.System.Console;
import dk.earthgame.TAT.SignUpdater.SignUpdater;
import dk.earthgame.nijikokun.register.payment.Method;

/**
 * BankAccount for Bukkit
 * @author TAT
 */
public class BankAccount extends JavaPlugin {
    protected final Plugin thisPlugin = this;
    
    //System
    File myFolder;
    private HashMap<String,UserSaves> UserSaves = new HashMap<String,UserSaves>();
    
    //Features
    public ATMSign ATMSign = new ATMSign(this);
    public BalanceSign BalanceSign = new BalanceSign(this);
    public BankAreas BankAreas = new BankAreas(this);
    public Interest Interest = new Interest(this);
    public LoanSystem LoanSystem = new LoanSystem(this);
    public Password PasswordSystem = new Password(this);
    public Settings settings = new Settings(this);
    public Console console;
    Font font = new Font();
    public SignUpdater signupdater;
    
    //Listeners
    BankAccountCommandExecutor cmdExecutor   = new BankAccountCommandExecutor(this);
    BankAccountDisabled disabledExecutor     = new BankAccountDisabled();
    BankAccountPluginListener pluginListener = new BankAccountPluginListener(this);
    BankAccountBlockListener blockListener   = new BankAccountBlockListener(this);
    BankAccountPlayerListener playerListener = new BankAccountPlayerListener(this);
    BankAccountEntityListener entityListener = new BankAccountEntityListener(this);
    
    //Economy
    public Method Method = null;
    
    //NPC
    //NPCManager m = new NPCManager(this);

    //#########################################################################//
    
    //SYSTEM
    /**
     * Check if a player have a PermissionNode
     * @param player The player
     * @param node The PermissionNode
     * @param extraLookup If it's an extra lookup (Used by the system)
     * @since 0.5
     * @return If the player have the permission
     */
    private boolean checkPermission(Player player,PermissionNodes node,boolean extraLookup) {
        if (player != null) {
            if (settings.UsePermissions) {
                if (settings.Permissions.has(player, node.getNode())) {
                    return true;
                }
            }
            if (settings.UseGroupManager) {
                if (settings.GroupManager.getWorldsHolder().getWorldPermissions(player).has(player, node.getNode())) {
                    return true;
                }
            }
            if (settings.UseOP) {
                if (player.isOp()) {
                    return true;
                }
            }
            if (node != PermissionNodes.ACCESS && !extraLookup) {
                if (node != PermissionNodes.ADMIN) {
                    if (checkPermission(player,PermissionNodes.ADMIN, true)) {
                        return true;
                    }
                }
                if (node != PermissionNodes.EXTENDED) {
                    if (checkPermission(player,PermissionNodes.EXTENDED, true)) {
                        return true;
                    }
                }
                if (node == PermissionNodes.OPEN || node == PermissionNodes.DEPOSIT || node == PermissionNodes.WITHDRAW || node == PermissionNodes.LIST) {
                    if (checkPermission(player, PermissionNodes.BASIC, true))
                        return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Check for a permission
     * 
     * @param player The player
     * @param node The PermissionNode (dk.earthgame.TAT.BankAccount.System.PermissionNodes)
     * @since 0.5
     * @return If the player have the permission
     */
    public boolean playerPermission(Player player,PermissionNodes node) {
        return checkPermission(player, node, false);
    }
    
    /**
     * Setting up BankAccount
     */
    public void onEnable() {
        console = new Console(getDescription());
        getDataFolder().mkdir();
        
        // Register our events
        getCommand("account").setExecutor(cmdExecutor);
        getCommand("account").setUsage("/account help - Show help to BankAccount");
        
        PluginManager pm = getServer().getPluginManager();
        //RightClick - Used for area selection
        pm.registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
        //Damage - Used for bounty
        pm.registerEvent(Type.ENTITY_DAMAGE, entityListener, Priority.Normal, this);
        //Enable/Disable - Used for hook up to other plugins
        pm.registerEvent(Type.PLUGIN_ENABLE, pluginListener, Priority.Low, this);
        pm.registerEvent(Type.PLUGIN_DISABLE, pluginListener, Priority.Low, this);
        //Sign - Used for balance signs
        pm.registerEvent(Type.SIGN_CHANGE, blockListener, Priority.Normal, this);
        pm.registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
        
        console.enabled();

        myFolder = getDataFolder();
        if (!myFolder.exists()) {
            console.message("Config folder missing, creating...");
            myFolder.mkdir();
            console.message("Folder created");
        }
        
        /*
         * Check if economy isn't hooked up 20 seconds after startup of BankAccount
         */
        settings.checkJobId = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
                if (!pluginListener.Methods.hasMethod()) {
                    //Shutdown if economy isn't found
                    console.warning("Stopping BankAccount - Reason: Missing economy plugin!");
                    getServer().getPluginManager().disablePlugin(thisPlugin);
                    settings.checkJobId = 0;
                }
            }
        }, 20*20);

        settings.createDefaultConfiguration();
        settings.loadConfiguration();
        
        /*
         * Check if missing hook up is possible
         * Checking for permissions plugins
         * 
         * Used if BankAccount is started after one of the third-part plugins
         */
        if (settings.Permissions == null && settings.UsePermissions) {
            Plugin test = getServer().getPluginManager().getPlugin("Permissions");
            if (test != null) {
                if (test.isEnabled()) {
                    settings.Permissions = ((Permissions)test).getHandler();
                    console.info("Established connection with Permissions!");
                }
            }
        }
        if (settings.GroupManager == null && settings.UseGroupManager) {
            Plugin test = getServer().getPluginManager().getPlugin("GroupManager");
            if (test != null) {
                if (test.isEnabled()) {
                    settings.GroupManager = (GroupManager)test;
                    console.info("Established connection with GroupManager!");
                }
            }
        }
        if (signupdater == null) {
            Plugin test = getServer().getPluginManager().getPlugin("SignUpdater");
            if (test != null) {
                if (test.isEnabled()) {
                    signupdater = (SignUpdater)test;
                    console.info("Established connection with SignUpdater!");
                }
            }
        }

        BalanceSign.loadSigns();
    }

    /**
     * Setting up BankAccount to use disabledExecutor and cancel tasks
     */
    public void onDisable() {
        LoanSystem.shutdownRunner();
        Interest.shutdownInterest();
        getCommand("account").setExecutor(disabledExecutor);
        getCommand("account").setUsage(ChatColor.RED + "BankAccount is disabled");
        console.disabled();
    }

    /**
     * Add a transaction to the database
     * 
     * @param player Username of the player
     * @param account Name of account
     * @param type Type of transaction (dk.earthgame.TAT.BankAccount.System.TransactionTypes)
     * @param amount amount of money the transaction (0.00 if money isn't a part of the transaction)
     * @throws BankAccountException 
     * @since 0.5
     */
    public void addTransaction(String player, String account, TransactionTypes type, Double amount) throws BankAccountException {
        if (settings.Transactions) {
            try {
                int time = Math.round(new Date().getTime()/1000);
                settings.stmt.executeUpdate("INSERT INTO `" + settings.SQL_transaction_table + "` (`player`,`account`,`type`,`amount`,`time`) VALUES ('" + player + "','" + account + "','" + type.get() + "','" + amount + "','" + time +"')");
            } catch(SQLException e) {
                if (!e.getMessage().equalsIgnoreCase(null))
                    console.warning("Error #16-2: " + e.getMessage());
                else
                    console.warning("Error #16-1: " + e.getErrorCode() + " - " + e.getSQLState());
                throwException("SQL error adding transaction");
            }
        }
    }
    
    //USERSAVES
    
    /**
     * Get the saved data of a player
     * 
     * @param player The username of the player
     * @since 0.5
     * @return UserSaves - the saved data
     */
    public UserSaves getSaved(String player) {
        if (UserSaves.containsKey(player)) {
            return UserSaves.get(player);
        }
        
        UserSaves save = new UserSaves();
        UserSaves.put(player, save);
        return save;
    }
    
    //ATM / ACCOUNTS
    /**
     * Check if an account exists
     * 
     * @param accountname The name of the account
     * @since 0.5
     * @return If the account exists
     * @throws BankAccountException 
     */
    public boolean accountExists(String accountname) throws BankAccountException {
        ResultSet rs;
        int id = 0;
        try {
            if (settings.UseMySQL) {
                rs = settings.stmt.executeQuery("SELECT `id` FROM `" + settings.SQL_account_table + "` WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
            } else {
                rs = settings.stmt.executeQuery("SELECT `rowid` FROM `" + settings.SQL_account_table + "` WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
            }
            while (rs.next()) {
                if (settings.UseMySQL) {
                    id = rs.getInt("id");
                } else {
                    id = rs.getInt("rowid");
                }
            }
        } catch (SQLException e) {
            if (!e.getMessage().equalsIgnoreCase(null))
                console.warning("Error #01-2: " + e.getMessage());
            else
                console.warning("Error #01-1: " + e.getErrorCode() + " - " + e.getSQLState());
            throwException("SQL error while looking up for account");
        }
        if (id > 0) {
            return true;
        }
        return false;
    }
    
    /**
     * List of accounts, the user have access to
     * 
     * @since 0.5
     * @param player Username of the player
     * @return List of accounts 
     * @throws BankAccountException 
     */
    public List<String> accountList(String player) throws BankAccountException {
        List<String> accounts = new ArrayList<String>();
        ResultSet rs = null;
        try {
            rs = settings.selectStmt.executeQuery("SELECT `accountname` FROM `" + settings.SQL_account_table + "` WHERE `owners` LIKE '%" + player + "%' OR `users` LIKE '%" + player + "%'");
            while (rs.next()) {
                String accountname = rs.getString("accountname");
                //Make sure it's not just a part of the name
                if (accessAccount(accountname,player,false)) {
                    accounts.add(accountname);
                }
            }
        } catch (SQLException e) {
            if (!e.getMessage().equalsIgnoreCase(null))
                console.warning("Error #22-2: " + e.getMessage());
            else
                console.warning("Error #22-1: " + e.getErrorCode() + " - " + e.getSQLState());
            throwException("SQL error getting account list");
        }
        //Be sure that rs is closed
        try {
            rs.close();
        } catch (SQLException e) {
            if (!e.getMessage().equalsIgnoreCase(null))
                console.warning("Error #22-2: " + e.getMessage());
            else
                console.warning("Error #22-1: " + e.getErrorCode() + " - " + e.getSQLState());
        }
        return accounts;
    }
    
    /**
     * List of accounts, the user have access to
     * 
     * @since 0.5
     * @param player The player
     * @return List of accounts
     * @throws BankAccountException 
     */
    public List<String> accountList(Player player) throws BankAccountException { return accountList(player.getName()); }
    
    /**
     * Open new account without fee paid
     * 
     * @param accountname The name of the account
     * @param owners Username of the owners - Name of players separated by semicolon (;)
     * @since 0.6
     * @return If the account is successfully created
     * @throws BankAccountException
     */
    public boolean openAccount(String accountname,String owners) throws BankAccountException { return openAccount(accountname, owners, 0); }
    
    /**
     * Open new account
     * 
     * @param accountname The name of the account
     * @param owners Username of the owners - Name of players separated by semicolon (;)
     * @param feePaid 
     * @since 0.5.1
     * @return If the account is successfully created
     * @throws BankAccountException 
     */
    public boolean openAccount(String accountname,String owners,double feePaid) throws BankAccountException {
        double StartAmount = 0;
        if (settings.StartAmount_Active) {
            StartAmount += feePaid*settings.StartAmount_Fee;
            StartAmount += settings.StartAmount_Static;
        }
        
        try {
            settings.stmt.executeUpdate("INSERT INTO `" + settings.SQL_account_table + "` (`accountname`,`cleanname`,`owners`,`users`,`amount`) VALUES ('" + accountname + "','" + accountname.toLowerCase() + "','" + owners + "','','" + StartAmount + "')");
            BalanceSign.updateSigns(accountname);
            return true;
        } catch(SQLException e) {
            if (!e.getMessage().equalsIgnoreCase(null))
                console.warning("Error #02-3: " + e.getMessage());
            else
                console.warning("Error #02-2: " + e.getErrorCode() + " - " + e.getSQLState());
            throwException("SQL error opening account");
        } catch (Exception e) {
            console.warning("Error #02-1: " + e.toString());
            throwException("Intern error opening account");
        }
        return false;
    }
    
    /**
     * Do the player have access to an account
     * 
     * @param accountname Name of account
     * @param player The player
     * @param writeAccess Only look for owners
     * @since 0.5
     * @return If the player have access
     * @throws BankAccountException 
     */
    public boolean accessAccount(String accountname,Player player,boolean writeAccess) throws BankAccountException {
        if (!accountExists(accountname)) {
            //There is no spoon... I mean account
            return false;
        }
        if (settings.SuperAdmins && playerPermission(player, PermissionNodes.ADMIN)) {
            //Ta ta taaa da.. SuperAdmin!
            return true;
        }
        try {
            String coloum;
            if (writeAccess) {
                coloum = "owners";
            } else {
                coloum = "users`, `owners";
            }
            ResultSet rs;
            rs = settings.stmt.executeQuery("SELECT `" + coloum + "` FROM `" + settings.SQL_account_table + "` WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
            while(rs.next()) {
                //Owners
                String[] owners = rs.getString("owners").split(";");
                for (String p : owners) {
                    if (p.equalsIgnoreCase(player.getName())) {
                        return true;
                    }
                }
                //Users (if no write access is needed)
                if (!writeAccess) {
                    String[] users = rs.getString("users").split(";");
                    for (String p : users) {
                        if (p.equalsIgnoreCase(player.getName())) {
                            return true;
                        }
                    }
                }
            }
        } catch(SQLException e1) {
            if (!e1.getMessage().equalsIgnoreCase(null))
                console.warning("Error #03-3: " + e1.getMessage());
            else
                console.warning("Error #03-2: " + e1.getErrorCode() + " - " + e1.getSQLState());
            throwException("SQL error checking access to account");
        } catch(Exception e) {
            console.warning("Error #03-1: " + e.toString());
            throwException("Intern error checking access to account");
        }
        return false;
    }
    
    /**
     * Do the player have access to an account
     * 
     * @param accountname Name of account
     * @param player Username of player
     * @param writeAccess Only look for owners
     * @since 0.5
     * @return If the player have access
     * @throws BankAccountException 
     */
    public boolean accessAccount(String accountname,String player,boolean writeAccess) throws BankAccountException {
        try {
            ResultSet rs;
            rs = settings.stmt.executeQuery("SELECT `users`, `owners` FROM `" + settings.SQL_account_table + "` WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
            while(rs.next()) {
                //Owners
                String[] owners = rs.getString("owners").split(";");
                for (String p : owners) {
                    if (p.equalsIgnoreCase(player)) {
                        return true;
                    }
                }
                //Users (if no write access is needed)
                if (!writeAccess) {
                    String[] users = rs.getString("users").split(";");
                    for (String p : users) {
                        if (p.equalsIgnoreCase(player)) {
                            return true;
                        }
                    }
                }
            }
        } catch(SQLException e1) {
            if (!e1.getMessage().equalsIgnoreCase(null))
                console.warning("Error #03-3: " + e1.getMessage());
            else
                console.warning("Error #03-2: " + e1.getErrorCode() + " - " + e1.getSQLState());
            throwException("SQL error checking access to account");
        } catch(Exception e) {
            console.warning("Error #03-1: " + e.toString());
            throwException("Intern error checking access to account");
        }
        return false;
    }
    
    public Account getAccount(String accountname) throws BankAccountException {
    	if (accountExists(accountname)) {
    		return new Account(this, accountname);
    	} else {
    		return null;
    	}
    }
    
    /**
     * Throw a BankAccountException with a specific message in case full debug is enabled
     * 
     * @param msg Message
     * @since 0.6
     * @see #throwException()
     * @throws BankAccountException
     */
    public void throwException(String msg) throws BankAccountException {
        console.info("Check throwException");
        if (settings.Debug_Full) {
            throw new BankAccountException(msg);
        }
    }
    
    /**
     * Throw a BankAccountException with a blank message in case full debug is enabled
     * 
     * @since 0.6
     * @see #throwException(String)
     * @throws BankAccountException
     */
    public void throwException() throws BankAccountException { throwException(""); }
}