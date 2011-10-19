package dk.earthgame.TAT.BankAccount;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.config.Configuration;

import com.nijiko.permissions.PermissionHandler;

import dk.earthgame.TAT.BankAccount.Enum.FeeModes;
import dk.earthgame.TAT.BankAccount.Features.AccountFee;
import dk.earthgame.TAT.BankAccount.Features.PlayerFee;
import dk.earthgame.TAT.BankAccount.System.Upgrade;

/**
 * BankAccount settings
 * @author TAT
 */
public class Settings {
    private BankAccount plugin;
    
    Configuration config;
    public int checkJobId;
    public int areaWandId;
    public boolean multiBanks;
    //MySQL
    public boolean useMySQL = false;
    String MySQL_host;
    String MySQL_port;
    String MySQL_username;
    String MySQL_password;
    String MySQL_database;
    //SQL
    public Connection con;
    public Statement selectStmt;
    public Statement updateStmt;
    public Statement stmt;
    public String SQL_account_table;
    public String SQL_area_table;
    public String SQL_loan_table;
    public String SQL_transaction_table;
    public String SQL_banks_table;
    //Permissions
    public boolean useOP;
    public PermissionHandler Permissions = null;
    public boolean usePermissions;
    public GroupManager GroupManager = null;
    public boolean useGroupManager;
    public boolean areas;
    public boolean superAdmins;
    public boolean depositAll;
    //Interest
    public int interestTime;
    public double interestOnlineAmount;
    public double interestOfflineAmount;
    public int interestNeededOnline;
    public boolean multiInterests;
    //Transaction
    public boolean transactions;
    //Fee
    public PlayerFee OpeningFee;
    public PlayerFee DepositFee;
    public AccountFee WithdrawFee;
    public AccountFee TransferFee;
    public PlayerFee ClosingFee;
    public PlayerFee SignFee;
    //Start Amount
    public boolean startAmount_Active;
    public double startAmount_Fee;
    public double startAmount_Static;
    //Account
    public double maxAmount;
    //Debug messages
    public boolean debug_Loan;
    public boolean debug_Interest;
    
    public Settings(BankAccount plugin) {
        this.plugin = plugin;
    }
    
    private FeeModes stringToType(String s) {
        if (s.equalsIgnoreCase("percentage"))
            return FeeModes.PERCENTAGE;
        if (s.equalsIgnoreCase("static"))
            return FeeModes.STATIC;
        if (s.equalsIgnoreCase("smart1"))
            return FeeModes.SMART1;
        if (s.equalsIgnoreCase("smart2"))
            return FeeModes.SMART2;
        return FeeModes.NONE;
    }
    
    @SuppressWarnings("unchecked")
    boolean loadConfiguration() {
        config = new Configuration(new File(plugin.getDataFolder(), "config.yml"));
        config.load();
        //SQL
        transactions = config.getBoolean("SQL-info.Transactions", false);
        useMySQL = config.getBoolean("SQL-info.MySQL", false);
        MySQL_host = config.getString("SQL-info.Host","localhost");
        MySQL_port = config.getString("SQL-info.Port","3306");
        MySQL_username = config.getString("SQL-info.User","root");
        MySQL_password = config.getString("SQL-info.Pass","");
        MySQL_database = config.getString("SQL-info.Database","minecraft");
        //SQL TABLES
        SQL_account_table = config.getString("SQL-tables.Account","bankaccounts");
        SQL_area_table = config.getString("SQL-tables.Area","bankareas");
        SQL_loan_table = config.getString("SQL-tables.Loan","bankloans");
        SQL_transaction_table = config.getString("SQL-tables.Transaction","banktransactions");
        SQL_banks_table = config.getString("SQL-tables.Banks","banks");
        //Permissions
        useOP = config.getBoolean("Permissions.OP",true);
        usePermissions = config.getBoolean("Permissions.Permissions",false);
        useGroupManager = config.getBoolean("Permissions.GroupManager",false);
        superAdmins = config.getBoolean("Permissions.SuperAdmins", false);
        depositAll = config.getBoolean("Permissions.DepositAll", false);
        //Interest
        interestOnlineAmount = config.getDouble("Interest.DefaultInterest.Online-amount", 0);
        interestNeededOnline = config.getInt("Interest.DefaultInterest.Online-limit", 1);
        if (interestNeededOnline < 1) {
            interestNeededOnline = 1;
            plugin.console.warning("Interest -> Online-limit must be between 1 and 100");
            plugin.console.info("Interest -> Online-limit set to 1");
        } else if (interestNeededOnline > 100) {
            interestNeededOnline = 100;
            plugin.console.warning("Interest -> Online-limit must be between 1 and 100");
            plugin.console.info("Interest -> Online-limit set to 100");
        }
        interestOfflineAmount = config.getDouble("Interest.DefaultInterest.Offline-amount", 0);
        interestTime = config.getInt("Interest.Time", 0);
        multiInterests = config.getBoolean("Interest.MultipleInterests", false);
        //Area
        areas = config.getBoolean("Areas.Active",false);
        areaWandId = config.getInt("Areas.AreaWandid",339);
        multiBanks = config.getBoolean("Areas.MultipleBanks", false);
        //Loan
        plugin.LoanSystem.LoanActive = config.getBoolean("Loan.Active", false);
        plugin.LoanSystem.Fixed_rate = config.getDouble("Loan.Fixed-rate", 0);
        plugin.LoanSystem.Rates = (HashMap<Double, Double>)config.getProperty("Loan.Rate");
        plugin.LoanSystem.Max_amount = config.getDouble("Loan.Max-amount", 200);
        plugin.LoanSystem.PaymentTime = config.getInt("Loan.Payment-time", 60);
        plugin.LoanSystem.PaymentParts = config.getInt("Loan.Payment-parts", 3);
        //Fee
        OpeningFee = new PlayerFee(stringToType(config.getString("Fee.Opening.Mode","NONE")), config.getDouble("Fee.Opening.Percentage",0), config.getDouble("Fee.Opening.Static",0), plugin);
        DepositFee = new PlayerFee(stringToType(config.getString("Fee.Deposit.Mode","NONE")), config.getDouble("Fee.Deposit.Percentage",0), config.getDouble("Fee.Deposit.Static",0), plugin);
        WithdrawFee = new AccountFee(stringToType(config.getString("Fee.Withdraw.Mode","NONE")), config.getDouble("Fee.Withdraw.Percentage",0), config.getDouble("Fee.Withdraw.Static",0), plugin);
        TransferFee = new AccountFee(stringToType(config.getString("Fee.Transfer.Mode","NONE")), config.getDouble("Fee.Transfer.Percentage",0), config.getDouble("Fee.Transfer.Static",0), plugin);
        ClosingFee = new PlayerFee(stringToType(config.getString("Fee.Closing.Mode","NONE")), config.getDouble("Fee.Closing.Percentage",0), 0, plugin);
        SignFee = new PlayerFee(stringToType(config.getString("Fee.Sign.Mode","NONE")), 0, config.getDouble("Fee.Sign.Static",0), plugin);
        //Start Amount
        startAmount_Active = config.getBoolean("StartAmount.Active", false);
        startAmount_Fee = config.getDouble("StartAmount.Fee", 0);
        startAmount_Static = config.getDouble("StartAmount.Static", 0);
        //Account
        maxAmount = config.getDouble("Account.MaxAmount", 0);
        //Debug
        debug_Interest = config.getBoolean("Debug.Interest", true);
        debug_Loan = config.getBoolean("Debug.Loan", true);
        
        plugin.console.info("Properties Loaded");
        //Load class
        try {
            if (useMySQL) {
                Class.forName("com.mysql.jdbc.Driver");
            } else {
                Class.forName("org.sqlite.JDBC");
            }
        } catch (ClassNotFoundException e2) {
            e2.printStackTrace();
        }
        //Try connect to database
        try {
            if (useMySQL) {
                con = DriverManager.getConnection("jdbc:mysql://" + MySQL_host + ":" + MySQL_port + "/" + MySQL_database, MySQL_username, MySQL_password);
            } else {
                con = DriverManager.getConnection("jdbc:sqlite:" + plugin.myFolder.getAbsolutePath() + "/BankAccount.db");
            }
            try {
                if (useMySQL) {
                    selectStmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);
                    updateStmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);
                    stmt       = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);
                    plugin.console.info("Connected to MySQL");
                } else {
                    selectStmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
                    updateStmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
                    stmt       = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
                    plugin.console.info("Connected to SQLite");
                }
                
                //Check if the tables exists, else create them
                boolean checkAccount = false;
                boolean checkArea = false;
                boolean checkLoan = false;
                boolean checkTransaction = false;
                boolean checkBanks = false;
                try {
                    ResultSet tables = con.getMetaData().getTables(null, null, null, null);
                    while (tables.next()) {
                        String tablename = tables.getString("TABLE_NAME");
                        if (tablename.equalsIgnoreCase(SQL_account_table)) {
                            checkAccount = true;
                        } else if (tablename.equalsIgnoreCase(SQL_area_table)) {
                            checkArea = true;
                        } else if (tablename.equalsIgnoreCase(SQL_loan_table)) {
                            checkLoan = true;
                        } else if (tablename.equalsIgnoreCase(SQL_transaction_table)) {
                            checkTransaction = true;
                        } else if (tablename.equalsIgnoreCase(SQL_banks_table)) {
                            checkBanks = true;
                        }
                    }
                } catch (SQLException e3) {
                    plugin.console.warning("Couldn't get tables existing! Running as if all exists");
                    plugin.console.warning(e3.toString());
                }
                try {
                    if (!checkAccount) {
                        //ACCOUNT TABLE
                        plugin.console.info("Creating table " + SQL_account_table);
                        String query = "CREATE TABLE IF NOT EXISTS `" + SQL_account_table + "` (`accountname` VARCHAR( 255 ) NOT NULL , `cleanname` VARCHAR( 255 ) NOT NULL , `owners` LONGTEXT NOT NULL, `users` LONGTEXT NOT NULL, `password` VARCHAR( 255 ) NULL DEFAULT '', `amount` DOUBLE( 255,2 ) NOT NULL DEFAULT '0')";
                        if (useMySQL)
                            query = "CREATE TABLE IF NOT EXISTS `" + SQL_account_table + "` (`id` INT( 255 ) NOT NULL AUTO_INCREMENT PRIMARY KEY , `accountname` VARCHAR( 255 ) NOT NULL , `cleanname` VARCHAR( 255 ) NOT NULL , `owners` LONGTEXT NOT NULL, `users` LONGTEXT NOT NULL, `password` VARCHAR( 255 ) NULL DEFAULT '', `amount` DOUBLE( 255,2 ) NOT NULL DEFAULT '0')";
                        stmt.execute(query);
                        checkAccount = true;
                    }
                    if (checkArea && areas) {
                        //AREA TABLE
                        ResultSet rs;
                        rs = plugin.SQLWorker.executeSelect("SELECT * FROM `" + SQL_area_table + "`");
                        while(rs.next()) {
                            String areaname = rs.getString("areaname");
                            World world = plugin.getServer().getWorld(rs.getString("world"));
                            plugin.BankAreas.add(
                                areaname,
                                new Location(world,rs.getInt("x1"),rs.getInt("y1"),rs.getInt("z1")),
                                new Location(world,rs.getInt("x2"),rs.getInt("y2"),rs.getInt("z2"))
                            );
                        }
                        plugin.SQLWorker.executeDelete("DROP TABLE `" + SQL_area_table + "`");
                        checkArea = true;
                    }
                    if (!checkLoan && plugin.LoanSystem.LoanActive) {
                        plugin.console.info("Creating table " + SQL_loan_table);
                        //LOAN TABLE
                        String query = "CREATE TABLE IF NOT EXISTS `" + SQL_loan_table + "` (`player` VARCHAR( 255 ) NOT NULL, `totalamount` DOUBLE( 255,2 ) NOT NULL, `remaining` DOUBLE( 255,2 ) NOT NULL, `timepayment` INT( 255 ) NOT NULL, `timeleft` INT( 255 ) NOT NULL, `part` INT( 255 ) NOT NULL, `parts` INT( 255 ) NOT NULL)";
                        if (useMySQL) {
                            query = "CREATE TABLE IF NOT EXISTS `" + SQL_loan_table + "` (`id` INT( 255 ) NOT NULL AUTO_INCREMENT PRIMARY KEY, `player` VARCHAR( 255 ) NOT NULL, `totalamount` DOUBLE( 255,2 ) NOT NULL, `timeleft` INT( 255 ) NOT NULL, `part` INT( 255 ) NOT NULL, `parts` INT( 255 ) NOT NULL)";
                        }
                        stmt.execute(query);
                        checkLoan = true;
                    }
                    if (!checkTransaction && transactions) {
                        //TRANSACTION TABLE
                        plugin.console.info("Creating table " + SQL_transaction_table);
                        String query = "CREATE TABLE IF NOT EXISTS `" + SQL_transaction_table + "` (`player` VARCHAR( 255 ) NOT NULL, `account` VARCHAR( 255 ) NULL, `type` INT( 255 ) NOT NULL, `amount` DOUBLE( 255,2 ) NULL, `time` INT( 255 ) NOT NULL)";
                        if (useMySQL) {
                            query = "CREATE TABLE IF NOT EXISTS `" + SQL_transaction_table + "` (`id` INT( 255 ) NOT NULL AUTO_INCREMENT PRIMARY KEY , `player` VARCHAR( 255 ) NOT NULL, `account` VARCHAR( 255 ) NULL, `type` INT( 255 ) NOT NULL, `amount` DOUBLE( 255,2 ) NULL, `time` INT( 255 ) NOT NULL)";
                        }
                        stmt.execute(query);
                        checkTransaction = true;
                    }
                    if (!checkBanks && multiBanks) {
                        //BANKS TABLE
                        plugin.console.info("Creating table " + SQL_banks_table);
                        String query = "CREATE TABLE `" + SQL_banks_table + "` (`cleanname` VARCHAR( 255 ) NOT NULL , `bankers` LONGTEXT NOT NULL , `online-interest` DOUBLE( 255,2 ) NOT NULL DEFAULT '0', `offline-interest` DOUBLE( 255,2 ) NOT NULL DEFAULT '0', `online-amount` INT( 3 ) NOT NULL DEFAULT '0')";
                        if (useMySQL) {
                            query = "CREATE TABLE `" + SQL_banks_table + "` (`id` INT( 255 ) NOT NULL AUTO_INCREMENT PRIMARY KEY , `bankname` VARCHAR( 255 ) NOT NULL , `cleanname` VARCHAR( 255 ) NOT NULL , `bankers` LONGTEXT NOT NULL , `online-interest` DOUBLE( 255,2 ) NOT NULL DEFAULT '0', `offline-interest` DOUBLE( 255,2 ) NOT NULL DEFAULT '0', `online-amount` INT( 3 ) NOT NULL DEFAULT '0')";
                        }
                        stmt.execute(query);
                        checkBanks = true;
                    }
                    //Run upgrades of SQL tables
                    new Upgrade(plugin, useMySQL);
                } catch (SQLException e3) {
                    if (!checkAccount) {
                        plugin.console.warning("Failed to find and create table " + SQL_account_table);
                    }
                    if (!checkLoan && plugin.LoanSystem.LoanActive) {
                        plugin.console.warning("Failed to find and create table " + SQL_loan_table);
                        plugin.console.info("Disabled loans!");
                        plugin.LoanSystem.LoanActive = false;
                        if (plugin.LoanSystem.running) {
                            plugin.LoanSystem.shutdownRunner();
                        }
                    }
                    if (!checkTransaction && transactions) {
                        plugin.console.warning("Failed to find and create table " + SQL_transaction_table);
                        plugin.console.info("Disabled transactions!");
                        transactions = false;
                    }
                    if (!checkBanks && multiBanks) {
                        plugin.console.warning("Failed to find and create table " + SQL_banks_table);
                        plugin.console.info("Disabled multiple banks!");
                        multiBanks = false;
                    }
                    if (!e3.getMessage().equalsIgnoreCase(null))
                        plugin.console.warning(e3.getMessage());
                    else
                    	plugin.console.warning(e3.getErrorCode() + " - " + e3.getSQLState());
                    plugin.console.info("Shuting down");
                    plugin.getServer().getPluginManager().disablePlugin(plugin);
                    return false;
                }
            } catch (SQLException e2) {
                if (useMySQL) {
                    plugin.console.warning("Failed to connect to MySQL");
                } else {
                    plugin.console.warning("Failed to connect to SQLite");
                }
                plugin.console.warning(e2.toString());
                plugin.console.info("Shuting down");
                plugin.getServer().getPluginManager().disablePlugin(plugin);
                return false;
            }
        } catch (SQLException e1) {
            if (useMySQL) {
                plugin.console.warning("Failed to connect to MySQL");
            } else {
                plugin.console.warning("Failed to connect to SQLite");
            }
            plugin.console.warning(e1.toString());
            plugin.console.info("Shuting down");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return false;
        }
        return true;
    }
    
    void createDefaultConfiguration() {
        String name = "config.yml";
        File actual = new File(plugin.getDataFolder(), name);
        if (!actual.exists()) {
            InputStream input = BankAccount.class.getResourceAsStream("/Config/" + name);
            if (input != null) {
                FileOutputStream output = null;

                try {
                    output = new FileOutputStream(actual);
                    byte[] buf = new byte[8192];
                    int length = 0;
                    while ((length = input.read(buf)) > 0) {
                        output.write(buf, 0, length);
                    }
                    
                    plugin.console.info("Default config file created!");
                } catch (IOException e) {
                    e.printStackTrace();
                    plugin.console.info("Error creating config file!");
                } finally {
                    try {
                        if (input != null)
                            input.close();
                    } catch (IOException e) {}

                    try {
                        if (output != null)
                            output.close();
                    } catch (IOException e) {}
                }
            }
        } else {
            plugin.console.info("Config file found!");
        }
    }
}
