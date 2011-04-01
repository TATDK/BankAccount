package dk.earthgame.TAT.BankAccount;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nijiko.coelho.iConomy.iConomy;

import dk.earthgame.TAT.BankAccount.System.PermissionNodes;
import dk.earthgame.TAT.BankAccount.System.TransactionTypes;
import dk.earthgame.TAT.BankAccount.System.UserSaves;

public class BankAccountCommandExecutor implements CommandExecutor {
	private final BankAccount plugin;

	public BankAccountCommandExecutor(BankAccount instantiate) {
		plugin = instantiate;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		boolean isPlayer = (sender instanceof Player);
		if (!isPlayer) {
			sender.sendMessage("Non-players can't use BankAccount");
			return true;
		}
		String sendername = ((Player)sender).getName();
		if (label.equalsIgnoreCase("account")) {
			if (!plugin.playerPermission((Player)sender,PermissionNodes.ACCESS)) {
				sender.sendMessage("You don't have access to use BankAccount");
				return true;
			}
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
  					if (!plugin.playerPermission((Player)sender,PermissionNodes.OPEN)) {
  						sender.sendMessage("You don't have access to open an account");
  						return true;
  					}
  					if (!locationCheck) {
  						sender.sendMessage("ATM: You're not in bank area");
  						return true;
  					}
  					if (plugin.accountExists(args[1])) {
  						sender.sendMessage("ATM: " + ChatColor.RED + "Accountname is taken");
  					} else {
  						String players = "";
  						players += sendername;
  						if (args.length >= 3) {
  							for (int i = 3;i<=args.length;i++) {
  								players += ";" + args[i-1];
  							}
  						}
  						if (plugin.addAccount(args[1], players)) {
  							plugin.addTransaction(sendername, args[1], TransactionTypes.OPEN, 0.00);
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
  					if (!plugin.accountExists(args[1])) {
  						sender.sendMessage("ATM: " + ChatColor.RED + "Account not found");
  					} else {
  						if (plugin.accessAccount(args[1], sendername)) {
  							sender.sendMessage("ATM: Balance of " + args[1] + ": " + ChatColor.GREEN + iConomy.getBank().format(plugin.getBalance(args[1])));
  						} else {
  							sender.sendMessage("ATM: " + ChatColor.RED + "You don't have access to this account!");
  						}
  					}
  				} else if (args[0].equalsIgnoreCase("info") && args.length >= 2) {
  					if (!locationCheck) {
  						sender.sendMessage("ATM: You're not in bank area");
  						return true;
  					}
  					if (!plugin.accountExists(args[1])) {
  						sender.sendMessage("ATM: " + ChatColor.RED + "Account not found");
  					} else {
  						if (plugin.accessAccount(args[1], sendername)) {
  							sender.sendMessage("ATM: Balance of " + args[1] + ": " + ChatColor.GREEN + iConomy.getBank().format(plugin.getBalance(args[1])));
  							sender.sendMessage(ChatColor.WHITE + "Players: " + ChatColor.GREEN + plugin.getUsers(args[1]));
  						} else {
  							sender.sendMessage("ATM: " + ChatColor.RED + "You don't have access to this account!");
  						}
  					}
  				} else if (args[0].equalsIgnoreCase("adduser") && args.length >= 3) {
  					if (!locationCheck) {
  						sender.sendMessage("ATM: You're not in bank area");
  						return true;
  					}
  					if (plugin.accessAccount(args[1], sendername)) {
  						if (plugin.addUser(args[1], args[2])) {
  							plugin.addTransaction(sendername, args[1], TransactionTypes.USER_ADD, 0.00);
  							sender.sendMessage("ATM: " + ChatColor.GREEN + args[2] + " added to account");
  						} else {
  							sender.sendMessage("ATM: " + ChatColor.RED + "Something went wrong");
  						}
  					} else {
  						sender.sendMessage("ATM: " + ChatColor.RED + "You don't have access to this account!");
  					}
  				} else if (args[0].equalsIgnoreCase("removeuser") && args.length >= 3) {
  					if (plugin.accessAccount(args[1], sendername)) {
  						if (plugin.removeUser(args[1], args[2])) {
  							plugin.addTransaction(sendername, args[1], TransactionTypes.USER_REMOVE, 0.00);
  							sender.sendMessage("ATM: " + ChatColor.GREEN + args[2] + " removed from account");
  						} else {
  							sender.sendMessage("ATM: " + ChatColor.RED + "Something went wrong");
  						}
  					} else {
  						sender.sendMessage("ATM: " + ChatColor.RED + "You don't have access to this account!");
  					}
  				} else if (args[0].equalsIgnoreCase("password") && args.length >= 2) {
  					if (!plugin.playerPermission((Player)sender,PermissionNodes.PASSWORD)) {
  						sender.sendMessage("You don't have access to set password");
  						return true;
  					}
  					if (!locationCheck) {
  						sender.sendMessage("ATM: You're not in bank area");
  						return true;
  					}
  					if (plugin.accessAccount(args[1], sendername)) {
	  					String password = "";
						if (args.length >= 3) {
							password = args[2];
						}
						password = plugin.passwordCrypt(password);
						if (!password.equalsIgnoreCase("Error")) {
							if (plugin.setPassword(args[1], password)) {
								plugin.addTransaction(sendername, args[1], TransactionTypes.PASSWORD, 0.00);
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
  					if (!plugin.playerPermission((Player)sender,PermissionNodes.DEPOSIT)) {
  						sender.sendMessage("You don't have access to deposit");
  						return true;
  					}
  					if (!locationCheck) {
  						sender.sendMessage("ATM: You're not in bank area");
  						return true;
  					}
  					if (plugin.accessAccount(args[1], sendername)) {
  						String password = "";
  						if (plugin.ATM(args[1], sendername, "deposit", Double.parseDouble(args[2]), password)) {
  							plugin.addTransaction(sendername, args[1], TransactionTypes.DEPOSIT, Double.parseDouble(args[2]));
  							sender.sendMessage("ATM: " + ChatColor.GREEN + iConomy.getBank().format(Double.parseDouble(args[2])) + " added to " + args[1]);
  						} else {
  							sender.sendMessage("ATM: " + ChatColor.RED + "Couldn't deposit, are you sure you have enough money?");
  						}
  					} else {
  						sender.sendMessage("ATM: " + ChatColor.RED + "You don't have access to this account!");
  					}
  				} else if (args[0].equalsIgnoreCase("withdraw") && args.length >= 3) {
  					if (!plugin.playerPermission((Player)sender,PermissionNodes.WITHDRAW)) {
  						sender.sendMessage("You don't have access to open an account");
  						return true;
  					}
  					if (!locationCheck) {
  						sender.sendMessage("ATM: You're not in bank area");
  						return true;
  					}
  					if (plugin.accessAccount(args[1], sendername)) {
  						String password = "";
  						if (args.length >= 4) {
  							password = args[3];
  						}
  						if (plugin.ATM(args[1], sendername, "withdraw", Double.parseDouble(args[2]), password)) {
  							plugin.addTransaction(sendername, args[1], TransactionTypes.WITHDRAW, Double.parseDouble(args[2]));
  							sender.sendMessage("ATM: " + ChatColor.GREEN + iConomy.getBank().format(Double.parseDouble(args[2])) + " " + iConomy.getBank().getCurrency() + " withdrawed from " + args[1]);
  						} else {
  							sender.sendMessage("ATM: " + ChatColor.RED + "Couldn't withdraw, are you sure you have enough money on account?");
  						}
  					} else {
  						sender.sendMessage("ATM: " + ChatColor.RED + "You don't have access to this account!");
  					}
  				} else if (args[0].equalsIgnoreCase("transfer") && args.length >= 4) {
  					if (!plugin.playerPermission((Player)sender,PermissionNodes.TRANSFER)) {
  						sender.sendMessage("You don't have access to transfer");
  						return true;
  					}
  					if (!locationCheck) {
  						sender.sendMessage("ATM: You're not in bank area");
  						return true;
  					}
  					if (plugin.accessAccount(args[1], sendername)) {
  						if (plugin.accountExists(args[2])) {
							String password = "";
							if (args.length >= 5) {
								password = args[4];
							}
							if (plugin.ATM(args[1], args[2], "transfer", Double.parseDouble(args[3]), password)) {
								plugin.addTransaction(sendername, args[1], TransactionTypes.TRANSFER_WITHDRAW, Double.parseDouble(args[3]));
								plugin.addTransaction("SYSTEM", args[2], TransactionTypes.TRANSFER_DEPOSIT, Double.parseDouble(args[3]));
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
  				} else if (args[0].equalsIgnoreCase("loan") && args.length >= 2) {
  					if (!plugin.playerPermission((Player)sender,PermissionNodes.LOAN)) {
  						sender.sendMessage("You don't have access to loan");
  						return true;
  					}
  					//CREATE LOAN
  					if (!locationCheck) {
  						sender.sendMessage("ATM: You're not in bank area");
  						return true;
  					}
  					if (!plugin.LoanSystem.LoanActive) {
  						sender.sendMessage("ATM: Loans not activated");
  					}
  					if (!plugin.LoanSystem.haveLoan(sendername) && plugin.getSaved(sendername).bounty == 0.00) {
  						plugin.addTransaction(sendername, null, TransactionTypes.LOAN_START, Double.parseDouble(args[1]));
  						if (plugin.LoanSystem.addLoan(sendername, Double.parseDouble(args[1]))) {
  							sender.sendMessage("ATM: " + ChatColor.GREEN + iConomy.getBank().format(Double.parseDouble(args[1])) + " loaned.");
  						} else {
  							sender.sendMessage("ATM: " + ChatColor.RED + "Couldn't create loan!");
  						}
  					} else if (plugin.getSaved(sendername).bounty > 0.00) {
  						sender.sendMessage("ATM: " + ChatColor.RED + "You have a bounty on your head!");
  					} else {
  						sender.sendMessage("ATM: " + ChatColor.RED + "You already have a loan, pay it first!");
  					}
  				} else if (args[0].equalsIgnoreCase("loan") && args.length == 1) {
  					if (!plugin.playerPermission((Player)sender,PermissionNodes.LOAN)) {
  						sender.sendMessage("You don't have access to have loan");
  						return true;
  					}
  					//LOAN STATUS
  					if (!locationCheck) {
  						sender.sendMessage("ATM: You're not in bank area");
  						return true;
  					}
  					if (!plugin.LoanSystem.LoanActive) {
  						sender.sendMessage("ATM: Loans not activated");
  					}
  					if (plugin.LoanSystem.haveLoan(sendername)) {
  						Loan playerLoan = plugin.LoanSystem.getLoan(sendername);
  						sender.sendMessage("ATM: " + ChatColor.GREEN + "Found loan.");
  						sender.sendMessage(ChatColor.GOLD + "Amount+rates: " + ChatColor.WHITE + iConomy.getBank().format(playerLoan.totalamount));
  						sender.sendMessage(ChatColor.GOLD + "Remaining: " + ChatColor.WHITE + iConomy.getBank().format(playerLoan.remaining));
  					} else {
  						sender.sendMessage("ATM: " + ChatColor.RED + "You don't have a loan.");
  					}
  				} else if (args[0].equalsIgnoreCase("pay") && args.length >= 2) {
  					if (!plugin.playerPermission((Player)sender,PermissionNodes.LOAN)) {
  						sender.sendMessage("You don't have access to have loan");
  						return true;
  					}
  					//PAY LOAN
  					if (!locationCheck) {
  						sender.sendMessage("ATM: You're not in bank area");
  						return true;
  					}
  					if (!plugin.LoanSystem.LoanActive) {
  						sender.sendMessage("ATM: Loans not activated");
  					}
  					if (plugin.LoanSystem.haveLoan(sendername)) {
  						double paid = plugin.LoanSystem.payment(sendername, Double.parseDouble(args[1]));
  						if (paid > 0.00) {
  							plugin.addTransaction(sendername, null, TransactionTypes.LOAN_PAYMENT, paid);
  							sender.sendMessage("ATM: " + ChatColor.GREEN + iConomy.getBank().format(paid) + " paid off your loan.");
  						} else {
  							sender.sendMessage("ATM: " + ChatColor.RED + "Couldn't pay!");
  						}
  					} else {
  						sender.sendMessage("ATM: " + ChatColor.RED + "You don't have a loan");
  					}
  				} else if (args[0].equalsIgnoreCase("close") && args.length >= 2) {
  					if (!plugin.playerPermission((Player)sender,PermissionNodes.CLOSE)) {
  						sender.sendMessage("You don't have access to close an account");
  						return true;
  					}
  					if (!locationCheck) {
  						sender.sendMessage("ATM: You're not in bank area");
  						return true;
  					}
  					if (plugin.accessAccount(args[1], sendername)) {
  						String password = "";
  						if (args.length >= 3) {
  							password = args[2];
  						}
  						double money = plugin.getBalance(args[1]);
  						if (plugin.closeAccount(args[1], sendername, password)) {
  							if (money > 0) {
  								plugin.addTransaction(sendername, args[1], TransactionTypes.WITHDRAW, money);
  								sender.sendMessage("ATM: " + iConomy.getBank().format(money) + " withdrawed");
  							}
  							plugin.addTransaction(sendername, args[1], TransactionTypes.CLOSE, 0.00);
  							sender.sendMessage("ATM: Account closed");
  						} else {
  							sender.sendMessage("ATM: Wrong accountname and/or password");
  						}
  					} else {
  						sender.sendMessage("ATM: " + ChatColor.RED + "You don't have access to this account!");
  					}
  				} else if (args[0].equalsIgnoreCase("select")) {
  					if (!plugin.playerPermission((Player)sender,PermissionNodes.ADMIN)) {
  						sender.sendMessage("You don't have access to select areas");
  						return true;
  					} else {
  						UserSaves mySave = plugin.getSaved(sendername);
  						if (mySave.selecting) {
  							mySave.selecting = false;
							sender.sendMessage("ATM: " + ChatColor.GREEN + "No longer selecting area");
						} else {
  							mySave.selecting = true;
							sender.sendMessage("ATM: " + ChatColor.GREEN + "Selecting area active, send command again to deactivate");
						}
  					}
  				} else if (args[0].equalsIgnoreCase("setarea") && args.length >= 2) {
  					if (!plugin.playerPermission((Player)sender,PermissionNodes.ADMIN)) {
  						sender.sendMessage("You don't have access to add areas");
  						return true;
  					} else {
  						UserSaves mySave = plugin.getSaved(sendername);
  						if (mySave.getPosition(1) != null && mySave.getPosition(2) != null) {
  							if (plugin.setArea(args[1], mySave.getPosition(1), mySave.getPosition(2), ((Player)sender).getWorld().getName())) {
  								sender.sendMessage("ATM: " + ChatColor.GREEN + "Area added");
  							} else {
  								sender.sendMessage("ATM: " + ChatColor.RED + "Something went wrong. Please try again.");
  							}
  						} else {
  							sender.sendMessage("ATM: " + ChatColor.RED + "You haven't selected an area!");
  						}
  					}
  				} else if (args[0].equalsIgnoreCase("removearea") && args.length >= 2) {
  					if (!plugin.playerPermission((Player)sender,PermissionNodes.ADMIN)) {
  						sender.sendMessage("You don't have access to remove areas");
  						return true;
  					} else {
  						if (plugin.removeArea(args[1])) {
  							sender.sendMessage("ATM: " + ChatColor.GREEN + "Area removed");
  						} else {
  							sender.sendMessage("ATM: " + ChatColor.RED + "Something went wrong. Please try again.");
  						}
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
	
	enum commandList {
		OPEN				("open <accountname> [players]"),
		INFO				("info <accountname>"),
		BALANCE				("balance <accountname>"),
		DEPOSIT				("deposit <accountname> <amount>"),
		WITHDRAW			("withdraw <accountname> <amount> [password]"),
		TRANSFER			("transfer <from account> <to account> <amount> [password]"),
		CLOSE				("close <accountname> [password]"),
		ADDUSER				("adduser <accountname> <player>"),
		REMOVEUSER			("removeuser <accountname> <player>"),
		PASSWORD			("password <accountname> [password]"),
		LOAN				("loan <amount>"),
		PAY					("pay <amount>"),
		SELECT				("select"),
		ADDAREA				("setarea <areaname>"),
		REMOVEAREA			("removearea <areaname>");
		
		private final String description;
		commandList(String description) {
			this.description = description;
		}
		public String getdescription() { return description; }
	};
	
	public void showHelp(CommandSender sender, int Page) {
		Player player = (Player)sender;
		List<String> commands = new ArrayList<String>();
		if (plugin.playerPermission(player, PermissionNodes.EXTENDED) || plugin.playerPermission(player, PermissionNodes.ADMIN)) {
			commands.add(commandList.OPEN.getdescription());
			commands.add(commandList.INFO.getdescription());
			commands.add(commandList.BALANCE.getdescription());
			commands.add(commandList.DEPOSIT.getdescription());
			commands.add(commandList.WITHDRAW.getdescription());
			commands.add(commandList.TRANSFER.getdescription());
			commands.add(commandList.ADDUSER.getdescription());
			commands.add(commandList.REMOVEUSER.getdescription());
			commands.add(commandList.PASSWORD.getdescription());
			commands.add(commandList.CLOSE.getdescription());
			commands.add(commandList.LOAN.getdescription());
			commands.add(commandList.PAY.getdescription());
			if (plugin.playerPermission(player, PermissionNodes.ADMIN)) {
				commands.add(commandList.SELECT.getdescription());
				commands.add(commandList.ADDAREA.getdescription());
				commands.add(commandList.REMOVEAREA.getdescription());
			}
		} else if (plugin.playerPermission(player, PermissionNodes.BASIC)) {
			commands.add(commandList.OPEN.getdescription());
			commands.add(commandList.INFO.getdescription());
			commands.add(commandList.BALANCE.getdescription());
			commands.add(commandList.DEPOSIT.getdescription());
			commands.add(commandList.WITHDRAW.getdescription());
			if (plugin.playerPermission(player, PermissionNodes.TRANSFER)) {
				commands.add(commandList.TRANSFER.getdescription());
			}
			if (plugin.playerPermission(player, PermissionNodes.USER)) {
				commands.add(commandList.ADDUSER.getdescription());
				commands.add(commandList.REMOVEUSER.getdescription());
			}
			if (plugin.playerPermission(player, PermissionNodes.PASSWORD)) {
				commands.add(commandList.PASSWORD.getdescription());
			}
			if (plugin.playerPermission(player, PermissionNodes.CLOSE)) {
				commands.add(commandList.CLOSE.getdescription());
			}
			if (plugin.playerPermission(player, PermissionNodes.LOAN)) {
				commands.add(commandList.LOAN.getdescription());
				commands.add(commandList.PAY.getdescription());
			}
		} else {
			if (plugin.playerPermission(player, PermissionNodes.OPEN)) {
				commands.add(commandList.OPEN.getdescription());
				commands.add(commandList.INFO.getdescription());
				commands.add(commandList.BALANCE.getdescription());
			}
			if (plugin.playerPermission(player, PermissionNodes.DEPOSIT)) {
				commands.add(commandList.DEPOSIT.getdescription());
			}
			if (plugin.playerPermission(player, PermissionNodes.WITHDRAW)) {
				commands.add(commandList.WITHDRAW.getdescription());
			}
			if (plugin.playerPermission(player, PermissionNodes.TRANSFER)) {
				commands.add(commandList.TRANSFER.getdescription());
			}
			if (plugin.playerPermission(player, PermissionNodes.USER)) {
				commands.add(commandList.ADDUSER.getdescription());
				commands.add(commandList.REMOVEUSER.getdescription());
			}
			if (plugin.playerPermission(player, PermissionNodes.PASSWORD)) {
				commands.add(commandList.PASSWORD.getdescription());
			}
			if (plugin.playerPermission(player, PermissionNodes.CLOSE)) {
				commands.add(commandList.CLOSE.getdescription());
			}
			if (plugin.playerPermission(player, PermissionNodes.LOAN)) {
				commands.add(commandList.LOAN.getdescription());
				commands.add(commandList.PAY.getdescription());
			}
		}
		
		player.sendMessage(ChatColor.DARK_GREEN + "Bank Account Help - Page " + Page + " of " + (int)Math.ceil(commands.size()/7));
		player.sendMessage(ChatColor.DARK_GREEN + "This is used mainly to shared bank accounts");
		player.sendMessage(ChatColor.GOLD + "/account help [page]");
		if (commands.size() > 7) {
			int start = Page*7;
			int temp = 0;
			for (String command : commands) {
				temp++;
				if (temp >= start && temp < start+7) {
					player.sendMessage(ChatColor.GOLD + "/account " + command);
				}
			}
		} else {
			for (String command : commands) {
				player.sendMessage(ChatColor.GOLD + "/account " + command);
			}
		}
	}
}
