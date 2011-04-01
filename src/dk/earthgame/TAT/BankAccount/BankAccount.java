package dk.earthgame.TAT.BankAccount;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.bukkit.util.config.Configuration;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;
import com.nijikokun.bukkit.Permissions.Permissions;

import dk.earthgame.TAT.BankAccount.System.PermissionNodes;
import dk.earthgame.TAT.BankAccount.System.TransactionTypes;
import dk.earthgame.TAT.BankAccount.System.UserSaves;

import org.anjocaido.groupmanager.GroupManager;

/**
 * BankAccount for Bukkit
 * 
 * @author TAT
 */
public class BankAccount extends JavaPlugin {
	protected final Plugin thisPlugin = this;
	//System
	protected final Logger log = Logger.getLogger("Minecraft");
	private PluginDescriptionFile pdfFile;
	private File myFolder;
	public Configuration config;
	private int interestTime;
	private double interestAmount;
	private int interestJobId;
	private int checkJobId;
	private HashMap<String,UserSaves> UserSaves = new HashMap<String,UserSaves>();
	private int areaWandId;
	LoanSystem LoanSystem = new LoanSystem(this);
	//MySQL
	private boolean UseMySQL = false;
	private String MySQL_host;
	private String MySQL_port;
	private String MySQL_username;
	private String MySQL_password;
	private String MySQL_database;
	//SQL
	public Connection con;
	public java.sql.Statement stmt;
	String SQL_account_table;
	String SQL_area_table;
	String SQL_loan_table;
	String SQL_transaction_table;
	//iConomy
	public iConomy iConomy;
	//Permissions
	private boolean UseOP;
	public Permissions Permissions = null;
	private boolean UsePermissions;
	public GroupManager GroupManager = null;
	private boolean UseGroupManager;
	public boolean Global;
	public boolean SuperAdmins;
	//Transaction
	private boolean Transactions;

	//#########################################################################//
	
	//SYSTEM
	
	void consoleLog(String string) {
		log.info(pdfFile.getName() + ": " + string);
	}

	void consoleWarning(String string) {
		log.warning(pdfFile.getName() + ": " + string);
	}
	
	//System
	boolean checkPermission(Player player,PermissionNodes node) {
		if (player != null) {
			if (UsePermissions) {
				if (com.nijikokun.bukkit.Permissions.Permissions.Security.permission(player, node.getNode())) {
					return true;
				}
			}
			if (UseGroupManager) {
				if (GroupManager.getWorldsHolder().getWorldPermissions(player).has(player, node.getNode())) {
					return true;
				}
			}
			if (UseOP) {
				if (player.isOp()) {
					return true;
				}
			}
			if (node != PermissionNodes.ACCESS) {
				if (checkPermission(player,PermissionNodes.ADMIN) || checkPermission(player,PermissionNodes.EXTENDED)) {
					return true;
				}
				if (node == PermissionNodes.OPEN || node == PermissionNodes.DEPOSIT || node == PermissionNodes.WITHDRAW) {
					if (checkPermission(player, PermissionNodes.BASIC))
						return true;
				}
			}
		}
		return false;
	}
	
	//Open function
	public boolean playerPermission(Player player,PermissionNodes node) {
		return checkPermission(player, node);
	}
	
	public void onEnable() {
		// Register our events
		getCommand("account").setExecutor(new BankAccountCommandExecutor(this));
		getCommand("account").setUsage("/account help - Show help to BankAccount");
		
		PlayerListener rightClickListener = new PlayerListener() {
			@Override
			public void onPlayerInteract(PlayerInteractEvent event) {
				if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
					UserSaves mySave = getSaved(event.getPlayer().getName());
					if (event.getPlayer().getItemInHand().getTypeId() == areaWandId && mySave.selecting) {
						World world = event.getClickedBlock().getWorld();
						Location pos = event.getClickedBlock().getLocation();
						world.getBlockAt(pos);
						if (mySave.setPosition(pos) == 2) {
							event.getPlayer().sendMessage("ATM: Area selected, to confirm: /account setarea <areaname>");
						} else {
							event.getPlayer().sendMessage("ATM: Position 1 selected, please select position 2");
						}
					}
				}
			}
		};
		
		ServerListener pluginListener = new ServerListener() {
			Plugin checkPlugin(String pluginname) {
				return getServer().getPluginManager().getPlugin(pluginname);
			}
			
			@Override
			public void onPluginEnable(PluginEnableEvent event) {
				String plugin = event.getPlugin().getDescription().getName();
				if (iConomy == null && plugin.equalsIgnoreCase("iConomy")) {
					Plugin test = checkPlugin("iConomy");
					if (test != null) {
						iConomy = (iConomy)test;
						if (LoanSystem.LoanActive && !LoanSystem.running) {
							LoanSystem.startupRunner();
						}
						if (interestTime > 0) {
							if (interestJobId > 0) {
								getServer().getScheduler().cancelTask(interestJobId);
							}
							interestJobId = getServer().getScheduler().scheduleSyncRepeatingTask(thisPlugin, new Runnable() {
								public void run() {
									consoleLog("Running interest system");
																		
									Double totalGiven = 0.00;
									try {
										if (UseMySQL) {
											//MySQL
											ResultSet accounts = stmt.executeQuery("SELECT `id`, `amount` FROM `" + SQL_account_table + "`");
											while (accounts.next()) {
												Double accountbalance = accounts.getDouble("amount");
												totalGiven += accountbalance*(interestAmount/100);
												accountbalance *= 1+(interestAmount/100);
												accounts.updateDouble("amount", accountbalance);
												accounts.updateRow();
											}
											accounts.close();
										} else {
											//SQLite
											PreparedStatement prep = con.prepareStatement("UPDATE `" + SQL_account_table + "` SET `amount` = ? WHERE `accountname` = ?");
											ResultSet accounts = stmt.executeQuery("SELECT `accountname`, `amount` FROM `" + SQL_account_table + "`");
											while (accounts.next()) {
												String accountname = accounts.getString("accountname");
												Double accountbalance = accounts.getDouble("amount");
												totalGiven += accountbalance*(interestAmount/100);
												accountbalance *= 1+(interestAmount/100);
												prep.setDouble(1, accountbalance);
												prep.setString(2, accountname);
												prep.addBatch();
											}
											accounts.close();
											con.setAutoCommit(false);
											prep.executeBatch();
											con.commit();
											con.setAutoCommit(true);
										}
									} catch (SQLException e) {
										consoleWarning("Couldn't execute interest");
										consoleLog(e.toString());
									}
									consoleLog("Total given " + com.nijiko.coelho.iConomy.iConomy.getBank().format(totalGiven) + " " + com.nijiko.coelho.iConomy.iConomy.getBank().getCurrency() + " in interest");
								}
							}, interestTime*20*60, interestTime*20*60);
							consoleLog("Running interest every " + interestTime + " minutes by " + interestAmount + "%");
						}
						consoleLog("Established connection with " + plugin + "!");
					}
				}
				if (Permissions == null && plugin.equalsIgnoreCase("Permissions")) {
					Plugin test = checkPlugin("Permissions");
					if (test != null) {
						Permissions = (Permissions)test;
						consoleLog("Established connection with " + plugin + "!");
					}
				}
				if (GroupManager == null && plugin.equalsIgnoreCase("GroupManager")) {
					Plugin test = checkPlugin("GroupManager");
					if (test != null) {
						GroupManager = (GroupManager)test;
						consoleLog("Established connection with " + plugin + "!");
						if (checkJobId > 0) {
							getServer().getScheduler().cancelTask(checkJobId);
						}
					}
				}
			}

			@Override
			public void onPluginDisable(PluginDisableEvent event) {
				String plugin = event.getPlugin().getDescription().getName();
				if (iConomy != null && plugin.equalsIgnoreCase("iConomy")) {
					iConomy = null;
					if (LoanSystem.LoanActive && LoanSystem.running) {
						LoanSystem.shutdownRunner();
					}
					consoleWarning("Lost connection with " + plugin + "!");
					consoleWarning("Stopping BankAccount - Reason: Missing iConomy plugin!");
					getServer().getPluginManager().disablePlugin(thisPlugin);
				}
				if (Permissions != null && plugin.equalsIgnoreCase("Permissions")) {
					Permissions = null;
					consoleWarning("Lost connection with " + plugin + "!");
				}
				if (GroupManager != null && plugin.equalsIgnoreCase("GroupManager")) {
					GroupManager = null;
					consoleWarning("Lost connection with " + plugin + "!");
				}
			}
		};
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Type.PLAYER_INTERACT, rightClickListener, Priority.Normal, this);
		pm.registerEvent(Type.PLUGIN_ENABLE, pluginListener, Priority.Low, this);
		pm.registerEvent(Type.PLUGIN_DISABLE, pluginListener, Priority.Low, this);
		
		pdfFile = this.getDescription();
		log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );

		myFolder = getDataFolder();
		if (!myFolder.exists()) {
			consoleLog("Config folder missing, creating...");
			myFolder.mkdir();
			consoleLog("Folder created");
		}
		
		checkJobId = this.getServer().getScheduler().scheduleSyncDelayedTask(thisPlugin, new Runnable() {
			public void run() {
				if (iConomy == null) {
					consoleWarning("Stopping BankAccount - Reason: Missing iConomy plugin!");
					getServer().getPluginManager().disablePlugin(thisPlugin);
					checkJobId = 0;
				}
			}
		}, 20*60);

		createDefaultConfiguration();
		loadConfiguration();
	}

	public void onDisable() {
		if (interestJobId > 0) {
			this.getServer().getScheduler().cancelTask(interestJobId);
		}
		interestJobId = 0;
		getCommand("account").setExecutor(new BankAccountDisabled(this));
		getCommand("account").setUsage(ChatColor.RED + "BankAccount is disabled");
		log.info(pdfFile.getName() + " is disabled!" );
	}

	public void addTransaction(String player, String account, TransactionTypes type, Double amount) {
		if (Transactions) {
			try {
				int time = Math.round(new Date().getTime()/1000);
				stmt.executeUpdate("INSERT INTO `" + SQL_transaction_table + "` (`player`,`account`,`type`,`amount`,`time`) VALUES ('" + player + "','" + account + "','" + type.get() + "','" + amount + "','" + time +"')");
			} catch(SQLException e) {
				if (!e.getMessage().equalsIgnoreCase(null))
					consoleWarning("Error #16-2: " + e.getMessage());
				else
					consoleWarning("Error #16-1: " + e.getErrorCode() + " - " + e.getSQLState());
			}
		}
	}
	
	//CONFIGURATION AND USERSAVES
	
	UserSaves getSaved(String player) {
		if (UserSaves.containsKey(player)) {
			return UserSaves.get(player);
		}
		
		UserSaves save = new UserSaves();
		UserSaves.put(player, save);
		return save;
	}
	
	@SuppressWarnings("unchecked")
	private boolean loadConfiguration() {
		config = new Configuration(new File(this.getDataFolder(), "config.yml"));
		config.load();
		//MySQL
		UseMySQL = config.getBoolean("UseMySQL", false);
		MySQL_host = config.getString("MySQL-info.Host","localhost");
		MySQL_port = config.getString("MySQL-info.Port","3306");
		MySQL_username = config.getString("MySQL-info.User","root");
		MySQL_password = config.getString("MySQL-info.Pass","");
		MySQL_database = config.getString("MySQL-info.Database","minecraft");
		//SQL
		SQL_account_table = config.getString("SQL-account-table","bankaccounts");
		SQL_area_table = config.getString("SQL-area-table","bankareas");
		SQL_loan_table = config.getString("SQL-loan-table","bankloans");
		SQL_transaction_table = config.getString("SQL-transactions-table","banktransactions");
		//Permissions
		SuperAdmins = config.getBoolean("SuperAdmins", false);
		UseOP = config.getBoolean("UseOP",true);
		UsePermissions = config.getBoolean("UsePermissions",false);
		UseGroupManager = config.getBoolean("UseGroupManager",false);
		//Interest
		interestAmount = config.getDouble("Interest.Amount", 0);
		interestTime = config.getInt("Interest.Time", 0);
		//Area
		Global = config.getBoolean("Global",true);
		areaWandId = config.getInt("AreaWandid",339);
		//Loan
		LoanSystem.LoanActive = config.getBoolean("Loan.Active", false);
		LoanSystem.Fixed_rate = config.getDouble("Loan.Fixed-rate", 0.00);
		LoanSystem.Rates = (Map<Double, Double>)config.getProperty("Loan.Rate");
		LoanSystem.Max_amount = config.getDouble("Loan.Max-amount", 200.00);
		LoanSystem.PaymentTime = config.getInt("Loan.Payment-time", 60);
		LoanSystem.PaymentParts = config.getInt("Loan.Payment-parts", 3);
		//Other
		Transactions = config.getBoolean("Transactions", false);
		
		consoleLog("Properties Loaded");
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
				con = DriverManager.getConnection("jdbc:sqlite:" + myFolder.getAbsolutePath() + "/BankAccount.db");
			}
			try {
				if (UseMySQL) {
					stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);
					consoleLog("Connected to MySQL");
				} else {
					stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
					consoleLog("Connected to SQLite");
				}
				try {
					boolean checkAccount = false;
					boolean checkArea = false;
					boolean checkLoan = false;
					boolean checkTransaction = false;
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
						}
					}
					if (!checkAccount) {
						//ACCOUNT TABLE
						String query = "CREATE TABLE IF NOT EXISTS `" + SQL_account_table + "` (`accountname` VARCHAR( 255 ) NOT NULL , `players` LONGTEXT NOT NULL, `password` VARCHAR( 255 ) NULL DEFAULT '', `amount` DOUBLE( 255,2 ) NOT NULL DEFAULT '0')";
						if (UseMySQL) {
							consoleWarning("Created table " + SQL_account_table);
							query = "CREATE TABLE IF NOT EXISTS `" + SQL_account_table + "` (`id` INT( 255 ) NOT NULL AUTO_INCREMENT PRIMARY KEY , `accountname` VARCHAR( 255 ) NOT NULL , `players` LONGTEXT NOT NULL, `password` VARCHAR( 255 ) NULL DEFAULT '', `amount` DOUBLE( 255,2 ) NOT NULL DEFAULT '0')";
						}
						stmt.execute(query);
					}
					if (!checkArea) {
						//AREA TABLE
						String query = "CREATE TABLE IF NOT EXISTS `" + SQL_area_table + "` (`areaname` VARCHAR( 255 ) NOT NULL , `world` VARCHAR( 255 ) NOT NULL , `x1` INT( 255 ) NOT NULL , `y1` INT( 255 ) NOT NULL , `z1` INT( 255 ) NOT NULL , `x2` INT( 255 ) NOT NULL , `y2` INT( 255 ) NOT NULL , `z2` INT( 255 ) NOT NULL)";
						if (UseMySQL) {
							consoleWarning("Created table " + SQL_area_table);
							query = "CREATE TABLE IF NOT EXISTS `" + SQL_area_table + "` (`id` INT( 255 ) NOT NULL AUTO_INCREMENT PRIMARY KEY , `areaname` VARCHAR( 255 ) NOT NULL , `world` VARCHAR( 255 ) NOT NULL , `x1` INT( 255 ) NOT NULL , `y1` INT( 255 ) NOT NULL , `z1` INT( 255 ) NOT NULL , `x2` INT( 255 ) NOT NULL , `y2` INT( 255 ) NOT NULL , `z2` INT( 255 ) NOT NULL)";
						}
						stmt.execute(query);
					}
					if (!checkLoan) {
						//LOAN TABLE
						String query = "CREATE TABLE IF NOT EXISTS `" + SQL_loan_table + "` (`player` VARCHAR( 255 ) NOT NULL, `totalamount` DOUBLE( 255,2 ) NOT NULL, `remaining` DOUBLE( 255,2 ) NOT NULL, `timepayment` INT( 255 ) NOT NULL, `timeleft` INT( 255 ) NOT NULL, `part` INT( 255 ) NOT NULL, `parts` INT( 255 ) NOT NULL)";
						if (UseMySQL) {
							consoleWarning("Created table " + SQL_loan_table);
							query = "CREATE TABLE IF NOT EXISTS `" + SQL_loan_table + "` (`id` INT( 255 ) NOT NULL AUTO_INCREMENT PRIMARY KEY, `player` VARCHAR( 255 ) NOT NULL, `totalamount` DOUBLE( 255,2 ) NOT NULL, `timeleft` INT( 255 ) NOT NULL, `part` INT( 255 ) NOT NULL, `parts` INT( 255 ) NOT NULL)";
						}
						stmt.execute(query);
					}
					if (!checkTransaction) {
						//TRANSACTION TABLE
						String query = "CREATE TABLE IF NOT EXISTS `" + SQL_transaction_table + "` (`player` VARCHAR( 255 ) NOT NULL, `account` VARCHAR( 255 ) NULL, `type` INT( 255 ) NOT NULL, `amount` DOUBLE( 255,2 ) NULL, `time` INT( 255 ) NOT NULL)";
						if (UseMySQL) {
							consoleWarning("Created table " + SQL_transaction_table);
							query = "CREATE TABLE IF NOT EXISTS `" + SQL_transaction_table + "` (`id` INT( 255 ) NOT NULL AUTO_INCREMENT PRIMARY KEY , `player` VARCHAR( 255 ) NOT NULL, `account` VARCHAR( 255 ) NULL, `type` INT( 255 ) NOT NULL, `amount` DOUBLE( 255,2 ) NULL, `time` INT( 255 ) NOT NULL)";
						}
						stmt.execute(query);
					}
					
					//Upgrade 0.3c
					File Upgrade03c = new File(this.getDataFolder(), "SQLUpgrade03c");
					if (Upgrade03c.exists()) {
						try {
							if (UseMySQL) {
								String query = "ALTER TABLE `" + SQL_account_table + "` CHANGE  `amount`  `amount` DOUBLE( 255, 2 ) NOT NULL DEFAULT  '0.00'";
								stmt.execute(query);
								consoleLog("Tables upgraded to v.0.3c");
								if (Upgrade03c.delete()) {
									consoleLog("SQLUpgrade03c deleted");
								} else {
									consoleWarning("SQLUpgrade03c could not be deleted, please remove it yourself");
								}
							} else {
								consoleLog("SQLUpgrade03c is not for SQLite");
								if (Upgrade03c.delete()) {
									consoleLog("SQLUpgrade03c deleted");
								} else {
									consoleWarning("SQLUpgrade03c could not be deleted, please remove it yourself");
								}
							}
						} catch (SQLException e4) {
							consoleWarning("Could not upgrade tables to v.0.3c");
							consoleWarning(e4.toString());
						}
					}
				} catch (SQLException e3) {
					consoleWarning("Failed to find and create table " + SQL_account_table);
					consoleWarning("Failed to find and create table " + SQL_area_table);
					consoleWarning(e3.toString());
					consoleLog("Shuting down");
					this.getServer().getPluginManager().disablePlugin(this);
					return false;
				}
			} catch (SQLException e2) {
				if (UseMySQL) {
					consoleWarning("Failed to connect to MySQL");
				} else {
					consoleWarning("Failed to connect to SQLite");
				}
				consoleWarning(e2.toString());
				consoleLog("Shuting down");
				this.getServer().getPluginManager().disablePlugin(this);
				return false;
			}
		} catch (SQLException e1) {
			if (UseMySQL) {
				consoleWarning("Failed to connect to MySQL");
			} else {
				consoleWarning("Failed to connect to SQLite");
			}
			consoleWarning(e1.toString());
			consoleLog("Shuting down");
			this.getServer().getPluginManager().disablePlugin(this);
			return false;
		}
		return true;
	}
	
	private void createDefaultConfiguration() {
		String name = "config.yml";
		File actual = new File(getDataFolder(), name);
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
					
					consoleLog("Default config file created!");
				} catch (IOException e) {
					e.printStackTrace();
					consoleLog("Error creating config file!");
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
			consoleLog("Config file found!");
		}
	}
	
	//ATM / ACCOUNTS
	
	public boolean accountExists(String accountname) {
		ResultSet rs;
		int id = 0;
		try {
			if (UseMySQL) {
				rs = stmt.executeQuery("SELECT `id` FROM `" + SQL_account_table + "` WHERE `accountname` = '" + accountname + "'");
			} else {
				rs = stmt.executeQuery("SELECT `rowid` FROM `" + SQL_account_table + "` WHERE `accountname` = '" + accountname + "'");
			}
			try {
				while (rs.next()) {
					if (UseMySQL) {
						id = rs.getInt("id");
					} else {
						id = rs.getInt("rowid");
					}
				}
			} catch (SQLException e1) {
				if (!e1.getMessage().equalsIgnoreCase(null))
					consoleWarning("Error #01-4: " + e1.getMessage());
				else
					consoleWarning("Error #01-3: " + e1.getErrorCode() + " - " + e1.getSQLState());
			}
		} catch (SQLException e) {
			if (!e.getMessage().equalsIgnoreCase(null))
				consoleWarning("Error #01-2: " + e.getMessage());
			else
				consoleWarning("Error #01-1: " + e.getErrorCode() + " - " + e.getSQLState());
		}
		if (id > 0) {
			return true;
		}
		return false;
	}
	
	public boolean addAccount(String accountname,String players) {
		try {
			stmt.executeUpdate("INSERT INTO `" + SQL_account_table + "` (`accountname`,`players`) VALUES ('" + accountname + "','" + players + "')");
			return true;
		} catch(SQLException e) {
			if (!e.getMessage().equalsIgnoreCase(null))
				consoleWarning("Error #02-2: " + e.getMessage());
			else
				consoleWarning("Error #02-1: " + e.getErrorCode() + " - " + e.getSQLState());
		}
		return false;
	}
	
	public boolean accessAccount(String accountname,String player) {
		if (SuperAdmins) {
			return true;
		}
		try {
			ResultSet rs;
			rs = stmt.executeQuery("SELECT `players` FROM `" + SQL_account_table + "` WHERE `accountname` = '" + accountname + "'");
			while(rs.next()) {
				String[] players = rs.getString("players").split(";");
				for (String p : players) {
					if (p.equalsIgnoreCase(player)) {
						return true;
					}
				}
			}
		} catch(SQLException e1) {
			if (!e1.getMessage().equalsIgnoreCase(null))
				consoleWarning("Error #03-3: " + e1.getMessage());
			else
				consoleWarning("Error #03-2: " + e1.getErrorCode() + " - " + e1.getSQLState());
		} catch(Exception e) {
			consoleWarning("Error #03-1: " + e.toString());
		}
		return false;
	}

	public boolean addUser(String accountname,String player) {
		try {
			String newPlayers = player;
			ResultSet rs;
			rs = stmt.executeQuery("SELECT `players` FROM `" + SQL_account_table + "` WHERE `accountname` = '" + accountname + "'");
			while(rs.next()) {
				String[] players = rs.getString("players").split(";");
				for (String p : players) {
					newPlayers += ";" + p;
				}
			}
			try {
				stmt.executeUpdate("UPDATE `" + SQL_account_table + "` SET `players` = '" + newPlayers + "' WHERE `accountname` = '" + accountname + "'");
				return true;
			} catch(SQLException e) {
				if (!e.getMessage().equalsIgnoreCase(null))
					consoleWarning("Error #04-4: " + e.getMessage());
				else
					consoleWarning("Error #04-3: " + e.getErrorCode() + " - " + e.getSQLState());
			}
		} catch(SQLException e) {
			if (!e.getMessage().equalsIgnoreCase(null))
				consoleWarning("Error #04-2: " + e.getMessage());
			else
				consoleWarning("Error #04-1: " + e.getErrorCode() + " - " + e.getSQLState());
		}
		return false;
	}
	
	public boolean removeUser(String accountname,String player) {
		try {
			String newPlayers = "";
			ResultSet rs;
			rs = stmt.executeQuery("SELECT `players` FROM `" + SQL_account_table + "` WHERE `accountname` = '" + accountname + "'");
			while(rs.next()) {
				String[] players = rs.getString("players").split(";");
				for (String p : players) {
					if (!p.equalsIgnoreCase(player)) {
						if (!newPlayers.equalsIgnoreCase("")) {
							newPlayers += ";";
						}
						newPlayers += p;	
					}
				}
			}
			try {
				stmt.executeUpdate("UPDATE `" + SQL_account_table + "` SET `players` = '" + newPlayers + "' WHERE `accountname` = '" + accountname + "'");
				return true;
			} catch(SQLException e) {
				if (!e.getMessage().equalsIgnoreCase(null))
					consoleWarning("Error #05-4: " + e.getMessage());
				else
					consoleWarning("Error #05-3: " + e.getErrorCode() + " - " + e.getSQLState());
			}
		} catch(SQLException e) {
			if (!e.getMessage().equalsIgnoreCase(null))
				consoleWarning("Error #05-2: " + e.getMessage());
			else
				consoleWarning("Error #05-1: " + e.getErrorCode() + " - " + e.getSQLState());
		}
		return false;
	}

	public boolean setPassword(String accountname,String password) {
		try {
			stmt.executeUpdate("UPDATE `" + SQL_account_table + "` SET `password` = '" + password + "' WHERE `accountname` = '" + accountname + "'");
			return true;
		} catch(SQLException e) {
			if (!e.getMessage().equalsIgnoreCase(null))
				consoleWarning("Error #06-2: " + e.getMessage());
			else
				consoleWarning("Error #06-1: " + e.getErrorCode() + " - " + e.getSQLState());
		}
		return false;
	}

	public boolean ATM(String accountname,String player,String type,Double amount,String password) {
		try {
			double account = getBalance(accountname);
			Account iConomyAccount = com.nijiko.coelho.iConomy.iConomy.getBank().getAccount(player);
			if (type == "deposit") {
				if (iConomyAccount.hasEnough(amount)) {
					account += amount;
					stmt.executeUpdate("UPDATE `" + SQL_account_table + "` SET `amount` = '" + account + "' WHERE `accountname` = '" + accountname + "'");
					iConomyAccount.subtract(amount);
					return true;
				} else {
					return false;
				}
			} else if (type == "withdraw") {
				if (passwordCheck(accountname, password)) {
					if ((account - amount) >= 0) {
						account -= amount;
						stmt.executeUpdate("UPDATE `" + SQL_account_table + "` SET `amount` = '" + account + "' WHERE `accountname` = '" + accountname + "'");
						iConomyAccount.add(amount);
						return true;
					} else {
						return false;
					}
				} else {
					return false;
				}
			} else if (type == "transfer") {
				if (passwordCheck(accountname, password)) {
					//Player = reciever account
					double reciever_account = getBalance(player);
					if ((account - amount) >= 0) {
						account -= amount;
						reciever_account += amount;
						stmt.executeUpdate("UPDATE `" + SQL_account_table + "` SET `amount` = '" + account + "' WHERE `accountname` = '" + accountname + "'");
						stmt.executeUpdate("UPDATE `" + SQL_account_table + "` SET `amount` = '" + reciever_account + "' WHERE `accountname` = '" + player + "'");
						return true;
					} else {
						return false;
					}
				} else {
					return false;
				}
			}
		} catch(SQLException e1) {
			if (!e1.getMessage().equalsIgnoreCase(null))
				consoleWarning("Error #07-3: " + e1.getMessage());
			else
				consoleWarning("Error #07-2: " + e1.getErrorCode() + " - " + e1.getSQLState());
		} catch(Exception e) {
			consoleWarning("Error #07-1: " + e.toString());
		}
		return false;
	}
	
	public boolean closeAccount(String accountname,String player,String password) {
		if (passwordCheck(accountname, password)) {
			try {
				Account iConomyAccount = com.nijiko.coelho.iConomy.iConomy.getBank().getAccount(player);
				double accountBalance = getBalance(accountname);
				stmt.executeUpdate("DELETE FROM `" + SQL_account_table + "` WHERE `accountname` = '" + accountname + "'");
				iConomyAccount.add(accountBalance);
				return true;
			} catch(SQLException e) {
				if (!e.getMessage().equalsIgnoreCase(null))
					consoleWarning("Error #08-3: " + e.getMessage());
				else
					consoleWarning("Error #08-2: " + e.getErrorCode() + " - " + e.getSQLState());
			} catch(Exception e) {
				consoleWarning("Error #08-1: " + e.toString());
			}
			return false;
		} else {
			return false;
		}
	}
	
	public String getUsers(String accountname) {
		try {
			String output = "";
			ResultSet rs;
			rs = stmt.executeQuery("SELECT `players` FROM `" + SQL_account_table + "` WHERE `accountname` = '" + accountname + "'");
			while(rs.next()) {
				String[] players = rs.getString("players").split(";");
				for (String p : players) {
					if (!output.equalsIgnoreCase("")) {
						output += ", ";
					}
					output += p;	
				}
			}
			return output;
		} catch (SQLException e1) {
			if (!e1.getMessage().equalsIgnoreCase(null))
				consoleWarning("Error #09-3: " + e1.getMessage());
			else
				consoleWarning("Error #09-2: " + e1.getErrorCode() + " - " + e1.getSQLState());
		} catch(Exception e) {
			consoleWarning("Error #09-1: " + e.toString());
		}
		return "Error loading players";
	}
	
	public double getBalance(String accountname) {
		try {
			ResultSet rs;
			rs = stmt.executeQuery("SELECT `amount` FROM `" + SQL_account_table + "` WHERE `accountname` = '" + accountname +"'");
			while (rs.next()) {
				return rs.getDouble("amount");
			}
		} catch (SQLException e1) {
			if (!e1.getMessage().equalsIgnoreCase(null))
				consoleWarning("Error #10-3: " + e1.getMessage());
			else
				consoleWarning("Error #10-2: " + e1.getErrorCode() + " - " + e1.getSQLState());
		} catch(Exception e) {
			consoleWarning("Error #10-1: " + e.toString());
		}
		return 0;
	}
	
	public boolean setBalance(double balance,String accountname) {
		try {
			stmt.executeUpdate("UPDATE `" + SQL_account_table + "` SET `amount` = '" + balance + "' WHERE `accountname` = '" + accountname + "'");
			return true;
		} catch (SQLException e) {
			if (!e.getMessage().equalsIgnoreCase(null))
				consoleWarning("Error #17-2: " + e.getMessage());
			else
				consoleWarning("Error #17-1: " + e.getErrorCode() + " - " + e.getSQLState());
		}
		return false;
	}
	
	public boolean add(double amount,String accountname) {
		double temp = getBalance(accountname);
		temp += amount;
		try {
			stmt.executeUpdate("UPDATE `" + SQL_account_table + "` SET `amount` = '" + temp + "' WHERE `accountname` = '" + accountname + "'");
			return true;
		} catch (SQLException e) {
			if (!e.getMessage().equalsIgnoreCase(null))
				consoleWarning("Error #18-2: " + e.getMessage());
			else
				consoleWarning("Error #18-1: " + e.getErrorCode() + " - " + e.getSQLState());
		}
		return false;
	}
	
	public boolean subtract(double amount,String accountname) {
		double temp = getBalance(accountname);
		temp -= amount;
		try {
			stmt.executeUpdate("UPDATE `" + SQL_account_table + "` SET `amount` = '" + temp + "' WHERE `accountname` = '" + accountname + "'");
			return true;
		} catch (SQLException e) {
			if (!e.getMessage().equalsIgnoreCase(null))
				consoleWarning("Error #19-2: " + e.getMessage());
			else
				consoleWarning("Error #19-1: " + e.getErrorCode() + " - " + e.getSQLState());
		}
		return false;
	}
	
	//AREAS
	
	public boolean areaExists(String name) {
		ResultSet rs;
		int id = 0;
		try {
			if (UseMySQL) {
				rs = stmt.executeQuery("SELECT `id` FROM `" + SQL_area_table + "` WHERE `areaname` = '" + name + "'");
			} else {
				rs = stmt.executeQuery("SELECT `rowid` FROM `" + SQL_area_table + "` WHERE `areaname` = '" + name + "'");
			}
			try {
				while (rs.next()) {
					if (UseMySQL) {
						id = rs.getInt("id");
					} else {
						id = rs.getInt("rowid");
					}
				}
			} catch (SQLException e1) {
				if (!e1.getMessage().equalsIgnoreCase(null))
					consoleWarning("Error #14-4: " + e1.getMessage());
				else
					consoleWarning("Error #14-3: " + e1.getErrorCode() + " - " + e1.getSQLState());
			}
		} catch (SQLException e) {
			if (!e.getMessage().equalsIgnoreCase(null))
				consoleWarning("Error #14-2: " + e.getMessage());
			else
				consoleWarning("Error #14-1: " + e.getErrorCode() + " - " + e.getSQLState());
		}
		if (id > 0) {
			return true;
		}
		return false;
	}
	
	public boolean inArea(String world,Location pos) {
		try {
			ResultSet rs = stmt.executeQuery("SELECT `x1`,`y1`,`z1`,`x2`,`y2`,`z2` FROM `" + SQL_area_table + "` WHERE `world` = '" + world + "'");
			while (rs.next()) {
				Vector min = new Vector(
					Math.min(rs.getInt("x1"), rs.getInt("x2")),
					Math.min(rs.getInt("y1"), rs.getInt("y2")),
					Math.min(rs.getInt("z1"), rs.getInt("z2"))
				);
				Vector max = new Vector(
					Math.max(rs.getInt("x1"), rs.getInt("x2")),
					Math.max(rs.getInt("y1"), rs.getInt("y2")),
					Math.max(rs.getInt("z1"), rs.getInt("z2"))
				);
				
				if (pos.getBlockX() >= min.getBlockX() && pos.getBlockX() <= max.getBlockX() &&
					pos.getBlockY() >= min.getBlockY() && pos.getBlockY() <= max.getBlockY() &&
					pos.getBlockZ() >= min.getBlockZ() && pos.getBlockZ() <= max.getBlockZ()) {
					return true;
				}
			}
		} catch(SQLException e) {
			if (!e.getMessage().equalsIgnoreCase(null))
				consoleWarning("Error #15-2: " + e.getMessage());
			else
				consoleWarning("Error #15-1: " + e.getErrorCode() + " - " + e.getSQLState());
		}
		return false;
	}
	
	public boolean setArea(String name,Location pos1,Location pos2,String world) {
		if (areaExists(name)) {
			return false;
		}
		try {
			stmt.executeUpdate("INSERT INTO `" + SQL_area_table + "` (`areaname`,`world`,`x1`, `y1`, `z1`, `x2`, `y2`, `z2`) VALUES ('" + name + "','" + world + "','" + pos1.getBlockX() + "','" + pos1.getBlockY() + "','" + pos1.getBlockZ() + "','" + pos2.getBlockX() + "','" + pos2.getBlockY() + "','" + pos2.getBlockZ() + "')");
			return true;
		} catch(SQLException e) {
			if (!e.getMessage().equalsIgnoreCase(null))
				consoleWarning("Error #12-2: " + e.getMessage());
			else
				consoleWarning("Error #12-1: " + e.getErrorCode() + " - " + e.getSQLState());
		}
		return false;
	}

	public boolean removeArea(String name) {
		try {
			stmt.executeUpdate("DELETE FROM `" + SQL_area_table + "` WHERE `areaname` = '" + name + "'");
			return true;
		} catch(SQLException e) {
			if (!e.getMessage().equalsIgnoreCase(null))
				consoleWarning("Error #13-3: " + e.getMessage());
			else
				consoleWarning("Error #13-2: " + e.getErrorCode() + " - " + e.getSQLState());
		} catch (Exception e) {
			consoleWarning("Error #13-1: " + e.toString());
		}
		return false;
	}
	
	//PASSWORDS
	
	private boolean passwordCheck(String accountname,String password) {
		String CryptPassword = passwordCrypt(password);
		try {
			ResultSet rs;
			rs = stmt.executeQuery("SELECT `password` FROM `" + SQL_account_table + "` WHERE `accountname` = '" + accountname + "'");
			while (rs.next()) {
				if (CryptPassword.equalsIgnoreCase(rs.getString("password"))) {
					return true;
				} else if (rs.getString("password").equalsIgnoreCase(password)) {
					return true;
				}
			}
		} catch(SQLException e) {
			if (!e.getMessage().equalsIgnoreCase(null))
				consoleWarning("Error #20-3: " + e.getMessage());
			else
				consoleWarning("Error #20-2: " + e.getErrorCode() + " - " + e.getSQLState());
		} catch(Exception e) {
			consoleWarning("Error #20-1: " + e.toString());
		}
		return false;
	}
	
	public String passwordCrypt(String password) {
		byte[] temp = password.getBytes();
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(temp);
			byte[] output = md.digest();
			password = bytesToHex(output);
			return password;
		} catch (NoSuchAlgorithmException e) {
			consoleWarning("Error #21-1: Couldn't crypt password");
			return "Error";
		}
	}

	private String bytesToHex(byte[] b) {
		char hexDigit[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
		StringBuffer buf = new StringBuffer();
		for (int j=0; j<b.length; j++) {
			buf.append(hexDigit[(b[j] >> 4) & 0x0f]);
			buf.append(hexDigit[b[j] & 0x0f]);
		}
		return buf.toString();
	}
}