package dk.earthgame.TAT.BankAccount;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.util.config.Configuration;

import com.nijiko.permissions.PermissionHandler;

import dk.earthgame.TAT.BankAccount.System.Upgrade;

/**
 * BankAccount settings
 * @author TAT
 */
public class Settings {
	private BankAccount plugin;
	
	Configuration config;
	int interestTime;
	double interestAmount;
	double interestOfflineAmount;
	int interestJobId;
	int checkJobId;
	int AreaWandId;
	boolean MultiBanks;
	//MySQL
	boolean UseMySQL = false;
	String MySQL_host;
	String MySQL_port;
	String MySQL_username;
	String MySQL_password;
	String MySQL_database;
	//SQL
	public Connection con;
	public java.sql.Statement stmt;
	public String SQL_account_table;
	String SQL_area_table;
	String SQL_loan_table;
	String SQL_transaction_table;
	String SQL_banks_table;
	//Permissions
	boolean UseOP;
	PermissionHandler Permissions = null;
	boolean UsePermissions;
	GroupManager GroupManager = null;
	boolean UseGroupManager;
	boolean Areas;
	boolean SuperAdmins;
	boolean DepositAll;
	//Transaction
	public boolean Transactions;
	//Fee
	public enum FeeModes {
		NONE,
		PERCENTAGE,
		STATIC,
		SMART1,
		SMART2;
	}
	FeeModes Fee_Mode = FeeModes.NONE;
	double Fee_Percentage;
	double Fee_Static;
	//Start Amount
	boolean StartAmount_Active;
	double StartAmount_Fee;
	double StartAmount_Static;
	//Account
	double MaxAmount;
	//Debug messages
	boolean Debug_Loan;
	boolean Debug_Interest;
	
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
		Transactions = config.getBoolean("SQL-info.Transactions", false);
		UseMySQL = config.getBoolean("SQL-info.MySQL", false);
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
		UseOP = config.getBoolean("Permissions.OP",true);
		UsePermissions = config.getBoolean("Permissions.Permissions",false);
		UseGroupManager = config.getBoolean("Permissions.GroupManager",false);
		SuperAdmins = config.getBoolean("Permissions.SuperAdmins", false);
		DepositAll = config.getBoolean("Permissions.DepositAll", false);
		//Interest
		interestAmount = config.getDouble("Interest.Amount", 0);
		interestOfflineAmount = config.getDouble("Interest.Offline-amount", 0);
		interestTime = config.getInt("Interest.Time", 0);
		//Area
		Areas = config.getBoolean("Areas.Active",false);
		AreaWandId = config.getInt("Areas.AreaWandid",339);
		/* FEATURE: Multiple banks
		MultiBanks = config.getBoolean("Areas.MultipleBanks", false);
		*/
		//Loan
		plugin.LoanSystem.LoanActive = config.getBoolean("Loan.Active", false);
		plugin.LoanSystem.Fixed_rate = config.getDouble("Loan.Fixed-rate", 0);
		plugin.LoanSystem.Rates = (Map<Double, Double>)config.getProperty("Loan.Rate");
		plugin.LoanSystem.Max_amount = config.getDouble("Loan.Max-amount", 200);
		plugin.LoanSystem.PaymentTime = config.getInt("Loan.Payment-time", 60);
		plugin.LoanSystem.PaymentParts = config.getInt("Loan.Payment-parts", 3);
		//Fee
		Fee_Mode = stringToType(config.getString("Fee.Mode","NONE"));
		Fee_Percentage = config.getDouble("Fee.Percentage",0);
		Fee_Static = config.getDouble("Fee.Static",0);
		//Start Amount
		StartAmount_Active = config.getBoolean("StartAmount.Active", false);
		StartAmount_Fee = config.getDouble("StartAmount.Fee", 0);
		StartAmount_Static = config.getDouble("StartAmount.Static", 0);
		//Account
		MaxAmount = config.getDouble("Account.MaxAmount", 0);
		//Debug
		Debug_Interest = config.getBoolean("Debug.Interest", true);
		Debug_Loan = config.getBoolean("Debug.Loan", true);
		
		plugin.console.info("Properties Loaded");
		try {
			if (UseMySQL) {
				Class.forName("com.mysql.jdbc.Driver");
			} else {
				Class.forName("org.sqlite.JDBC");
			}
		} catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		try {
			if (UseMySQL) {
				con = DriverManager.getConnection("jdbc:mysql://" + MySQL_host + ":" + MySQL_port + "/" + MySQL_database, MySQL_username, MySQL_password);
			} else {
				con = DriverManager.getConnection("jdbc:sqlite:" + plugin.myFolder.getAbsolutePath() + "/BankAccount.db");
			}
			try {
				if (UseMySQL) {
					stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);
					plugin.console.info("Connected to MySQL");
				} else {
					stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
					plugin.console.info("Connected to SQLite");
				}
				boolean checkAccount = false;
				boolean checkArea = false;
				boolean checkLoan = false;
				boolean checkTransaction = false;
				//boolean checkBanks = false;
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
							//checkBanks = true;
						}
					}
				} catch (SQLException e3) {
					plugin.console.warning("Couldn't get tables existing! Running as if all exists");
					plugin.console.warning(e3.toString());
				}
				try {
					if (!checkAccount) {
						//ACCOUNT TABLE
						String query = "CREATE TABLE IF NOT EXISTS `" + SQL_account_table + "` (`accountname` VARCHAR( 255 ) NOT NULL , `owners` LONGTEXT NOT NULL, `users` LONGTEXT NOT NULL, `password` VARCHAR( 255 ) NULL DEFAULT '', `amount` DOUBLE( 255,2 ) NOT NULL DEFAULT '0')";
						if (UseMySQL) {
							plugin.console.warning("Created table " + SQL_account_table);
							query = "CREATE TABLE IF NOT EXISTS `" + SQL_account_table + "` (`id` INT( 255 ) NOT NULL AUTO_INCREMENT PRIMARY KEY , `accountname` VARCHAR( 255 ) NOT NULL , `owners` LONGTEXT NOT NULL, `users` LONGTEXT NOT NULL, `password` VARCHAR( 255 ) NULL DEFAULT '', `amount` DOUBLE( 255,2 ) NOT NULL DEFAULT '0')";
						}
						stmt.execute(query);
					}
					if (!checkArea && Areas) {
						//AREA TABLE
						String query = "CREATE TABLE IF NOT EXISTS `" + SQL_area_table + "` (`areaname` VARCHAR( 255 ) NOT NULL , `world` VARCHAR( 255 ) NOT NULL , `x1` INT( 255 ) NOT NULL , `y1` INT( 255 ) NOT NULL , `z1` INT( 255 ) NOT NULL , `x2` INT( 255 ) NOT NULL , `y2` INT( 255 ) NOT NULL , `z2` INT( 255 ) NOT NULL)";
						if (UseMySQL) {
							plugin.console.warning("Created table " + SQL_area_table);
							query = "CREATE TABLE IF NOT EXISTS `" + SQL_area_table + "` (`id` INT( 255 ) NOT NULL AUTO_INCREMENT PRIMARY KEY , `areaname` VARCHAR( 255 ) NOT NULL , `world` VARCHAR( 255 ) NOT NULL , `x1` INT( 255 ) NOT NULL , `y1` INT( 255 ) NOT NULL , `z1` INT( 255 ) NOT NULL , `x2` INT( 255 ) NOT NULL , `y2` INT( 255 ) NOT NULL , `z2` INT( 255 ) NOT NULL)";
						}
						stmt.execute(query);
					}
					if (!checkLoan && plugin.LoanSystem.LoanActive) {
						//LOAN TABLE
						String query = "CREATE TABLE IF NOT EXISTS `" + SQL_loan_table + "` (`player` VARCHAR( 255 ) NOT NULL, `totalamount` DOUBLE( 255,2 ) NOT NULL, `remaining` DOUBLE( 255,2 ) NOT NULL, `timepayment` INT( 255 ) NOT NULL, `timeleft` INT( 255 ) NOT NULL, `part` INT( 255 ) NOT NULL, `parts` INT( 255 ) NOT NULL)";
						if (UseMySQL) {
							plugin.console.warning("Created table " + SQL_loan_table);
							query = "CREATE TABLE IF NOT EXISTS `" + SQL_loan_table + "` (`id` INT( 255 ) NOT NULL AUTO_INCREMENT PRIMARY KEY, `player` VARCHAR( 255 ) NOT NULL, `totalamount` DOUBLE( 255,2 ) NOT NULL, `timeleft` INT( 255 ) NOT NULL, `part` INT( 255 ) NOT NULL, `parts` INT( 255 ) NOT NULL)";
						}
						stmt.execute(query);
					}
					if (!checkTransaction && Transactions) {
						//TRANSACTION TABLE
						String query = "CREATE TABLE IF NOT EXISTS `" + SQL_transaction_table + "` (`player` VARCHAR( 255 ) NOT NULL, `account` VARCHAR( 255 ) NULL, `type` INT( 255 ) NOT NULL, `amount` DOUBLE( 255,2 ) NULL, `time` INT( 255 ) NOT NULL)";
						if (UseMySQL) {
							plugin.console.warning("Created table " + SQL_transaction_table);
							query = "CREATE TABLE IF NOT EXISTS `" + SQL_transaction_table + "` (`id` INT( 255 ) NOT NULL AUTO_INCREMENT PRIMARY KEY , `player` VARCHAR( 255 ) NOT NULL, `account` VARCHAR( 255 ) NULL, `type` INT( 255 ) NOT NULL, `amount` DOUBLE( 255,2 ) NULL, `time` INT( 255 ) NOT NULL)";
						}
						stmt.execute(query);
					}
					/*
					if (!checkBanks && MultiBanks) {
						//BANKS TABLE
						String query = "";
						if (UseMySQL) {
							plugin.console.warning("Created table " + SQL_banks_table);
							query = "";
						}
						stmt.execute(query);
					}
					*/
					//Run upgrades of SQL tables
					new Upgrade(plugin, UseMySQL);
				} catch (SQLException e3) {
					if (!checkAccount) {
						plugin.console.warning("Failed to find and create table " + SQL_account_table);
					}
					if (!checkArea && Areas) {
						plugin.console.warning("Failed to find and create table " + SQL_area_table);
						plugin.console.info("Disabled areas!");
						Areas = false;
					}
					if (!checkLoan && plugin.LoanSystem.LoanActive) {
						plugin.console.warning("Failed to find and create table " + SQL_loan_table);
						plugin.console.info("Disabled loans!");
						plugin.LoanSystem.LoanActive = false;
						if (plugin.LoanSystem.running) {
							plugin.LoanSystem.shutdownRunner();
						}
					}
					if (!checkTransaction && Transactions) {
						plugin.console.warning("Failed to find and create table " + SQL_transaction_table);
						plugin.console.info("Disabled transactions!");
						Transactions = false;
					}
					/*
					if (!checkBanks && MultiBanks) {
						plugin.console.warning("Failed to find and create table " + SQL_banks_table);
						plugin.console.info("Disabled multiple banks!");
						MultiBanks = false;
					}
					*/
					plugin.console.warning(e3.toString());
					plugin.console.info("Shuting down");
					plugin.getServer().getPluginManager().disablePlugin(plugin);
					return false;
				}
			} catch (SQLException e2) {
				if (UseMySQL) {
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
			if (UseMySQL) {
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
			InputStream input = BankAccount.class.getResourceAsStream("/config/" + name);
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
