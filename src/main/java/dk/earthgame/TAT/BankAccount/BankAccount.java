package dk.earthgame.TAT.BankAccount;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.anjocaido.groupmanager.GroupManager;

import com.nijikokun.bukkit.Permissions.Permissions;

import dk.earthgame.TAT.BankAccount.Settings.FeeModes;
import dk.earthgame.TAT.BankAccount.System.Font;
import dk.earthgame.TAT.BankAccount.System.Password;
import dk.earthgame.TAT.BankAccount.System.PermissionNodes;
import dk.earthgame.TAT.BankAccount.System.TransactionTypes;
import dk.earthgame.TAT.BankAccount.System.UserSaves;
import dk.earthgame.TAT.BankAccount.System.Console;
import dk.earthgame.nijikokun.register.payment.Method;
import dk.earthgame.nijikokun.register.payment.Method.MethodAccount;

/**
 * BankAccount for Bukkit
 * @author TAT
 */
public class BankAccount extends JavaPlugin {
	protected final Plugin thisPlugin = this;
	//System
	File myFolder;
	private HashMap<String,UserSaves> UserSaves = new HashMap<String,UserSaves>();
	LoanSystem LoanSystem = new LoanSystem(this);
	Password PasswordSystem = new Password(this);
	BankAccountCommandExecutor cmdExecutor = new BankAccountCommandExecutor(this);
	BankAccountDisabled disabledExecutor = new BankAccountDisabled();
	BankAccountPluginListener pluginListener = new BankAccountPluginListener(this);
	public Settings settings = new Settings(this);
	public Console console;
	Font font = new Font();
	//Economy
	public Method Method = null;
	//NPC
	//NPCManager m = new NPCManager(this);

	//#########################################################################//
	
	//SYSTEM
	
	void foundEconomy() {
		if (LoanSystem.LoanActive && !LoanSystem.running) {
			LoanSystem.startupRunner();
		}
		if (settings.interestTime > 0) {
			if (settings.interestJobId > 0) {
				getServer().getScheduler().cancelTask(settings.interestJobId);
			}
			settings.interestJobId = getServer().getScheduler().scheduleSyncRepeatingTask(thisPlugin, new Runnable() {
				public void run() {
					if (settings.Debug_Interest)
						console.info("Running interest system");
														
					double totalGiven = 0.00;
					try {
						if (settings.UseMySQL) {
							//MySQL
							ResultSet accounts = settings.stmt.executeQuery("SELECT `id`, `amount`, `owners`, `users` FROM `" + settings.SQL_account_table + "`");
							while (accounts.next()) {
								double accountbalance = accounts.getDouble("amount");
								String[] owners = accounts.getString("owners").split(";");
								String[] users = accounts.getString("users").split(";");
								int neededOnline = (owners.length+users.length)/settings.interestNeededOnline;
								int online = 0;
								boolean accountOnline = false;
								if (owners.length > 1) {
									for (String o : owners) {
										if (getServer().getPlayer(o) != null) {
											if (getServer().getPlayer(o).isOnline()) {
												online++;
												if (online > neededOnline) {
													accountOnline = true;
													break;
												}
											}
										}
									}
								} else {
									if (getServer().getPlayer(owners[0]) != null) {
										if (getServer().getPlayer(owners[0]).isOnline()) {
											online++;
										}
									}
								}
								if (users.length > 1) {
									for (String u : users) {
										if (getServer().getPlayer(u) != null) {
											if (getServer().getPlayer(u).isOnline()) {
												online++;
												if (online > neededOnline) {
													accountOnline = true;
													break;
												}
											}
										}
									}
								} else {
									if (getServer().getPlayer(users[0]) != null) {
										if (getServer().getPlayer(users[0]).isOnline()) {
											online++;
										}
									}
								}
								if (online > neededOnline) {
									accountOnline = true;
								}
								double interest;
								if (accountOnline)
									interest = settings.interestAmount;
								else
									interest = settings.interestOfflineAmount;
								if (settings.MaxAmount > 0 && ((accountbalance *= 1+(interest/100)) > settings.MaxAmount)) {
									totalGiven += (settings.MaxAmount-accountbalance);
									accountbalance = settings.MaxAmount;
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
							PreparedStatement prep = settings.con.prepareStatement("UPDATE `" + settings.SQL_account_table + "` SET `amount` = ? WHERE `cleanname` = ?");
							ResultSet accounts = settings.updateStmt.executeQuery("SELECT `cleanname`, `amount`, `owners`, `users` FROM `" + settings.SQL_account_table + "`");
							while (accounts.next()) {
								String accountname = accounts.getString("cleanname");
								double accountbalance = accounts.getDouble("amount");
								String[] owners = accounts.getString("owners").split(";");
								String[] users = accounts.getString("users").split(";");
								int neededOnline = (owners.length+users.length)/settings.interestNeededOnline;
								int online = 0;
								boolean accountOnline = false;
								if (owners.length > 1) {
									for (String o : owners) {
										if (getServer().getPlayer(o) != null) {
											if (getServer().getPlayer(o).isOnline()) {
												online++;
												if (online > neededOnline) {
													accountOnline = true;
													break;
												}
											}
										}
									}
								} else {
									if (getServer().getPlayer(owners[0]) != null) {
										if (getServer().getPlayer(owners[0]).isOnline()) {
											online++;
										}
									}
								}
								if (users.length > 1 && !accountOnline) {
									for (String u : users) {
										if (getServer().getPlayer(u) != null) {
											if (getServer().getPlayer(u).isOnline()) {
												online++;
												if (online > neededOnline) {
													accountOnline = true;
													break;
												}
											}
										}
									}
								} else {
									if (getServer().getPlayer(users[0]) != null) {
										if (getServer().getPlayer(users[0]).isOnline()) {
											online++;
										}
									}
								}
								if (online > neededOnline) {
									accountOnline = true;
								}
								double interest;
								if (accountOnline)
									interest = settings.interestAmount;
								else
									interest = settings.interestOfflineAmount;
								if (settings.MaxAmount > 0 && ((accountbalance *= 1+(interest/100)) > settings.MaxAmount)) {
									totalGiven += (settings.MaxAmount-accountbalance);
									accountbalance = settings.MaxAmount;
								} else {
									totalGiven += accountbalance*(interest/100);
									accountbalance *= 1+(interest/100);
								}
								prep.setDouble(1, accountbalance);
								prep.setString(2, accountname);
								prep.addBatch();
							}
							accounts.close();
							settings.con.setAutoCommit(false);
							prep.executeBatch();
							settings.con.commit();
							settings.con.setAutoCommit(true);
						}
					} catch (SQLException e) {
						console.warning("Couldn't execute interest");
						console.info(e.toString());
					}
					if (settings.Debug_Interest)
						console.info("Total given " + Method.format(totalGiven) + " in interest");
				}
			}, settings.interestTime*20*60, settings.interestTime*20*60);
			console.info("Running interest every " + settings.interestTime + " minutes by " + settings.interestAmount + "%");
		}
		console.info("Established connection with economy!");
	}

	/**
	 * Check if a player have a PermissionNode
	 * @param player The player
	 * @param node The PermissionNode
	 * @param extraLookup If it's an extra lookup (Used by the system)
	 * @since 0.5
	 * @return boolean - If the player have the permission
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
	 * @return boolean - If the player have the permission
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
		
		PlayerListener rightClickListener = new PlayerListener() {
			@Override
			public void onPlayerInteract(PlayerInteractEvent event) {
				if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
					UserSaves mySave = getSaved(event.getPlayer().getName());
					if (event.getPlayer().getItemInHand().getTypeId() == settings.AreaWandId && mySave.isSelecting()) {
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
		settings.checkJobId = this.getServer().getScheduler().scheduleSyncDelayedTask(thisPlugin, new Runnable() {
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
	}

	/**
	 * Setting up BankAccount to use disabledExecutor and cancel tasks
	 */
	public void onDisable() {
		LoanSystem.shutdownRunner();
		if (settings.interestJobId > 0) {
			this.getServer().getScheduler().cancelTask(settings.interestJobId);
		}
		settings.interestJobId = 0;
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
	 * @since 0.5
	 */
	public void addTransaction(String player, String account, TransactionTypes type, Double amount) {
		if (settings.Transactions) {
			try {
				int time = Math.round(new Date().getTime()/1000);
				settings.stmt.executeUpdate("INSERT INTO `" + settings.SQL_transaction_table + "` (`player`,`account`,`type`,`amount`,`time`) VALUES ('" + player + "','" + account + "','" + type.get() + "','" + amount + "','" + time +"')");
			} catch(SQLException e) {
				if (!e.getMessage().equalsIgnoreCase(null))
					console.warning("Error #16-2: " + e.getMessage());
				else
					console.warning("Error #16-1: " + e.getErrorCode() + " - " + e.getSQLState());
			}
		}
	}
	
	//CONFIGURATION AND USERSAVES
	
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
	 * @return boolean - If the account exists
	 */
	public boolean accountExists(String accountname) {
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
	 */
	public List<String> accountList(String player) {
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
	 */
	public List<String> accountList(Player player) { return accountList(player.getName()); }
	
	/**
	 * Add a new account
	 * 
	 * @param accountname The name of the account
	 * @param players Username of the players - Name of players separated by comma (,)
	 * @since 0.5
	 * @return boolean - If the account is successfully created
	 * @deprecated
	 * @see #openAccount(String accountname,String players,String commandsender)
	 */
	public boolean addAccount(String accountname,String players) { return openAccount(accountname, players, ""); }
	
	/**
	 * Open a new account
	 * 
	 * @param accountname The name of the account
	 * @param players Username of the players - Name of players separated by comma (,)
	 * @param commandsender Username of the player that opens the account
	 * @since 0.5.1
	 * @return boolean - If the account is successfully created
	 */
	public boolean openAccount(String accountname,String players,String commandsender) {
		double feePaid = 0;
		if (settings.Fee_Mode != FeeModes.NONE) {
			MethodAccount account = Method.getAccount(commandsender);
			double balance = account.balance();
			switch (settings.Fee_Mode) {
			case PERCENTAGE:
				feePaid = balance*(settings.Fee_Percentage/100);
				account.subtract(balance*(settings.Fee_Percentage/100));
				break;
			case STATIC:
				feePaid = settings.Fee_Static;
				if (account.hasEnough(settings.Fee_Static)) {
					account.subtract(settings.Fee_Static);
				} else {
					return false;
				}
				break;
			case SMART1:
				feePaid = balance*(settings.Fee_Percentage/100);
				balance -= balance*(settings.Fee_Percentage/100);
				if (balance >= settings.Fee_Static) {
					feePaid += settings.Fee_Static;
					balance -= settings.Fee_Static;
					account.set(balance);
				} else {
					return false;
				}
				break;
			case SMART2:
				if (balance >= settings.Fee_Static) {
					feePaid = settings.Fee_Static;
					balance -= settings.Fee_Static;
				} else {
					return false;
				}
				feePaid += balance*(settings.Fee_Percentage/100);
				balance -= balance*(settings.Fee_Percentage/100);
				account.set(balance);
				break;
			}
		}
		
		double StartAmount = 0;
		if (settings.StartAmount_Active) {
			StartAmount += feePaid*settings.StartAmount_Fee;
			StartAmount += settings.StartAmount_Static;
		}
		
		try {
			settings.stmt.executeUpdate("INSERT INTO `" + settings.SQL_account_table + "` (`accountname`,`cleanname`,`owners`,`users`,`amount`) VALUES ('" + accountname + "','" + accountname.toLowerCase() + "','" + players + "','','" + StartAmount + "')");
			return true;
		} catch(SQLException e) {
			if (!e.getMessage().equalsIgnoreCase(null))
				console.warning("Error #02-2: " + e.getMessage());
			else
				console.warning("Error #02-1: " + e.getErrorCode() + " - " + e.getSQLState());
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
	 * @return boolean - If the player have access
	 */
	public boolean accessAccount(String accountname,Player player,boolean writeAccess) {
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
		} catch(Exception e) {
			console.warning("Error #03-1: " + e.toString());
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
	 * @return boolean - If the player have access
	 */
	public boolean accessAccount(String accountname,String player,boolean writeAccess) {
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
		} catch(Exception e) {
			console.warning("Error #03-1: " + e.toString());
		}
		return false;
	}

	/**
	 * Add user to account
	 * 
	 * @param accountname Name of account
	 * @param player Username of the player
	 * @since 0.5
	 * @see #addOwner(String accountname,String player)
	 * @return boolean - If the user is successfully added
	 */
	public boolean addUser(String accountname,String player) {
		try {
			String newPlayers = player;
			ResultSet rs;
			rs = settings.stmt.executeQuery("SELECT `users` FROM `" + settings.SQL_account_table + "` WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
			while(rs.next()) {
				String[] players = rs.getString("users").split(";");
				for (String p : players) {
					newPlayers += ";" + p;
				}
			}
			try {
				settings.stmt.executeUpdate("UPDATE `" + settings.SQL_account_table + "` SET `users` = '" + newPlayers + "' WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
				return true;
			} catch(SQLException e) {
				if (!e.getMessage().equalsIgnoreCase(null))
					console.warning("Error #04-4: " + e.getMessage());
				else
					console.warning("Error #04-3: " + e.getErrorCode() + " - " + e.getSQLState());
			}
		} catch(SQLException e) {
			if (!e.getMessage().equalsIgnoreCase(null))
				console.warning("Error #04-2: " + e.getMessage());
			else
				console.warning("Error #04-1: " + e.getErrorCode() + " - " + e.getSQLState());
		}
		return false;
	}
	
	/**
	 * Remove an user from an account
	 * 
	 * @param accountname Name of account
	 * @param player Username of the player
	 * @since 0.5
	 * @see #removeOwner(String accountname,String player)
	 * @return boolean - If the user is successfully removed
	 */
	public boolean removeUser(String accountname,String player) {
		try {
			String newPlayers = "";
			ResultSet rs;
			rs = settings.stmt.executeQuery("SELECT `users` FROM `" + settings.SQL_account_table + "` WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
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
				settings.stmt.executeUpdate("UPDATE `" + settings.SQL_account_table + "` SET `users` = '" + newPlayers + "' WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
				return true;
			} catch(SQLException e) {
				if (!e.getMessage().equalsIgnoreCase(null))
					console.warning("Error #05-4: " + e.getMessage());
				else
					console.warning("Error #05-3: " + e.getErrorCode() + " - " + e.getSQLState());
			}
		} catch(SQLException e) {
			if (!e.getMessage().equalsIgnoreCase(null))
				console.warning("Error #05-2: " + e.getMessage());
			else
				console.warning("Error #05-1: " + e.getErrorCode() + " - " + e.getSQLState());
		}
		return false;
	}
	
	/**
	 * Add owner to account
	 * 
	 * @param accountname Name of account
	 * @param player Username of the player
	 * @since 0.5
	 * @see #addUser(String accountname,String player)
	 * @return boolean - If the owner is successfully added
	 */
	public boolean addOwner(String accountname,String player) {
		try {
			String newPlayers = player;
			ResultSet rs;
			rs = settings.stmt.executeQuery("SELECT `owners` FROM `" + settings.SQL_account_table + "` WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
			while(rs.next()) {
				String[] players = rs.getString("owners").split(";");
				for (String p : players) {
					newPlayers += ";" + p;
				}
			}
			try {
				settings.stmt.executeUpdate("UPDATE `" + settings.SQL_account_table + "` SET `owners` = '" + newPlayers + "' WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
				return true;
			} catch(SQLException e) {
				if (!e.getMessage().equalsIgnoreCase(null))
					console.warning("Error #xx-4: " + e.getMessage());
				else
					console.warning("Error #xx-3: " + e.getErrorCode() + " - " + e.getSQLState());
			}
		} catch(SQLException e) {
			if (!e.getMessage().equalsIgnoreCase(null))
				console.warning("Error #xx-2: " + e.getMessage());
			else
				console.warning("Error #xx-1: " + e.getErrorCode() + " - " + e.getSQLState());
		}
		return false;
	}
	
	/**
	 * Remove an owner from an account
	 * 
	 * @param accountname Name of account
	 * @param player Username of the player
	 * @since 0.5
	 * @see #removeUser(String accountname,String player)
	 * @return boolean - If the owner is successfully removed
	 */
	public boolean removeOwner(String accountname,String player) {
		try {
			String newPlayers = "";
			ResultSet rs;
			rs = settings.stmt.executeQuery("SELECT `owners` FROM `" + settings.SQL_account_table + "` WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
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
				settings.stmt.executeUpdate("UPDATE `" + settings.SQL_account_table + "` SET `owners` = '" + newPlayers + "' WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
				return true;
			} catch(SQLException e) {
				if (!e.getMessage().equalsIgnoreCase(null))
					console.warning("Error #xx-4: " + e.getMessage());
				else
					console.warning("Error #xx-3: " + e.getErrorCode() + " - " + e.getSQLState());
			}
		} catch(SQLException e) {
			if (!e.getMessage().equalsIgnoreCase(null))
				console.warning("Error #xx-2: " + e.getMessage());
			else
				console.warning("Error #xx-1: " + e.getErrorCode() + " - " + e.getSQLState());
		}
		return false;
	}

	/**
	 * Set the password for an account
	 * 
	 * @param accountname Name of account
	 * @param password The new password (Must be encrypted!)
	 * @since 0.5
	 * @return boolean - If the password is successfully set
	 */
	public boolean setPassword(String accountname,String password) {
		try {
			settings.stmt.executeUpdate("UPDATE `" + settings.SQL_account_table + "` SET `password` = '" + password + "' WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
			return true;
		} catch(SQLException e) {
			if (!e.getMessage().equalsIgnoreCase(null))
				console.warning("Error #06-2: " + e.getMessage());
			else
				console.warning("Error #06-1: " + e.getErrorCode() + " - " + e.getSQLState());
		}
		return false;
	}

	/**
	 * Send action to the ATM
	 * 
	 * @param accountname Name of account
	 * @param player Username of the player
	 * @param type Type of action
	 * @param amount Amount money
	 * @param password Password
	 * @since 0.5
	 * @return boolean - If the action is run successfully
	 */
	public boolean ATM(String accountname,String player,String type,Double amount,String password) {
		try {
			double account = getBalance(accountname);
			MethodAccount economyAccount = Method.getAccount(player);
			if (type == "deposit") {
				if (settings.MaxAmount > 0 && (account+amount) > settings.MaxAmount) {
					//Cancel the transaction
					return false;
				} else if (economyAccount.hasEnough(amount)) {
					account += amount;
					settings.stmt.executeUpdate("UPDATE `" + settings.SQL_account_table + "` SET `amount` = '" + account + "' WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
					economyAccount.subtract(amount);
					return true;
				} else {
					return false;
				}
			} else if (type == "withdraw") {
				if (PasswordSystem.passwordCheck(accountname, password)) {
					if ((account - amount) >= 0) {
						account -= amount;
						settings.stmt.executeUpdate("UPDATE `" + settings.SQL_account_table + "` SET `amount` = '" + account + "' WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
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
					if (settings.MaxAmount > 0 && (receiver_account+amount) > settings.MaxAmount) {
						//Cancel the transaction
						return false;
					} else if ((account - amount) >= 0) {
						account -= amount;
						receiver_account += amount;
						settings.stmt.executeUpdate("UPDATE `" + settings.SQL_account_table + "` SET `amount` = '" + account + "' WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
						settings.stmt.executeUpdate("UPDATE `" + settings.SQL_account_table + "` SET `amount` = '" + receiver_account + "' WHERE `cleanname` = '" + player.toLowerCase() + "'");
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
				console.warning("Error #07-3: " + e1.getMessage());
			else
				console.warning("Error #07-2: " + e1.getErrorCode() + " - " + e1.getSQLState());
		} catch(Exception e) {
			console.warning("Error #07-1: " + e.toString());
		}
		return false;
	}
	
	/**
	 * Close an account
	 * 
	 * @param accountname Name of account
	 * @param player Username of the player
	 * @param password Password
	 * @since 0.5
	 * @return boolean - If the account is successfully closed
	 */
	public boolean closeAccount(String accountname,String player,String password) {
		if (PasswordSystem.passwordCheck(accountname, password)) {
			try {
				MethodAccount economyAccount = Method.getAccount(player);
				double accountBalance = getBalance(accountname);
				settings.stmt.executeUpdate("DELETE FROM `" + settings.SQL_account_table + "` WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
				economyAccount.add(accountBalance);
				return true;
			} catch(SQLException e) {
				if (!e.getMessage().equalsIgnoreCase(null))
					console.warning("Error #08-3: " + e.getMessage());
				else
					console.warning("Error #08-2: " + e.getErrorCode() + " - " + e.getSQLState());
			} catch(Exception e) {
				console.warning("Error #08-1: " + e.toString());
			}
			return false;
		} else {
			return false;
		}
	}
	
	/**
	 * Get the users of an account
	 * 
	 * @param accountname Name of account
	 * @since 0.5
	 * @return String of users (seperated by comma and space(, ))
	 */
	public String getUsers(String accountname) {
		try {
			String output = "";
			ResultSet rs;
			rs = settings.stmt.executeQuery("SELECT `users` FROM `" + settings.SQL_account_table + "` WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
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
				console.warning("Error #09-3: " + e1.getMessage());
			else
				console.warning("Error #09-2: " + e1.getErrorCode() + " - " + e1.getSQLState());
		} catch(Exception e) {
			console.warning("Error #09-1: " + e.toString());
		}
		return "Error loading users";
	}
	
	/**
	 * Get the owners of an account
	 * 
	 * @param accountname Name of account
	 * @since 0.5
	 * @return String of owners (seperated by comma and space(, ))
	 */
	public String getOwners(String accountname) {
		try {
			String output = "";
			ResultSet rs;
			rs = settings.stmt.executeQuery("SELECT `owners` FROM `" + settings.SQL_account_table + "` WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
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
				console.warning("Error #09-3: " + e1.getMessage());
			else
				console.warning("Error #09-2: " + e1.getErrorCode() + " - " + e1.getSQLState());
		} catch(Exception e) {
			console.warning("Error #09-1: " + e.toString());
		}
		return "Error loading owners";
	}
	
	/**
	 * Get the balance of an account
	 * 
	 * @param accountname Name of account
	 * @since 0.5
	 * @return double - Amount of money on account
	 */
	public double getBalance(String accountname) {
		try {
			ResultSet rs;
			rs = settings.stmt.executeQuery("SELECT `amount` FROM `" + settings.SQL_account_table + "` WHERE `cleanname` = '" + accountname.toLowerCase() +"'");
			while (rs.next()) {
				return rs.getDouble("amount");
			}
		} catch (SQLException e1) {
			if (!e1.getMessage().equalsIgnoreCase(null))
				console.warning("Error #10-3: " + e1.getMessage());
			else
				console.warning("Error #10-2: " + e1.getErrorCode() + " - " + e1.getSQLState());
		} catch(Exception e) {
			console.warning("Error #10-1: " + e.toString());
		}
		return 0;
	}
	
	/**
	 * Set the balance of an account
	 * @param balance New balance
	 * @param accountname Name of account
	 * @since 0.5
	 * @return boolean - If the account balance is successfully changed
	 */
	public boolean setBalance(double balance,String accountname) {
		try {
			settings.stmt.executeUpdate("UPDATE `" + settings.SQL_account_table + "` SET `amount` = '" + balance + "' WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
			return true;
		} catch (SQLException e) {
			if (!e.getMessage().equalsIgnoreCase(null))
				console.warning("Error #17-2: " + e.getMessage());
			else
				console.warning("Error #17-1: " + e.getErrorCode() + " - " + e.getSQLState());
		}
		return false;
	}
	
	/**
	 * Add money to an account
	 * 
	 * @param amount Amount of money that shall be added
	 * @param accountname Name of account
	 * @since 0.5
	 * @return boolean - If the money is successfully added
	 */
	public boolean add(double amount,String accountname) {
		double temp = getBalance(accountname);
		temp += amount;
		try {
			settings.stmt.executeUpdate("UPDATE `" + settings.SQL_account_table + "` SET `amount` = '" + temp + "' WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
			return true;
		} catch (SQLException e) {
			if (!e.getMessage().equalsIgnoreCase(null))
				console.warning("Error #18-2: " + e.getMessage());
			else
				console.warning("Error #18-1: " + e.getErrorCode() + " - " + e.getSQLState());
		}
		return false;
	}
	
	/**
	 * Subtract money from an account
	 * 
	 * @param amount Amount of money that shall be subtracted
	 * @param accountname Name of account
	 * @since 0.5
	 * @return boolean - If the money is successfully subtracted
	 */
	public boolean subtract(double amount,String accountname) {
		double temp = getBalance(accountname);
		temp -= amount;
		try {
			settings.stmt.executeUpdate("UPDATE `" + settings.SQL_account_table + "` SET `amount` = '" + temp + "' WHERE `cleanname` = '" + accountname.toLowerCase() + "'");
			return true;
		} catch (SQLException e) {
			if (!e.getMessage().equalsIgnoreCase(null))
				console.warning("Error #19-2: " + e.getMessage());
			else
				console.warning("Error #19-1: " + e.getErrorCode() + " - " + e.getSQLState());
		}
		return false;
	}
	
	//AREAS
	/**
	 * Does an area exists
	 * 
	 * @param name Name of area
	 * @since 0.5
	 * @return boolean - If the area exists
	 */
	public boolean areaExists(String name) {
		ResultSet rs;
		int id = 0;
		try {
			if (settings.UseMySQL) {
				rs = settings.stmt.executeQuery("SELECT `id` FROM `" + settings.SQL_area_table + "` WHERE `areaname` = '" + name + "'");
			} else {
				rs = settings.stmt.executeQuery("SELECT `rowid` FROM `" + settings.SQL_area_table + "` WHERE `areaname` = '" + name + "'");
			}
			try {
				while (rs.next()) {
					if (settings.UseMySQL) {
						id = rs.getInt("id");
					} else {
						id = rs.getInt("rowid");
					}
				}
			} catch (SQLException e1) {
				if (!e1.getMessage().equalsIgnoreCase(null))
					console.warning("Error #14-4: " + e1.getMessage());
				else
					console.warning("Error #14-3: " + e1.getErrorCode() + " - " + e1.getSQLState());
			}
		} catch (SQLException e) {
			if (!e.getMessage().equalsIgnoreCase(null))
				console.warning("Error #14-2: " + e.getMessage());
			else
				console.warning("Error #14-1: " + e.getErrorCode() + " - " + e.getSQLState());
		}
		if (id > 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * Are the position inside an area
	 * 
	 * @param world Name of world
	 * @param pos Position
	 * @since 0.5
	 * @return boolean - If the position is inside an area
	 */
	public boolean inArea(String world,Location pos) {
		try {
			ResultSet rs = settings.stmt.executeQuery("SELECT `x1`,`y1`,`z1`,`x2`,`y2`,`z2` FROM `" + settings.SQL_area_table + "` WHERE `world` = '" + world + "'");
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
				console.warning("Error #15-2: " + e.getMessage());
			else
				console.warning("Error #15-1: " + e.getErrorCode() + " - " + e.getSQLState());
		}
		return false;
	}
	
	/**
	 * Add an area
	 * 
	 * @param name Name of area
	 * @param pos1 Position 1
	 * @param pos2 Position 2
	 * @param world Name of world
	 * @since 0.5
	 * @return boolean - If the area is successfully added
	 */
	public boolean setArea(String name,Location pos1,Location pos2,String world) {
		if (areaExists(name)) {
			return false;
		}
		try {
			settings.stmt.executeUpdate("INSERT INTO `" + settings.SQL_area_table + "` (`areaname`,`world`,`x1`, `y1`, `z1`, `x2`, `y2`, `z2`) VALUES ('" + name + "','" + world + "','" + pos1.getBlockX() + "','" + pos1.getBlockY() + "','" + pos1.getBlockZ() + "','" + pos2.getBlockX() + "','" + pos2.getBlockY() + "','" + pos2.getBlockZ() + "')");
			return true;
		} catch(SQLException e) {
			if (!e.getMessage().equalsIgnoreCase(null))
				console.warning("Error #12-2: " + e.getMessage());
			else
				console.warning("Error #12-1: " + e.getErrorCode() + " - " + e.getSQLState());
		}
		return false;
	}

	/**
	 * Remove an area
	 * 
	 * @param name Name of area
	 * @since 0.5
	 * @return boolean - If the area is successfully removed
	 */
	public boolean removeArea(String name) {
		try {
			settings.stmt.executeUpdate("DELETE FROM `" + settings.SQL_area_table + "` WHERE `areaname` = '" + name + "'");
			return true;
		} catch(SQLException e) {
			if (!e.getMessage().equalsIgnoreCase(null))
				console.warning("Error #13-3: " + e.getMessage());
			else
				console.warning("Error #13-2: " + e.getErrorCode() + " - " + e.getSQLState());
		} catch (Exception e) {
			console.warning("Error #13-1: " + e.toString());
		}
		return false;
	}
	
	/**
	 * Output Info to log on behalf of BankAccount
	 * 
	 * @deprecated
	 * @param message
	 * @since 0.5
	 * @see #console
	 */
	public void consoleInfo(String message) { console.info(message); }

	/**
	 * Output Warning to log on behalf of BankAccount
	 * 
	 * @deprecated
	 * @param message
	 * @since 0.5
	 * @see #console
	 */
	public void consoleWarning(String message) { console.warning(message); }
}