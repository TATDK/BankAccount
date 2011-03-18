package dk.earthgame.TAT.BankAccount;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.nijiko.coelho.iConomy.iConomy;

public class BankAccountCommandExecutor implements CommandExecutor {
	private final BankAccount plugin;

	public BankAccountCommandExecutor(BankAccount instantiate) {
		plugin = instantiate;
	}

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	String sendername = ((Player)sender).getName();
    	if (label.equalsIgnoreCase("account")) {
    		//LocationCheck: Are you in bankarea?
      		boolean locationCheck = false;
  	    	if (!plugin.Global) {
  	    		if (plugin.inArea(((Player)sender).getWorld().getName(), ((Player)sender).getLocation())) {
  	    			locationCheck = true;
  	    		}
  	    	} else {
  	    		locationCheck = true;
  	    	}
  	    	
  			if (args.length > 0) {
  				if (args[0].equalsIgnoreCase("open") && args.length >= 2) {
  					if (!locationCheck) {
  						sender.sendMessage("ATM: You're not in bank area");
  						return true;
  					}
  					if (BankAccount.accountExists(args[1])) {
  						sender.sendMessage("ATM: " + ChatColor.RED + "Accountname is taken");
  					} else {
  						String players = "";
  						players += sendername;
  						if (args.length >= 3) {
  							for (int i = 3;i<=args.length;i++) {
  								players += ";" + args[i-1];
  							}
  						}
  						if (BankAccount.addAccount(args[1], players)) {
  							sender.sendMessage("ATM: " + ChatColor.GREEN + args[1] + " created");
  							players = "";
  	  						players += sendername;
  	  						if (args.length >= 3) {
  	  							for (int i = 3;i<=args.length;i++) {
  	  								players += ", " + args[i-1];
  	  							}
  	  						}
  							sender.sendMessage(ChatColor.WHITE + "Players: " + ChatColor.GREEN + players);
  						} else {
  							sender.sendMessage("ATM: " + ChatColor.RED + "Couldn't create bank account");
  						}
  					}
  				} else if (args[0].equalsIgnoreCase("balance") && args.length >= 2) {
  					if (!locationCheck) {
  						sender.sendMessage("ATM: You're not in bank area");
  						return true;
  					}
  					if (!BankAccount.accountExists(args[1])) {
  						sender.sendMessage("ATM: " + ChatColor.RED + "Account not found");
  					} else {
  						if (BankAccount.accessAccount(args[1], sendername)) {
  							sender.sendMessage("ATM: Balance of " + args[1] + ": " + ChatColor.GREEN + iConomy.getBank().format(BankAccount.getBalance(args[1])));
  						} else {
  							sender.sendMessage("ATM: " + ChatColor.RED + "You don't have access to this account!");
  						}
  					}
  				} else if (args[0].equalsIgnoreCase("info") && args.length >= 2) {
  					if (!locationCheck) {
  						sender.sendMessage("ATM: You're not in bank area");
  						return true;
  					}
  					if (!BankAccount.accountExists(args[1])) {
  						sender.sendMessage("ATM: " + ChatColor.RED + "Account not found");
  					} else {
  						if (BankAccount.accessAccount(args[1], sendername)) {
  							sender.sendMessage("ATM: Balance of " + args[1] + ": " + ChatColor.GREEN + iConomy.getBank().format(BankAccount.getBalance(args[1])));
  							sender.sendMessage(ChatColor.WHITE + "Players: " + ChatColor.GREEN + BankAccount.getUsers(args[1]));
  						} else {
  							sender.sendMessage("ATM: " + ChatColor.RED + "You don't have access to this account!");
  						}
  					}
  				} else if (args[0].equalsIgnoreCase("adduser") && args.length >= 3) {
  					if (!locationCheck) {
  						sender.sendMessage("ATM: You're not in bank area");
  						return true;
  					}
  					if (BankAccount.accessAccount(args[1], sendername)) {
  						BankAccount.addUser(args[1], args[2]);
  						sender.sendMessage("ATM: " + ChatColor.GREEN + args[2] + " added to account");
  					} else {
  						sender.sendMessage("ATM: " + ChatColor.RED + "You don't have access to this account!");
  					}
  				} else if (args[0].equalsIgnoreCase("removeuser") && args.length >= 3) {
  					if (BankAccount.accessAccount(args[1], sendername)) {
  						BankAccount.removeUser(args[1], args[2]);
  						sender.sendMessage("ATM: " + ChatColor.GREEN + args[2] + " removed from account");
  					} else {
  						sender.sendMessage("ATM: " + ChatColor.RED + "You don't have access to this account!");
  					}
  				} else if (args[0].equalsIgnoreCase("password") && args.length >= 2) {
  					if (!locationCheck) {
  						sender.sendMessage("ATM: You're not in bank area");
  						return true;
  					}
  					if (BankAccount.accessAccount(args[1], sendername)) {
	  					String password = "";
						if (args.length >= 3) {
							password = args[2];
						}
						password = BankAccount.passwordCrypt(password);
						if (password != "Error") {
							if (BankAccount.setPassword(args[1], password)) {
								sender.sendMessage("ATM: " + ChatColor.GREEN + "Password set for account");
							} else {
								sender.sendMessage("ATM: " + ChatColor.RED + "Couldn't set password for account");
							}
						} else {
							sender.sendMessage("ATM: " + ChatColor.RED + "Couldn't set password for account");
						}
  					} else {
  						sender.sendMessage("ATM: " + ChatColor.RED + "You don't have access to this account!");
  					}
  				} else if (args[0].equalsIgnoreCase("deposit") && args.length >= 3) {
  					if (!locationCheck) {
  						sender.sendMessage("ATM: You're not in bank area");
  						return true;
  					}
  					if (BankAccount.accessAccount(args[1], sendername)) {
  						String password = "";
  						if (BankAccount.ATM(args[1], sendername, "deposit", Double.parseDouble(args[2]), password)) {
  							sender.sendMessage("ATM: " + ChatColor.GREEN + iConomy.getBank().format(Double.parseDouble(args[2])) + " added to " + args[1]);
  						} else {
  							sender.sendMessage("ATM: " + ChatColor.RED + "Couldn't deposit, are you sure you have enough money?");
  						}
  					} else {
  						sender.sendMessage("ATM: " + ChatColor.RED + "You don't have access to this account!");
  					}
  				} else if (args[0].equalsIgnoreCase("withdraw") && args.length >= 3) {
  					if (!locationCheck) {
  						sender.sendMessage("ATM: You're not in bank area");
  						return true;
  					}
  					if (BankAccount.accessAccount(args[1], sendername)) {
  						String password = "";
  						if (args.length >= 4) {
  							password = args[3];
  						}
  						if (BankAccount.ATM(args[1], sendername, "withdraw", Double.parseDouble(args[2]), password)) {
  							sender.sendMessage("ATM: " + ChatColor.GREEN + iConomy.getBank().format(Double.parseDouble(args[2])) + " " + iConomy.getBank().getCurrency() + " withdrawed from " + args[1]);
  						} else {
  							sender.sendMessage("ATM: " + ChatColor.RED + "Couldn't withdraw, are you sure you have enough money on account?");
  						}
  					} else {
  						sender.sendMessage("ATM: " + ChatColor.RED + "You don't have access to this account!");
  					}
  				} else if (args[0].equalsIgnoreCase("transfer") && args.length >= 4) {
  					if (!locationCheck) {
  						sender.sendMessage("ATM: You're not in bank area");
  						return true;
  					}
  					if (BankAccount.accessAccount(args[1], sendername)) {
  						if (BankAccount.accountExists(args[2])) {
							String password = "";
							if (args.length >= 5) {
								password = args[4];
							}
							if (BankAccount.ATM(args[1], args[2], "transfer", Double.parseDouble(args[3]), password)) {
								sender.sendMessage("ATM: " + ChatColor.GREEN + iConomy.getBank().format(Double.parseDouble(args[3])) + " transfered from " + args[1] + " to " + args[2]);
							} else {
								sender.sendMessage("ATM: " + ChatColor.RED + "Couldn't transfer, are you sure you have enough money on account?");
							}
  						} else {
  							sender.sendMessage("ATM: " + ChatColor.RED + "Reciever account not found!");
  						}
  					} else {
  						sender.sendMessage("ATM: " + ChatColor.RED + "You don't have access to this account!");
  					}
  				} else if (args[0].equalsIgnoreCase("loan") && args.length >= 4) {
  					if (!locationCheck) {
  						sender.sendMessage("ATM: You're not in bank area");
  						return true;
  					}
  					if (BankAccount.accessAccount(args[1], sendername)) {
  						if (BankAccount.accountExists(args[2])) {
							String password = "";
							if (args.length >= 4) {
								password = args[3];
							}
							/*if (BankAccount.ATM(args[1], "transfer", Double.parseDouble(args[2]), password)) {
								sender.sendMessage("ATM: " + ChatColor.GREEN + iConomy.getBank().format(Double.parseDouble(args[3])) + " transfered from " + args[1] + " to " + args[2]);
							} else {
								sender.sendMessage("ATM: " + ChatColor.RED + "Couldn't transfer, are you sure you have enough money on account?");
							}*/
  						} else {
  							sender.sendMessage("ATM: " + ChatColor.RED + "Reciever account not found!");
  						}
  					} else {
  						sender.sendMessage("ATM: " + ChatColor.RED + "You don't have access to this account!");
  					}
  				} else if (args[0].equalsIgnoreCase("close") && args.length >= 2) {
  					if (!locationCheck) {
  						sender.sendMessage("ATM: You're not in bank area");
  						return true;
  					}
  					if (BankAccount.accessAccount(args[1], sendername)) {
  						String password = "";
  						if (args.length >= 3) {
  							password = args[2];
  						}
  						double money = BankAccount.getBalance(args[1]);
  						if (BankAccount.closeAccount(args[1], sendername, password)) {
  							if (money > 0) {
  								sender.sendMessage("ATM: " + iConomy.getBank().format(money) + " withdrawed");
  							}
  							sender.sendMessage("ATM: Account closed");
  						} else {
  							sender.sendMessage("ATM: Wrong accountname and/or password");
  						}
  					} else {
  						sender.sendMessage("ATM: " + ChatColor.RED + "You don't have access to this account!");
  					}
  				} else if (args[0].equalsIgnoreCase("select")) {
  					if (plugin.playerIsAdmin((Player)sender) > 0) {
  						UserSaves mySave = plugin.getSaved((Player)sender);
  						if (mySave.selecting) {
  							mySave.selecting = false;
							sender.sendMessage("ATM: " + ChatColor.GREEN + "No longer selecting area");
						} else {
  							mySave.selecting = true;
							sender.sendMessage("ATM: " + ChatColor.GREEN + "Selecting area active, send command again to deactivate");
						}
  					} else {
  						sender.sendMessage("ATM: " + ChatColor.RED + "You don't have access to this command!");
  					}
  				} else if (args[0].equalsIgnoreCase("setarea") && args.length >= 2) {
  					if (plugin.playerIsAdmin((Player)sender) > 0) {
  						UserSaves mySave = plugin.getSaved((Player)sender);
  						if (mySave.getPosition(1) != null && mySave.getPosition(2) != null) {
  							if (BankAccount.setArea(args[1], mySave.getPosition(1), mySave.getPosition(2), ((Player)sender).getWorld().getName())) {
  								sender.sendMessage("ATM: " + ChatColor.GREEN + "Area added");
  							} else {
  								sender.sendMessage("ATM: " + ChatColor.RED + "Something went wrong. Please try again.");
  							}
  						} else {
  							sender.sendMessage("ATM: " + ChatColor.RED + "You haven't selected an area!");
  						}
  					} else {
  						sender.sendMessage("ATM: " + ChatColor.RED + "You don't have access to this command!");
  					}
  				} else if (args[0].equalsIgnoreCase("removearea") && args.length >= 2) {
  					if (plugin.playerIsAdmin((Player)sender) > 0) {
  						if (BankAccount.removeArea(args[1])) {
  							sender.sendMessage("ATM: " + ChatColor.GREEN + "Area removed");
  						} else {
  							sender.sendMessage("ATM: " + ChatColor.RED + "Something went wrong. Please try again.");
  						}
  					} else {
  						sender.sendMessage("ATM: " + ChatColor.RED + "You don't have access to this command!");
  					}
  				} else if (args[0].equalsIgnoreCase("help") && args.length >= 1) {
  					if (args.length >= 2) {
  						showHelp(sender,Integer.parseInt(args[1]));
  					} else {
  						showHelp(sender,1);
  					}
  				} else {
  					showHelp(sender,1);
  				}
  			} else {
  				showHelp(sender,1);
  			}
  			return true;
  		}
		return false;
    }
    
    public void showHelp(CommandSender player, int Page) {
    	player.sendMessage(ChatColor.DARK_RED + "Bank Account Help - Page " + Page + " of 2");
    	player.sendMessage(ChatColor.DARK_GREEN + "This is used mainly to shared bank accounts");
		switch (Page) {
		case 1:
			player.sendMessage(ChatColor.RED + "/account help [page]");
			player.sendMessage(ChatColor.RED + "/account open <accountname> [players]");
			player.sendMessage(ChatColor.RED + "/account info <accountname>");
			player.sendMessage(ChatColor.RED + "/account balance <accountname>");
			player.sendMessage(ChatColor.RED + "/account adduser <accountname> <player>");
			player.sendMessage(ChatColor.RED + "/account removeuser <accountname> <player>");
			player.sendMessage(ChatColor.RED + "/account password <accountname> [password]");
			player.sendMessage(ChatColor.RED + "/account deposit <accountname> <amount>");
			break;
		case 2:
			player.sendMessage(ChatColor.RED + "/account withdraw <accountname> <amount> [password]");
			player.sendMessage(ChatColor.RED + "/account transfer <from account> <to account> <amount> [password]");
			player.sendMessage(ChatColor.RED + "/account loan <account> <amount> [password]");
			player.sendMessage(ChatColor.RED + "/account close <accountname> [password]");
			if (plugin.playerIsAdmin((Player)player) > 0) {
				player.sendMessage(ChatColor.RED + "/account select");
				player.sendMessage(ChatColor.RED + "/account setarea <areaname>");
				player.sendMessage(ChatColor.RED + "/account removearea <areaname>");
			}
			break;
		}
    }
}
