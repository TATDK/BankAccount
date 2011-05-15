package dk.earthgame.TAT.BankAccount;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageByProjectileEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.bukkit.util.config.Configuration;
//import org.martin.bukkit.npclib.NPCManager;

import com.nijikokun.register.payment.Method;
import com.nijikokun.register.payment.Method.MethodAccount;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

import dk.earthgame.TAT.BankAccount.System.Password;
import dk.earthgame.TAT.BankAccount.System.PermissionNodes;
import dk.earthgame.TAT.BankAccount.System.TransactionTypes;
import dk.earthgame.TAT.BankAccount.System.Upgrade;
import dk.earthgame.TAT.BankAccount.System.UserSaves;

import org.anjocaido.groupmanager.GroupManager;

/**
 * BankAccount for Bukkit
 * 
 * @author TAT
 * @since 0.5
 */
public class BankAccount extends JavaPlugin {
	protected final Plugin thisPlugin = this;
	//System
	protected final Logger log = Logger.getLogger("Minecraft");
	private PluginDescriptionFile pdfFile;
	HashMap<String, Integer> fontWidth = new HashMap<String, Integer>();
	private File myFolder;
	Configuration config;
	private int interestTime;
	private double interestAmount;
	private int interestJobId;
	int checkJobId;
	private HashMap<String,UserSaves> UserSaves = new HashMap<String,UserSaves>();
	private int AreaWandId;
	private boolean MultiBanks;
	LoanSystem LoanSystem = new LoanSystem(this);
	Password PasswordSystem = new Password(this);
	BankAccountCommandExecutor cmdExecutor = new BankAccountCommandExecutor(this);
	BankAccountDisabled disabledExecutor = new BankAccountDisabled();
	BAPluginListener pluginListener = new BAPluginListener(this);
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
	public String SQL_account_table;
	String SQL_area_table;
	String SQL_loan_table;
	String SQL_transaction_table;
	String SQL_banks_table;
	//economy
	public Method Method = null;
	//Permissions
	private boolean UseOP;
	PermissionHandler Permissions = null;
	boolean UsePermissions;
	GroupManager GroupManager = null;
	boolean UseGroupManager;
	boolean Areas;
	private boolean SuperAdmins;
	boolean DepositAll;
	//Transaction
	public boolean Transactions;
	//Debug messages
	boolean Debug_Loan;
	boolean Debug_Interest;
	//NPC
	//NPCManager m = new NPCManager(this);

	//#########################################################################//
	
	//SYSTEM
	
	void loadFontWidth() {
		/*
		 * Widths is in pixels
		 * Got them from fontWidths.txt uploaded to the Bukkit forum by Edward Hand
		 * http://forums.bukkit.org/threads/formatting-plugin-output-text-into-columns.8481/
		 */
		fontWidth.clear();
		fontWidth.put(" ",4);
		fontWidth.put("!",2);
		fontWidth.put("\"",5);
		fontWidth.put("#",6);
		fontWidth.put("$",6);
		fontWidth.put("%",6);
		fontWidth.put("&",6);
		fontWidth.put("'",3);
		fontWidth.put("(",5);
		fontWidth.put(")",5);
		fontWidth.put("*",5);
		fontWidth.put("+",6);
		fontWidth.put(",",2);
		fontWidth.put("-",6);
		fontWidth.put(".",2);
		fontWidth.put("/",6);
		fontWidth.put("0",6);
		fontWidth.put("1",6);
		fontWidth.put("2",6);
		fontWidth.put("3",6);
		fontWidth.put("4",6);
		fontWidth.put("5",6);
		fontWidth.put("6",6);
		fontWidth.put("7",6);
		fontWidth.put("8",6);
		fontWidth.put("9",6);
		fontWidth.put(":",2);
		fontWidth.put(";",2);
		fontWidth.put("<",5);
		fontWidth.put("=",6);
		fontWidth.put(">",5);
		fontWidth.put("?",6);
		fontWidth.put("@",7);
		fontWidth.put("A",6);
		fontWidth.put("B",6);
		fontWidth.put("C",6);
		fontWidth.put("D",6);
		fontWidth.put("E",6);
		fontWidth.put("F",6);
		fontWidth.put("G",6);
		fontWidth.put("H",6);
		fontWidth.put("I",4);
		fontWidth.put("J",6);
		fontWidth.put("K",6);
		fontWidth.put("L",6);
		fontWidth.put("M",6);
		fontWidth.put("N",6);
		fontWidth.put("O",6);
		fontWidth.put("P",6);
		fontWidth.put("Q",6);
		fontWidth.put("R",6);
		fontWidth.put("S",6);
		fontWidth.put("T",6);
		fontWidth.put("U",6);
		fontWidth.put("V",6);
		fontWidth.put("W",6);
		fontWidth.put("X",6);
		fontWidth.put("Y",6);
		fontWidth.put("Z",6);
		fontWidth.put("_",6);
		fontWidth.put("'",3);
		fontWidth.put("a",6);
		fontWidth.put("b",6);
		fontWidth.put("c",6);
		fontWidth.put("d",6);
		fontWidth.put("e",6);
		fontWidth.put("f",5);
		fontWidth.put("g",6);
		fontWidth.put("h",6);
		fontWidth.put("i",2);
		fontWidth.put("j",6);
		fontWidth.put("k",5);
		fontWidth.put("l",3);
		fontWidth.put("m",6);
		fontWidth.put("n",6);
		fontWidth.put("o",6);
		fontWidth.put("p",6);
		fontWidth.put("q",6);
		fontWidth.put("r",6);
		fontWidth.put("s",6);
		fontWidth.put("t",4);
		fontWidth.put("u",6);
		fontWidth.put("v",6);
		fontWidth.put("w",6);
		fontWidth.put("x",6);
		fontWidth.put("y",6);
		fontWidth.put("z",6);
	}
	
	int stringWidth(String text) {
		if (fontWidth.isEmpty()) {
			return 0;
		}
		char[] chars = text.toCharArray();
		int width = 0;
		for (char current : chars) {
			if (fontWidth.containsKey(String.valueOf(current))) {
				width += fontWidth.get(String.valueOf(current));
			}
		}
		return width;
	}
	
	void foundEconomy() {
		if (LoanSystem.LoanActive && !LoanSystem.running) {
			LoanSystem.startupRunner();
		}
		if (interestTime > 0) {
			if (interestJobId > 0) {
				getServer().getScheduler().cancelTask(interestJobId);
			}
			interestJobId = getServer().getScheduler().scheduleSyncRepeatingTask(thisPlugin, new Runnable() {
				public void run() {
					if (Debug_Interest)
						consoleInfo("Running interest system");
														
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
						consoleInfo(e.toString());
					}
					if (Debug_Interest)
						consoleInfo("Total given " + Method.format(totalGiven) + " in interest");
				}
			}, interestTime*20*60, interestTime*20*60);
			consoleInfo("Running interest every " + interestTime + " minutes by " + interestAmount + "%");
		}
		consoleInfo("Established connection with economy!");
	}
	
	/**
	 * Output Info to log on behalf of BankAccount
	 * 
	 * @since 0.5
	 * @param message
	 */
	public void consoleInfo(String message) {
		log.info("[" + pdfFile.getName() + "] " + message);
	}

	/**
	 * Output Warning to log on behalf of BankAccount
	 * 
	 * @since 0.5
	 * @param message
	 */
	public void consoleWarning(String message) {
		log.warning("[" + pdfFile.getName() + "] " + message);
	}

	private boolean checkPermission(Player player,PermissionNodes node,boolean extraLookup) {
		if (player != null) {
			if (UsePermissions) {
				if (Permissions.has(player, node.getNode())) {
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
	 * @param player - The player
	 * @param node - The PermissionNode (dk.earthgame.TAT.BankAccount.System.PermissionNodes)
	 * @since 0.5
	 * @return boolean - If the player have the permission
	 */
	public boolean playerPermission(Player player,PermissionNodes node) {
		return checkPermission(player, node, false);
	}
	
	public void onEnable() {
		getDataFolder().mkdir();
		
		// Register our events
		getCommand("account").setExecutor(cmdExecutor);
		getCommand("account").setUsage("/account help - Show help to BankAccount");
		
		PlayerListener rightClickListener = new PlayerListener() {
			@Override
			public void onPlayerInteract(PlayerInteractEvent event) {
				if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
					UserSaves mySave = getSaved(event.getPlayer().getName());
					if (event.getPlayer().getItemInHand().getTypeId() == AreaWandId && mySave.isSelecting()) {
						Location pos = event.getClickedBlock().getLocation();
						if (mySave.setPosition(pos) == 2) {
							event.getPlayer().sendMessage("ATM: Area selected, to confirm: /account setarea <areaname>");
						} else {
							event.getPlayer().sendMessage("ATM: Position 1 selected, please select position 2");
						}
					}
				}
			}
		};
		
		EntityListener entityListener = new EntityListener() {
			private void check(EntityDamageByEntityEvent event) {
				Entity attacker = event.getDamager();
				Entity defender = event.getEntity();
				
				if (defender instanceof Player) {
					Player player = (Player)defender;
					if (player.getHealth() - event.getDamage() <= 0) {
						if (attacker != null && attacker instanceof Player) {
							double bounty = getSaved(player.getName()).getBounty();
							if (bounty > 0.00) {
								MethodAccount attackerAccount = Method.getAccount(((Player)attacker).getName());
								attackerAccount.add(bounty);
								getSaved(player.getName()).setBounty(0.00);
							}
						}
					}
				}
			}
			
			private void check(EntityDamageByProjectileEvent event) {
				Entity attacker = event.getDamager();
				Entity defender = event.getEntity();
				
				if (defender instanceof Player) {
					Player player = (Player)defender;
					if (player.getHealth() - event.getDamage() <= 0) {
						if (attacker != null && attacker instanceof Player) {
							double bounty = getSaved(player.getName()).getBounty();
							if (bounty > 0.00) {
								MethodAccount attackerAccount = Method.getAccount(((Player)attacker).getName());
								attackerAccount.add(bounty);
								getSaved(player.getName()).setBounty(0.00);
							}
						}
					}
				}
			}
			
			@Override
			public void onEntityDamage(EntityDamageEvent event) {
				if (event instanceof EntityDamageByProjectileEvent) {
					check((EntityDamageByProjectileEvent) event);
					return;
				} else if (event instanceof EntityDamageByEntityEvent) {
					check((EntityDamageByEntityEvent) event);
					return;
				}
			}
		};
		
		PluginManager pm = getServer().getPluginManager();
		//RightClick - Used for area selection
		pm.registerEvent(Type.PLAYER_INTERACT, rightClickListener, Priority.Normal, this);
		//Damage - Used for bounty
		pm.registerEvent(Type.ENTITY_DAMAGE, entityListener, Priority.Normal, this);
		//Enable/Disable - Used for hook up to other plugins
		pm.registerEvent(Type.PLUGIN_ENABLE, pluginListener, Priority.Low, this);
		pm.registerEvent(Type.PLUGIN_DISABLE, pluginListener, Priority.Low, this);
		
		pdfFile = this.getDescription();
		log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );

		myFolder = getDataFolder();
		if (!myFolder.exists()) {
			consoleInfo("Config folder missing, creating...");
			myFolder.mkdir();
			consoleInfo("Folder created");
		}
		
		/*
		 * Check if economy isn't hooked up 20 seconds after startup of BankAccount
		 */
		checkJobId = this.getServer().getScheduler().scheduleSyncDelayedTask(thisPlugin, new Runnable() {
			public void run() {
				if (!pluginListener.Methods.hasMethod()) {
					//Shutdown if economy isn't found
					consoleWarning("Stopping BankAccount - Reason: Missing economy plugin!");
					getServer().getPluginManager().disablePlugin(thisPlugin);
					checkJobId = 0;
				}
			}
		}, 20*20);

		createDefaultConfiguration();
		loadConfiguration();
		loadFontWidth();
		
		/*
		 * Check if missing hook up is possible
		 * Checking for permissions plugins
		 * 
		 * Used if BankAccount is started after one of the third-part plugins
		 */
		if (Permissions == null && UsePermissions) {
			Plugin test = getServer().getPluginManager().getPlugin("Permissions");
			if (test != null) {
				if (test.isEnabled()) {
					Permissions = ((Permissions)test).getHandler();
					consoleInfo("Established connection with Permissions!");
				}
			}
		}
		if (GroupManager == null && UseGroupManager) {
			Plugin test = getServer().getPluginManager().getPlugin("GroupManager");
			if (test != null) {
				if (test.isEnabled()) {
					GroupManager = (GroupManager)test;
					consoleInfo("Established connection with GroupManager!");
				}
			}
		}
	}

	public void onDisable() {
		if (interestJobId > 0) {
			this.getServer().getScheduler().cancelTask(interestJobId);
		}
		interestJobId = 0;
		getCommand("account").setExecutor(disabledExecutor);
		getCommand("account").setUsage(ChatColor.RED + "BankAccount is disabled");
		log.info(pdfFile.getName() + " is disabled!" );
	}

	/**
	 * Add a transaction to the database
	 * 
	 * @param player - Username of the player
	 * @param account - Name of account
	 * @param type - Type of transaction (dk.earthgame.TAT.BankAccount.System.TransactionTypes)
	 * @param amount - amount of money the transaction (0.00 if money isn't a part of the transaction)
	 * @since 0.5
	 */
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
	
	/**
	 * Get the saved data of a player
	 * 
	 * @param player - The username of the player
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
	
	@SuppressWarnings("unchecked")
	private boolean loadConfiguration() {
		config = new Configuration(new File(this.getDataFolder(), "config.yml"));
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
		interestTime = config.getInt("Interest.Time", 0);
		//Area
		Areas = config.getBoolean("Areas.Active",false);
		AreaWandId = config.getInt("Areas.AreaWandid",339);
		MultiBanks = config.getBoolean("Areas.MultipleBanks", false);
		//Loan
		LoanSystem.LoanActive = config.getBoolean("Loan.Active", false);
		LoanSystem.Fixed_rate = config.getDouble("Loan.Fixed-rate", 0.00);
		LoanSystem.Rates = (Map<Double, Double>)config.getProperty("Loan.Rate");
		LoanSystem.Max_amount = config.getDouble("Loan.Max-amount", 200.00);
		LoanSystem.PaymentTime = config.getInt("Loan.Payment-time", 60);
		LoanSystem.PaymentParts = config.getInt("Loan.Payment-parts", 3);
		//Debug
		Debug_Interest = config.getBoolean("Debug.Interest", true);
		Debug_Loan = config.getBoolean("Debug.Loan", true);
		
		consoleInfo("Properties Loaded");
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
					consoleInfo("Connected to MySQL");
				} else {
					stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
					consoleInfo("Connected to SQLite");
				}
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
					consoleWarning("Couldn't get tables existing! Running as if all exists");
					consoleWarning(e3.toString());
				}
				try {
					if (!checkAccount) {
						//ACCOUNT TABLE
						String query = "CREATE TABLE IF NOT EXISTS `" + SQL_account_table + "` (`accountname` VARCHAR( 255 ) NOT NULL , `owners` LONGTEXT NOT NULL, `users` LONGTEXT NOT NULL, `password` VARCHAR( 255 ) NULL DEFAULT '', `amount` DOUBLE( 255,2 ) NOT NULL DEFAULT '0')";
						if (UseMySQL) {
							consoleWarning("Created table " + SQL_account_table);
							query = "CREATE TABLE IF NOT EXISTS `" + SQL_account_table + "` (`id` INT( 255 ) NOT NULL AUTO_INCREMENT PRIMARY KEY , `accountname` VARCHAR( 255 ) NOT NULL , `owners` LONGTEXT NOT NULL, `users` LONGTEXT NOT NULL, `password` VARCHAR( 255 ) NULL DEFAULT '', `amount` DOUBLE( 255,2 ) NOT NULL DEFAULT '0')";
						}
						stmt.execute(query);
					}
					if (!checkArea && Areas) {
						//AREA TABLE
						String query = "CREATE TABLE IF NOT EXISTS `" + SQL_area_table + "` (`areaname` VARCHAR( 255 ) NOT NULL , `world` VARCHAR( 255 ) NOT NULL , `x1` INT( 255 ) NOT NULL , `y1` INT( 255 ) NOT NULL , `z1` INT( 255 ) NOT NULL , `x2` INT( 255 ) NOT NULL , `y2` INT( 255 ) NOT NULL , `z2` INT( 255 ) NOT NULL)";
						if (UseMySQL) {
							consoleWarning("Created table " + SQL_area_table);
							query = "CREATE TABLE IF NOT EXISTS `" + SQL_area_table + "` (`id` INT( 255 ) NOT NULL AUTO_INCREMENT PRIMARY KEY , `areaname` VARCHAR( 255 ) NOT NULL , `world` VARCHAR( 255 ) NOT NULL , `x1` INT( 255 ) NOT NULL , `y1` INT( 255 ) NOT NULL , `z1` INT( 255 ) NOT NULL , `x2` INT( 255 ) NOT NULL , `y2` INT( 255 ) NOT NULL , `z2` INT( 255 ) NOT NULL)";
						}
						stmt.execute(query);
					}
					if (!checkLoan && LoanSystem.LoanActive) {
						//LOAN TABLE
						String query = "CREATE TABLE IF NOT EXISTS `" + SQL_loan_table + "` (`player` VARCHAR( 255 ) NOT NULL, `totalamount` DOUBLE( 255,2 ) NOT NULL, `remaining` DOUBLE( 255,2 ) NOT NULL, `timepayment` INT( 255 ) NOT NULL, `timeleft` INT( 255 ) NOT NULL, `part` INT( 255 ) NOT NULL, `parts` INT( 255 ) NOT NULL)";
						if (UseMySQL) {
							consoleWarning("Created table " + SQL_loan_table);
							query = "CREATE TABLE IF NOT EXISTS `" + SQL_loan_table + "` (`id` INT( 255 ) NOT NULL AUTO_INCREMENT PRIMARY KEY, `player` VARCHAR( 255 ) NOT NULL, `totalamount` DOUBLE( 255,2 ) NOT NULL, `timeleft` INT( 255 ) NOT NULL, `part` INT( 255 ) NOT NULL, `parts` INT( 255 ) NOT NULL)";
						}
						stmt.execute(query);
					}
					if (!checkTransaction && Transactions) {
						//TRANSACTION TABLE
						String query = "CREATE TABLE IF NOT EXISTS `" + SQL_transaction_table + "` (`player` VARCHAR( 255 ) NOT NULL, `account` VARCHAR( 255 ) NULL, `type` INT( 255 ) NOT NULL, `amount` DOUBLE( 255,2 ) NULL, `time` INT( 255 ) NOT NULL)";
						if (UseMySQL) {
							consoleWarning("Created table " + SQL_transaction_table);
							query = "CREATE TABLE IF NOT EXISTS `" + SQL_transaction_table + "` (`id` INT( 255 ) NOT NULL AUTO_INCREMENT PRIMARY KEY , `player` VARCHAR( 255 ) NOT NULL, `account` VARCHAR( 255 ) NULL, `type` INT( 255 ) NOT NULL, `amount` DOUBLE( 255,2 ) NULL, `time` INT( 255 ) NOT NULL)";
						}
						stmt.execute(query);
					}
					if (!checkBanks && MultiBanks) {
						//BANKS TABLE
						String query = "";
						if (UseMySQL) {
							consoleWarning("Created table " + SQL_banks_table);
							query = "";
						}
						stmt.execute(query);
					}
					
					//Run upgrades of SQL tables
					new Upgrade(this, UseMySQL);
				} catch (SQLException e3) {
					if (!checkAccount) {
						consoleWarning("Failed to find and create table " + SQL_account_table);
					}
					if (!checkArea && Areas) {
						consoleWarning("Failed to find and create table " + SQL_area_table);
						consoleInfo("Disabled areas!");
						Areas = false;
					}
					if (!checkLoan && LoanSystem.LoanActive) {
						consoleWarning("Failed to find and create table " + SQL_loan_table);
						consoleInfo("Disabled loans!");
						LoanSystem.LoanActive = false;
						if (LoanSystem.running) {
							LoanSystem.shutdownRunner();
						}
					}
					if (!checkTransaction && Transactions) {
						consoleWarning("Failed to find and create table " + SQL_transaction_table);
						consoleInfo("Disabled transactions!");
						Transactions = false;
					}
					if (!checkBanks && MultiBanks) {
						consoleWarning("Failed to find and create table " + SQL_banks_table);
						consoleInfo("Disabled multiple banks!");
						MultiBanks = false;
					}
					consoleWarning(e3.toString());
					consoleInfo("Shuting down");
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
				consoleInfo("Shuting down");
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
			consoleInfo("Shuting down");
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
					
					consoleInfo("Default config file created!");
				} catch (IOException e) {
					e.printStackTrace();
					consoleInfo("Error creating config file!");
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
			consoleInfo("Config file found!");
		}
	}
	
	//ATM / ACCOUNTS
	/**
	 * Check if an account exists
	 * 
	 * @since 0.5
	 * @param accountname - The name of the account
	 * @return boolean - If the account exists
	 */
	public boolean accountExists(String accountname) {
		ResultSet rs;
		int id = 0;
		try {
			if (UseMySQL) {
				rs = stmt.executeQuery("SELECT `id` FROM `" + SQL_account_table + "` WHERE `accountname` = '" + accountname + "'");
			} else {
				rs = stmt.executeQuery("SELECT `rowid` FROM `" + SQL_account_table + "` WHERE `accountname` = '" + accountname + "'");
			}
			while (rs.next()) {
				if (UseMySQL) {
					id = rs.getInt("id");
				} else {
					id = rs.getInt("rowid");
				}
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
	
	/**
	 * List of accounts, the user have access to
	 * 
	 * @since 0.5
	 * @param player - Username of the player
	 * @return List of accounts
	 */
	public List<String> accountList(String player) {
		List<String> accounts = new ArrayList<String>();
		ResultSet rs;
		try {
			rs = stmt.executeQuery("SELECT `accountname` FROM `" + SQL_account_table + "` WHERE `owners` LIKE '%" + player + "%' OR `users` LIKE '%" + player + "%'");
			while (rs.next()) {
				//Make sure it's not just a part of the name
				if (accessAccount(rs.getString("accountname"),player,false)) {
					accounts.add(rs.getString("accountname"));
				}
			}
			rs.close();
		} catch (SQLException e) {
			if (!e.getMessage().equalsIgnoreCase(null))
				consoleWarning("Error #22-2: " + e.getMessage());
			else
				consoleWarning("Error #22-1: " + e.getErrorCode() + " - " + e.getSQLState());
		}
		return accounts;
	}
	
	/**
	 * List of accounts, the user have access to
	 * 
	 * @since 0.5
	 * @param player - The player
	 * @return List of accounts
	 */
	public List<String> accountList(Player player) {
		return accountList(player.getName());
	}
	
	/**
	 * Add an new account
	 * 
	 * @param accountname - The name of the account
	 * @param player - Username of the players - Name of players seperated by comma (,)
	 * @since 0.5
	 * @return boolean - If the account is successfully created
	 */
	public boolean addAccount(String accountname,String players) {
		try {
			stmt.executeUpdate("INSERT INTO `" + SQL_account_table + "` (`accountname`,`owners`,`users`) VALUES ('" + accountname + "','" + players + "','')");
			return true;
		} catch(SQLException e) {
			if (!e.getMessage().equalsIgnoreCase(null))
				consoleWarning("Error #02-2: " + e.getMessage());
			else
				consoleWarning("Error #02-1: " + e.getErrorCode() + " - " + e.getSQLState());
		}
		return false;
	}
	
	/**
	 * Do the player have access to an account
	 * 
	 * @param accountname - Name of account
	 * @param player - The player
	 * @param writeAccess - Only look for owners
	 * @since 0.5
	 * @return boolean - If the player have access
	 */
	public boolean accessAccount(String accountname,Player player,boolean writeAccess) {
		if (!accountExists(accountname)) {
			//There is no spoon... I mean account
			return false;
		}
		if (SuperAdmins && playerPermission(player, PermissionNodes.ADMIN)) {
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
			rs = stmt.executeQuery("SELECT `" + coloum + "` FROM `" + SQL_account_table + "` WHERE `accountname` = '" + accountname + "'");
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
				consoleWarning("Error #03-3: " + e1.getMessage());
			else
				consoleWarning("Error #03-2: " + e1.getErrorCode() + " - " + e1.getSQLState());
		} catch(Exception e) {
			consoleWarning("Error #03-1: " + e.toString());
		}
		return false;
	}
	
	/**
	 * Do the player have access to an account
	 * 
	 * @param accountname - Name of account
	 * @param player - Username of player
	 * @param writeAccess - Only look for owners
	 * @since 0.5
	 * @return boolean - If the player have access
	 */
	public boolean accessAccount(String accountname,String player,boolean writeAccess) {
		try {
			ResultSet rs;
			rs = stmt.executeQuery("SELECT `users`, `owners` FROM `" + SQL_account_table + "` WHERE `accountname` = '" + accountname + "'");
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
				consoleWarning("Error #03-3: " + e1.getMessage());
			else
				consoleWarning("Error #03-2: " + e1.getErrorCode() + " - " + e1.getSQLState());
		} catch(Exception e) {
			consoleWarning("Error #03-1: " + e.toString());
		}
		return false;
	}

	/**
	 * Add user to account
	 * 
	 * @param accountname - Name of account
	 * @param player - Username of the player
	 * @since 0.5
	 * @see addOwner(String accountname,String player)
	 * @return boolean - If the user is successfully added
	 */
	public boolean addUser(String accountname,String player) {
		try {
			String newPlayers = player;
			ResultSet rs;
			rs = stmt.executeQuery("SELECT `users` FROM `" + SQL_account_table + "` WHERE `accountname` = '" + accountname + "'");
			while(rs.next()) {
				String[] players = rs.getString("users").split(";");
				for (String p : players) {
					newPlayers += ";" + p;
				}
			}
			try {
				stmt.executeUpdate("UPDATE `" + SQL_account_table + "` SET `users` = '" + newPlayers + "' WHERE `accountname` = '" + accountname + "'");
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
	
	/**
	 * Remove an user from an account
	 * 
	 * @param accountname - Name of account
	 * @param player - Username of the player
	 * @since 0.5
	 * @see removeOwner(String accountname,String player)
	 * @return boolean - If the user is successfully removed
	 */
	public boolean removeUser(String accountname,String player) {
		try {
			String newPlayers = "";
			ResultSet rs;
			rs = stmt.executeQuery("SELECT `users` FROM `" + SQL_account_table + "` WHERE `accountname` = '" + accountname + "'");
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
			try {
				stmt.executeUpdate("UPDATE `" + SQL_account_table + "` SET `users` = '" + newPlayers + "' WHERE `accountname` = '" + accountname + "'");
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
	
	/**
	 * Add owner to account
	 * 
	 * @param accountname - Name of account
	 * @param player - Username of the player
	 * @since 0.5
	 * @see addUser(String accountname,String player)
	 * @return boolean - If the owner is successfully added
	 */
	public boolean addOwner(String accountname,String player) {
		try {
			String newPlayers = player;
			ResultSet rs;
			rs = stmt.executeQuery("SELECT `owners` FROM `" + SQL_account_table + "` WHERE `accountname` = '" + accountname + "'");
			while(rs.next()) {
				String[] players = rs.getString("owners").split(";");
				for (String p : players) {
					newPlayers += ";" + p;
				}
			}
			try {
				stmt.executeUpdate("UPDATE `" + SQL_account_table + "` SET `owners` = '" + newPlayers + "' WHERE `accountname` = '" + accountname + "'");
				return true;
			} catch(SQLException e) {
				if (!e.getMessage().equalsIgnoreCase(null))
					consoleWarning("Error #xx-4: " + e.getMessage());
				else
					consoleWarning("Error #xx-3: " + e.getErrorCode() + " - " + e.getSQLState());
			}
		} catch(SQLException e) {
			if (!e.getMessage().equalsIgnoreCase(null))
				consoleWarning("Error #xx-2: " + e.getMessage());
			else
				consoleWarning("Error #xx-1: " + e.getErrorCode() + " - " + e.getSQLState());
		}
		return false;
	}
	
	/**
	 * Remove an owner from an account
	 * 
	 * @param accountname - Name of account
	 * @param player - Username of the player
	 * @since 0.5
	 * @see removeUser(String accountname,String player)
	 * @return boolean - If the owner is successfully removed
	 */
	public boolean removeOwner(String accountname,String player) {
		try {
			String newPlayers = "";
			ResultSet rs;
			rs = stmt.executeQuery("SELECT `owners` FROM `" + SQL_account_table + "` WHERE `accountname` = '" + accountname + "'");
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
			try {
				stmt.executeUpdate("UPDATE `" + SQL_account_table + "` SET `owners` = '" + newPlayers + "' WHERE `accountname` = '" + accountname + "'");
				return true;
			} catch(SQLException e) {
				if (!e.getMessage().equalsIgnoreCase(null))
					consoleWarning("Error #xx-4: " + e.getMessage());
				else
					consoleWarning("Error #xx-3: " + e.getErrorCode() + " - " + e.getSQLState());
			}
		} catch(SQLException e) {
			if (!e.getMessage().equalsIgnoreCase(null))
				consoleWarning("Error #xx-2: " + e.getMessage());
			else
				consoleWarning("Error #xx-1: " + e.getErrorCode() + " - " + e.getSQLState());
		}
		return false;
	}

	/**
	 * Set the password for an account
	 * 
	 * @param accountname - Name of account
	 * @param password - The new password (Must be encrypted!)
	 * @since 0.5
	 * @return boolean - If the password is successfully set
	 */
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

	/**
	 * Send action to the ATM
	 * 
	 * @param accountname - Name of account
	 * @param player - Username of the player
	 * @param type - Type of action
	 * @param amount - Amount money
	 * @param password - Password
	 * @since 0.5
	 * @return boolean - If the action is run successfully
	 */
	public boolean ATM(String accountname,String player,String type,Double amount,String password) {
		try {
			double account = getBalance(accountname);
			MethodAccount economyAccount = Method.getAccount(player);
			if (type == "deposit") {
				if (economyAccount.hasEnough(amount)) {
					account += amount;
					stmt.executeUpdate("UPDATE `" + SQL_account_table + "` SET `amount` = '" + account + "' WHERE `accountname` = '" + accountname + "'");
					economyAccount.subtract(amount);
					return true;
				} else {
					return false;
				}
			} else if (type == "withdraw") {
				if (PasswordSystem.passwordCheck(accountname, password)) {
					if ((account - amount) >= 0) {
						account -= amount;
						stmt.executeUpdate("UPDATE `" + SQL_account_table + "` SET `amount` = '" + account + "' WHERE `accountname` = '" + accountname + "'");
						economyAccount.add(amount);
						return true;
					} else {
						return false;
					}
				} else {
					return false;
				}
			} else if (type == "transfer") {
				if (PasswordSystem.passwordCheck(accountname, password)) {
					//Player = receiver account
					double receiver_account = getBalance(player);
					if ((account - amount) >= 0) {
						account -= amount;
						receiver_account += amount;
						stmt.executeUpdate("UPDATE `" + SQL_account_table + "` SET `amount` = '" + account + "' WHERE `accountname` = '" + accountname + "'");
						stmt.executeUpdate("UPDATE `" + SQL_account_table + "` SET `amount` = '" + receiver_account + "' WHERE `accountname` = '" + player + "'");
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
	
	/**
	 * Close an account
	 * 
	 * @param accountname - Name of account
	 * @param player - Username of the player
	 * @param password - Password
	 * @since 0.5
	 * @return boolean - If the account is successfully closed
	 */
	public boolean closeAccount(String accountname,String player,String password) {
		if (PasswordSystem.passwordCheck(accountname, password)) {
			try {
				MethodAccount economyAccount = Method.getAccount(player);
				double accountBalance = getBalance(accountname);
				stmt.executeUpdate("DELETE FROM `" + SQL_account_table + "` WHERE `accountname` = '" + accountname + "'");
				economyAccount.add(accountBalance);
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
	
	/**
	 * Get the users of an account
	 * 
	 * @param accountname - Name of account
	 * @since 0.5
	 * @return String of users (seperated by comma and space(, ))
	 */
	public String getUsers(String accountname) {
		try {
			String output = "";
			ResultSet rs;
			rs = stmt.executeQuery("SELECT `users` FROM `" + SQL_account_table + "` WHERE `accountname` = '" + accountname + "'");
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
				consoleWarning("Error #09-3: " + e1.getMessage());
			else
				consoleWarning("Error #09-2: " + e1.getErrorCode() + " - " + e1.getSQLState());
		} catch(Exception e) {
			consoleWarning("Error #09-1: " + e.toString());
		}
		return "Error loading users";
	}
	
	/**
	 * Get the owners of an account
	 * 
	 * @param accountname - Name of account
	 * @since 0.5
	 * @return String of owners (seperated by comma and space(, ))
	 */
	public String getOwners(String accountname) {
		try {
			String output = "";
			ResultSet rs;
			rs = stmt.executeQuery("SELECT `owners` FROM `" + SQL_account_table + "` WHERE `accountname` = '" + accountname + "'");
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
				consoleWarning("Error #09-3: " + e1.getMessage());
			else
				consoleWarning("Error #09-2: " + e1.getErrorCode() + " - " + e1.getSQLState());
		} catch(Exception e) {
			consoleWarning("Error #09-1: " + e.toString());
		}
		return "Error loading owners";
	}
	
	/**
	 * Get the balance of an account
	 * 
	 * @param accountname - Name of account
	 * @since 0.5
	 * @return double - Amount of money on account
	 */
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
	
	/**
	 * Set the balance of an account
	 * @param balance - New balance
	 * @param accountname - Name of account
	 * @since 0.5
	 * @return boolean - If the account balance is successfully changed
	 */
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
	
	/**
	 * Add money to an account
	 * 
	 * @param amount - Amount of money that shall be added
	 * @param accountname - Name of account
	 * @since 0.5
	 * @return boolean - If the money is successfully added
	 */
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
	
	/**
	 * Subtract money from an account
	 * 
	 * @param amount - Amount of money that shall be subtracted
	 * @param accountname - Name of account
	 * @since 0.5
	 * @return boolean - If the money is successfully subtracted
	 */
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
	/**
	 * Does an area exists
	 * 
	 * @param name - Name of area
	 * @since 0.5
	 * @return boolean - If the area exists
	 */
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
	
	/**
	 * Are the position inside an area
	 * 
	 * @param world - Name of world
	 * @param pos - Position
	 * @since 0.5
	 * @return boolean - If the position is inside an area
	 */
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
	
	/**
	 * Add an area
	 * 
	 * @param name - Name of area
	 * @param pos1 - Position 1
	 * @param pos2 - Position 2
	 * @param world - Name of world
	 * @since 0.5
	 * @return boolean - If the area is successfully added
	 */
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

	/**
	 * Remove an area
	 * 
	 * @param name - Name of area
	 * @since 0.5
	 * @return boolean - If the area is successfully removed
	 */
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
}