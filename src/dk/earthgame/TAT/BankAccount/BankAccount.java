package dk.earthgame.TAT.BankAccount;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRightClickEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.bukkit.util.config.Configuration;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;
import com.nijikokun.bukkit.Permissions.Permissions;

/**
 * BankAccount for Bukkit
 * 
 * @author TAT
 */
public class BankAccount extends JavaPlugin {
	//System
	protected static final Logger log = Logger.getLogger("Minecraft");
	private static PluginDescriptionFile pdfFile;
	private static File myFolder;
	public Configuration config;
	private int interestTime;
	private double interestAmount;
	private int interestJobId;
	private HashMap<String,UserSaves> UserSaves = new HashMap<String,UserSaves>();
	private int areaWandId;
	//MySQL
	private static boolean UseMySQL = false;
	private static String MySQL_host;
	private String MySQL_port;
	private String MySQL_username;
	private String MySQL_password;
	private String MySQL_database;
	//SQL
	public static Connection con;
	public static java.sql.Statement stmt;
	private static String SQL_account_table;
	private static String SQL_area_table;
	//iConomy
	public static com.nijiko.coelho.iConomy.iConomy iConomy;
	private boolean useiConomy = false;
	//Permissions
	private static boolean UseOP = true;
	public Permissions Permissions = null;
	private static boolean UsePermissions = false;
	public boolean Global;
	public static boolean SuperAdmins;

	//#########################################################################//
	
	//THIRD-PART PLUGINS
	
	public boolean checkiConomy() {
		Plugin test = this.getServer().getPluginManager().getPlugin("iConomy");

		if (test != null) {
			BankAccount.iConomy = (iConomy)test;
			useiConomy = true;
		} else {
			consoleWarning("Stopping BankAccount - Reason: Missing iConomy plugin!");
			this.getServer().getPluginManager().disablePlugin(this);
			useiConomy = false;
		}

		return useiConomy;
	}

	public void setupPermissions() {
		// Initialize permissions system
		Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");

		if(Permissions == null) {
			if(test != null) {
				Permissions = (Permissions)test;
				consoleLog("Permission system found.");
			} else {
				consoleLog("Permission system not found.");
			}
		}
	}
	
	//SYSTEM
	
	public void consoleLog(String string) {
		log.info(pdfFile.getName() + ": " + string);
	}

	public static void consoleWarning(String string) {
		log.warning(pdfFile.getName() + ": " + string);
	}
	
	public Integer playerIsAdmin(Player player) {
		if (player != null) {
			if (UsePermissions) {
				if (com.nijikokun.bukkit.Permissions.Permissions.Security.permission(player, "bankaccount.admin")) {
					return 2;
				}
			}
			if (UseOP) {
				if (player.isOp()) {
					return 1;
				} else {
					return 0;
				}
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}
	
	public void onEnable() {
		// Register our events
		getCommand("account").setExecutor(new BankAccountCommandExecutor(this));
		getCommand("account").setUsage("/account help - Show help to BankAccount");
		
		BlockListener rightClickListener = new BlockListener() {
			@Override
            public void onBlockRightClick(BlockRightClickEvent event) {
				UserSaves mySave = getSaved(event.getPlayer());
				if (event.getPlayer().getItemInHand().getTypeId() == areaWandId && mySave.selecting) {
		        	World world = event.getBlock().getWorld();
		    		Location pos = event.getBlock().getLocation();
		    		world.getBlockAt(pos);
		        	if (mySave.setPosition(pos) == 2) {
		        		event.getPlayer().sendMessage("ATM: Area selected, to confirm: /account setarea <areaname>");
		        	} else {
		        		event.getPlayer().sendMessage("ATM: Position 1 selected, please select position 2");
		        	}
		        }
            }
		};
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Type.BLOCK_RIGHTCLICKED, rightClickListener, Priority.Normal, this);
		
		pdfFile = this.getDescription();
		log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );

		myFolder = getDataFolder();
		if (!myFolder.exists()) {
			consoleLog("Config folder missing, creating...");
			myFolder.mkdir();
			consoleLog("Folder created");
		}

		createDefaultConfiguration();
		loadConfiguration();
	}

	public void onDisable() {
		if (interestJobId > 0) {
			this.getServer().getScheduler().cancelTask(interestJobId);
		}
		getCommand("account").setExecutor(new BankAccountDisabled(this));
		getCommand("account").setUsage(ChatColor.RED + "BankAccount is disabled");
		log.info(pdfFile.getName() + " is disabled!" );
	}

	//CONFIGURATION AND USERSAVES
	
	public UserSaves getSaved(Player player) {
		if (UserSaves.containsKey(player.getName())) {
			return UserSaves.get(player.getName());
		}
		
		UserSaves save = new UserSaves();
		UserSaves.put(player.getName(), save);
		return save;
	}
	
	public boolean loadConfiguration() {
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
		//Permissions
		SuperAdmins = config.getBoolean("SuperAdmins", false);
		UseOP = config.getBoolean("UseOP",true);
		UsePermissions = config.getBoolean("UsePermissions",false);
		if (UsePermissions && Permissions == null) {
			setupPermissions();
		}
		//Interest
		interestAmount = config.getDouble("Interest.Amount", 0);
		interestTime = config.getInt("Interest.Time", 0);
		//Area
		Global = config.getBoolean("Global",true);
		areaWandId = config.getInt("AreaWandid",339);
		
		if (interestTime > 0) {
			if (interestJobId > 0) {
				this.getServer().getScheduler().cancelTask(interestJobId);
			}
			interestJobId = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
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
					consoleLog("Total given " + totalGiven + " " + com.nijiko.coelho.iConomy.iConomy.getBank().getCurrency() + " in interest");
				}
			}, interestTime*20*60, interestTime*20*60);
			consoleLog("Running interest every " + interestTime + " minutes by " + interestAmount + "%");
		}
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
					boolean check = false;
					if (UseMySQL) {
						ResultSet test = stmt.executeQuery("SHOW TABLES LIKE '" + SQL_account_table + "'");
						while (test.next()) {
							check = true;
						}
					}
					if (!check || !UseMySQL) {
						//ACCOUNT TABLE
						String query = "CREATE TABLE IF NOT EXISTS `" + SQL_account_table + "` (`accountname` VARCHAR( 255 ) NOT NULL , `players` LONGTEXT NOT NULL, `password` VARCHAR( 255 ) NULL DEFAULT '', `amount` DOUBLE( 255,2 ) NOT NULL DEFAULT '0')";
						if (UseMySQL) {
							consoleWarning("Created table " + SQL_account_table);
							query = "CREATE TABLE IF NOT EXISTS `" + SQL_account_table + "` (`id` INT( 255 ) NOT NULL AUTO_INCREMENT PRIMARY KEY , `accountname` VARCHAR( 255 ) NOT NULL , `players` LONGTEXT NOT NULL, `password` VARCHAR( 255 ) NULL DEFAULT '', `amount` DOUBLE( 255,2 ) NOT NULL DEFAULT '0')";
						}
						stmt.execute(query);
						//AREA TABLE
						query = "CREATE TABLE IF NOT EXISTS `" + SQL_area_table + "` (`areaname` VARCHAR( 255 ) NOT NULL , `world` VARCHAR( 255 ) NOT NULL , `x1` INT( 255 ) NOT NULL , `y1` INT( 255 ) NOT NULL , `z1` INT( 255 ) NOT NULL , `x2` INT( 255 ) NOT NULL , `y2` INT( 255 ) NOT NULL , `z2` INT( 255 ) NOT NULL)";
						if (UseMySQL) {
							consoleWarning("Created table " + SQL_area_table);
							query = "CREATE TABLE IF NOT EXISTS `" + SQL_area_table + "` (`id` INT( 255 ) NOT NULL AUTO_INCREMENT PRIMARY KEY , `areaname` VARCHAR( 255 ) NOT NULL , `world` VARCHAR( 255 ) NOT NULL , `x1` INT( 255 ) NOT NULL , `y1` INT( 255 ) NOT NULL , `z1` INT( 255 ) NOT NULL , `x2` INT( 255 ) NOT NULL , `y2` INT( 255 ) NOT NULL , `z2` INT( 255 ) NOT NULL)";
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
	
	public static Boolean accountExists(String accountname) {
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
				if (e1.getMessage() != null)
					consoleWarning("Error #014: " + e1.getMessage());
				else
					consoleWarning("Error #013: " + e1.getErrorCode() + " - " + e1.getSQLState());
			}
		} catch (SQLException e) {
			if (e.getMessage() != null)
				consoleWarning("Error #012: " + e.getMessage());
			else
				consoleWarning("Error #011: " + e.getErrorCode() + " - " + e.getSQLState());
		}
		if (id > 0) {
			return true;
		}
		return false;
	}
	
	public static Boolean addAccount(String accountname,String players) {
		try {
			stmt.executeUpdate("INSERT INTO `" + SQL_account_table + "` (`accountname`,`players`) VALUES ('" + accountname + "','" + players + "')");
			return true;
		} catch(SQLException e) {
			if (e.getMessage() != null)
				consoleWarning("Error #022: " + e.getMessage());
			else
				consoleWarning("Error #021: " + e.getErrorCode() + " - " + e.getSQLState());
		}
		return false;
	}
	
	public static Boolean accessAccount(String accountname,String player) {
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
		} catch(Exception e) {
			consoleWarning("Error #031: " + e.toString());
		}
		return false;
	}

	public static Boolean addUser(String accountname,String player) {
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
			}catch(Exception e) {
				consoleWarning("Error #042: " + e.toString());
			}
		}catch(Exception e) {
			consoleWarning("Error #041: " + e.toString());
		}
		return false;
	}
	
	public static Boolean removeUser(String accountname,String player) {
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
			}catch(Exception e) {
				consoleWarning("Error #052: " + e.toString());
			}
		}catch(Exception e) {
			consoleWarning("Error #051: " + e.toString());
		}
		return false;
	}

	public static Boolean setPassword(String accountname,String password) {
		try {
			stmt.executeUpdate("UPDATE `" + SQL_account_table + "` SET `password` = '" + password + "' WHERE `accountname` = '" + accountname + "'");
			return true;
		}catch(Exception e) {
			consoleWarning("Error #061: " + e.toString());
		}
		return false;
	}

	@SuppressWarnings("static-access")
	public static Boolean ATM(String accountname,String player,String type,Double amount,String password) {
		try {
			double account = getBalance(accountname);
			Account iConomyAccount = iConomy.getBank().getAccount(player);
			if (type == "deposit") {
				double wallet = iConomyAccount.getBalance();
				if ((wallet - amount) >= 0) {
					account += amount;
					stmt.executeUpdate("UPDATE `" + SQL_account_table + "` SET `amount` = '" + account + "' WHERE `accountname` = '" + accountname + "'");
					iConomyAccount.setBalance(wallet - amount);
					iConomyAccount.save();
					return true;
				} else {
					return false;
				}
			} else if (type == "withdraw") {
				if (passwordCheck(accountname, password)) {
					double wallet = iConomyAccount.getBalance();
					if ((account - amount) >= 0) {
						account -= amount;
						stmt.executeUpdate("UPDATE `" + SQL_account_table + "` SET `amount` = '" + account + "' WHERE `accountname` = '" + accountname + "'");
						iConomyAccount.setBalance(wallet + amount);
						iConomyAccount.save();
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
		}catch(Exception e) {
			consoleWarning("Error #071: " + e.toString());
		}
		return false;
	}
	
	@SuppressWarnings("static-access")
	public static Boolean closeAccount(String accountname,String player,String password) {
		if (passwordCheck(accountname, password)) {
			try {
				Account iConomyAccount = iConomy.getBank().getAccount(player);
				double wallet = iConomyAccount.getBalance();
				double accountBalance = getBalance(accountname);
				stmt.executeUpdate("DELETE FROM `" + SQL_account_table + "` WHERE `accountname` = '" + accountname + "'");
				iConomyAccount.setBalance(wallet + accountBalance);
				iConomyAccount.save();
				return true;
			}catch(Exception e) {
				consoleWarning("Error #081: " + e.toString());
			}
			return false;
		} else {
			return false;
		}
	}
	
	public static String getUsers(String accountname) {
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
		}catch(Exception e) {
			consoleWarning("Error #091: " + e.toString());
		}
		return "Error loading players";
	}
	
	public static double getBalance(String accountname) {
		try {
			ResultSet rs;
			rs = stmt.executeQuery("SELECT `amount` FROM `" + SQL_account_table + "` WHERE `accountname` = '" + accountname +"'");
			while (rs.next()) {
				return rs.getDouble("amount");
			}
		}catch(Exception e) {
			consoleWarning("Error #091: " + e.toString());
		}
		return 0;
	}

	//AREAS
	
	public static Boolean areaExists(String name) {
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
				if (e1.getMessage() != null)
					consoleWarning("Error #144: " + e1.getMessage());
				else
					consoleWarning("Error #143: " + e1.getErrorCode() + " - " + e1.getSQLState());
			}
		} catch (SQLException e) {
			if (e.getMessage() != null)
				consoleWarning("Error #142: " + e.getMessage());
			else
				consoleWarning("Error #141: " + e.getErrorCode() + " - " + e.getSQLState());
		}
		if (id > 0) {
			return true;
		}
		return false;
	}
	
	public Boolean inArea(String world,Location pos) {
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
			consoleWarning("Error #151: " + e.toString());
		}
		return false;
	}
	
	public static Boolean setArea(String name,Location pos1,Location pos2,String world) {
		if (areaExists(name)) {
			return false;
		}
		try {
			stmt.executeUpdate("INSERT INTO `" + SQL_area_table + "` (`areaname`,`world`,`x1`, `y1`, `z1`, `x2`, `y2`, `z2`) VALUES ('" + name + "','" + world + "','" + pos1.getBlockX() + "','" + pos1.getBlockY() + "','" + pos1.getBlockZ() + "','" + pos2.getBlockX() + "','" + pos2.getBlockY() + "','" + pos2.getBlockZ() + "')");
			return true;
		} catch(SQLException e) {
			consoleWarning("Error #122: " + e.toString());
		}
		return false;
	}

	public static Boolean removeArea(String name) {
		try {
			stmt.executeUpdate("DELETE FROM `" + SQL_area_table + "` WHERE `areaname` = '" + name + "'");
			return true;
		} catch(SQLException e) {
			consoleWarning("Error #132: " + e.toString());
		} catch (Exception e) {
			consoleWarning("Error #131: " + e.toString());
		}
		return false;
	}
	
	//PASSWORDS
	
	public static Boolean passwordCheck(String accountname,String password) {
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
		}catch(Exception e) {
			consoleWarning("Error #101: " + e.toString());
		}
		return false;
	}
	
	public static String passwordCrypt(String password) {
		byte[] temp = password.getBytes();
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(temp);
			byte[] output = md.digest();
			password = bytesToHex(output);
			return password;
		} catch (NoSuchAlgorithmException e) {
			BankAccount.consoleWarning("Error #111: Couldn't crypt password");
			return "Error";
		}
	}

	public static String bytesToHex(byte[] b) {
		char hexDigit[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
		StringBuffer buf = new StringBuffer();
		for (int j=0; j<b.length; j++) {
			buf.append(hexDigit[(b[j] >> 4) & 0x0f]);
			buf.append(hexDigit[b[j] & 0x0f]);
		}
		return buf.toString();
	}
}