package dk.earthgame.TAT.BankAccount;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dk.earthgame.TAT.BankAccount.System.CommandList;
import dk.earthgame.TAT.BankAccount.System.PermissionNodes;
import dk.earthgame.TAT.BankAccount.System.TransactionTypes;
import dk.earthgame.TAT.BankAccount.System.UserSaves;

public class BankAccountCommandExecutor implements CommandExecutor {
	private BankAccount plugin;
	
	public BankAccountCommandExecutor(BankAccount instantiate) {
		plugin = instantiate;
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		//Is the sender a player or not?
		boolean isPlayer = (sender instanceof Player);
		if (!isPlayer) {
			sender.sendMessage("Non-players can't use BankAccount");
			return true;
		}
		
		String sendername = ((Player)sender).getName();
		if (label.equalsIgnoreCase("account")) {
			//Do the player have access to use BankAccount
			if (!plugin.playerPermission((Player)sender,PermissionNodes.ACCESS)) {
				sender.sendMessage(ChatColor.DARK_RED + "You don't have access to use BankAccount");
				return true;
			}

			//Are you in an area? (If areas are enabled)
	  		if (plugin.settings.Areas) {
	  			CommandList foundCommand = CommandList.valueOf(args[0].toUpperCase());
	  			if (foundCommand != null) {
	  				if (foundCommand.getRequireArea() && !plugin.inArea(((Player)sender).getWorld().getName(), ((Player)sender).getLocation())) {
	  					sender.sendMessage("ATM: You're not in bank area");
	  					return true;
	  				}
	  			}
  			}

	  		if (args.length > 0) {
//OPEN
  				if (args[0].equalsIgnoreCase("open") && args.length >= 2) {
  					if (!plugin.playerPermission((Player)sender,PermissionNodes.OPEN)) {
  						sender.sendMessage("You don't have permission to use this command");
  						return true;
  					}
  					
  					if (plugin.stringWidth(args[1]) > 150) {
  						sender.sendMessage("ATM: " + ChatColor.RED + "Accountname to long");
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
  						if (plugin.openAccount(args[1], players, sendername)) {
  							plugin.addTransaction(sendername, args[1], TransactionTypes.OPEN, 0.00);
  							sender.sendMessage("ATM: " + ChatColor.GREEN + args[1] + " created");
  							players = "";
  	  						players += sendername;
  	  						if (args.length >= 3) {
  	  							for (int i = 3;i<=args.length;i++) {
  	  								players += "," + args[i-1];
  	  							}
  	  						}
  							sender.sendMessage(ChatColor.WHITE + "Players: " + ChatColor.GREEN + players);
  						} else {
  							sender.sendMessage("ATM: " + ChatColor.RED + "Couldn't create bank account");
  						}
  					}
//BALANCE
  				} else if (args[0].equalsIgnoreCase("balance") && args.length >= 2) {
  					if (!plugin.playerPermission((Player)sender,PermissionNodes.OPEN)) {
  						sender.sendMessage("You don't have permission to use this command");
  						return true;
  					}
  					
  					if (!plugin.accountExists(args[1])) {
  						sender.sendMessage("ATM: " + ChatColor.RED + "Account not found");
  					} else {
  						if (plugin.accessAccount(args[1], (Player)sender, false)) {
  							sender.sendMessage("ATM: Balance of " + args[1] + ": " + ChatColor.GREEN + plugin.Method.format(plugin.getBalance(args[1])));
  						} else {
  							sender.sendMessage("ATM: " + ChatColor.RED + "You don't have access to this account!");
  						}
  					}
//INFO
  				} else if (args[0].equalsIgnoreCase("info") && args.length >= 2) {
  					if (!plugin.playerPermission((Player)sender,PermissionNodes.OPEN)) {
  						sender.sendMessage("You don't have permission to use this command");
  						return true;
  					}
  					
  					if (!plugin.accountExists(args[1])) {
  						sender.sendMessage("ATM: " + ChatColor.RED + "Account not found");
  					} else {
  						if (plugin.accessAccount(args[1], (Player)sender, false)) {
  							sender.sendMessage("ATM: Balance of " + args[1] + ": " + ChatColor.GREEN + plugin.Method.format(plugin.getBalance(args[1])));
  							sender.sendMessage(ChatColor.WHITE + "Owners: " + ChatColor.GREEN + plugin.getOwners(args[1]));
  							sender.sendMessage(ChatColor.WHITE + "Users: " + ChatColor.GREEN + plugin.getUsers(args[1]));
  						} else {
  							sender.sendMessage("ATM: " + ChatColor.RED + "You don't have access to this account!");
  						}
  					}
//LIST
  				} else if (args[0].equalsIgnoreCase("list")) {
  					if (!plugin.playerPermission((Player)sender,PermissionNodes.LIST)) {
  						sender.sendMessage("You don't have permission to use this command");
  						return true;
  					}
  					
  					List<String> accounts = plugin.accountList((Player)sender);
  					int tmpWidth = 0;
  					String output = "";
  					for (String account : accounts) {
  						plugin.console.info(account);
  						if (tmpWidth == 0) {
  							tmpWidth = plugin.stringWidth(account);
  							output = account;
  						} else {
  							while (tmpWidth < 160) {
  								output += " ";
  								tmpWidth += plugin.stringWidth(" ");
  							}
  							output += account;
  	  						sender.sendMessage(output);
  	  						tmpWidth = 0;
  	  						output = "";
  						}
  					}
  					//If not all accounts is sent
  					if (output != "") {
  						sender.sendMessage(output);
  					}
//ADDUSER
  				} else if (args[0].equalsIgnoreCase("adduser") && args.length >= 3) {
  					if (!plugin.playerPermission((Player)sender,PermissionNodes.USER)) {
  						sender.sendMessage("You don't have permission to use this command");
  						return true;
  					}
  					
  					if (!plugin.accountExists(args[1])) {
  						sender.sendMessage("ATM: " + ChatColor.RED + "Account not found");
  					} else if (plugin.accessAccount(args[1], (Player)sender, true)) {
  						if (plugin.addUser(args[1], args[2])) {
  							plugin.addTransaction(sendername, args[1], TransactionTypes.USER_ADD, 0.00);
  							sender.sendMessage("ATM: " + ChatColor.GREEN + args[2] + " added to account");
  						} else {
  							sender.sendMessage("ATM: " + ChatColor.RED + "Something went wrong");
  						}
  					} else {
  						sender.sendMessage("ATM: " + ChatColor.RED + "You don't have access to this account!");
  					}
//REMOVEUSER
  				} else if (args[0].equalsIgnoreCase("removeuser") && args.length >= 3) {
  					if (!plugin.playerPermission((Player)sender,PermissionNodes.USER)) {
  						sender.sendMessage("You don't have permission to use this command");
  						return true;
  					}
  					
  					if (!plugin.accountExists(args[1])) {
  						sender.sendMessage("ATM: " + ChatColor.RED + "Account not found");
  					} else if (plugin.accessAccount(args[1], (Player)sender, true)) {
  						if (plugin.removeUser(args[1], args[2])) {
  							plugin.addTransaction(sendername, args[1], TransactionTypes.USER_REMOVE, 0.00);
  							sender.sendMessage("ATM: " + ChatColor.GREEN + args[2] + " removed from account");
  						} else {
  							sender.sendMessage("ATM: " + ChatColor.RED + "Something went wrong");
  						}
  					} else {
  						sender.sendMessage("ATM: " + ChatColor.RED + "You don't have access to this account!");
  					}
//ADDOWNER
  				} else if (args[0].equalsIgnoreCase("addowner") && args.length >= 3) {
  					if (!plugin.playerPermission((Player)sender,PermissionNodes.USER)) {
  						sender.sendMessage("You don't have permission to use this command");
  						return true;
  					}
  					
  					if (!plugin.accountExists(args[1])) {
  						sender.sendMessage("ATM: " + ChatColor.RED + "Account not found");
  					} else if (plugin.accessAccount(args[1], (Player)sender, true)) {
  						if (plugin.addOwner(args[1], args[2])) {
  							plugin.addTransaction(sendername, args[1], TransactionTypes.OWNER_ADD, 0.00);
  							sender.sendMessage("ATM: " + ChatColor.GREEN + args[2] + " added to account");
  						} else {
  							sender.sendMessage("ATM: " + ChatColor.RED + "Something went wrong");
  						}
  					} else {
  						sender.sendMessage("ATM: " + ChatColor.RED + "You don't have access to this account!");
  					}
//REMOVEOWNER
  				} else if (args[0].equalsIgnoreCase("removeowner") && args.length >= 3) {
  					if (!plugin.playerPermission((Player)sender,PermissionNodes.USER)) {
  						sender.sendMessage("You don't have permission to use this command");
  						return true;
  					}
  					
  					if (!plugin.accountExists(args[1])) {
  						sender.sendMessage("ATM: " + ChatColor.RED + "Account not found");
  					} else if (plugin.accessAccount(args[1], (Player)sender, true)) {
  						if (plugin.removeOwner(args[1], args[2])) {
  							plugin.addTransaction(sendername, args[1], TransactionTypes.OWNER_REMOVE, 0.00);
  							sender.sendMessage("ATM: " + ChatColor.GREEN + args[2] + " removed from account");
  						} else {
  							sender.sendMessage("ATM: " + ChatColor.RED + "Something went wrong");
  						}
  					} else {
  						sender.sendMessage("ATM: " + ChatColor.RED + "You don't have access to this account!");
  					}
//PASSWORD
  				} else if (args[0].equalsIgnoreCase("password") && args.length >= 2) {
  					if (!plugin.playerPermission((Player)sender,PermissionNodes.PASSWORD)) {
  						sender.sendMessage("You don't have permission to use this command");
  						return true;
  					}
  					
  					if (!plugin.accountExists(args[1])) {
  						sender.sendMessage("ATM: " + ChatColor.RED + "Account not found");
  					} else if (plugin.accessAccount(args[1], (Player)sender, true)) {
	  					String password = "";
						if (args.length >= 3) {
							password = args[2];
						}
						password = plugin.PasswordSystem.passwordCrypt(password);
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
//DEPOSIT
  				} else if (args[0].equalsIgnoreCase("deposit") && args.length >= 3) {
  					if (!plugin.playerPermission((Player)sender,PermissionNodes.DEPOSIT)) {
  						sender.sendMessage("You don't have permission to use this command");
  						return true;
  					}
  					
  					if (!plugin.accountExists(args[1])) {
  						sender.sendMessage("ATM: " + ChatColor.RED + "Account not found");
  					} else if (plugin.accessAccount(args[1], (Player)sender, false) || plugin.settings.DepositAll) {
  						if (Double.parseDouble(args[2]) <= 0.00) {
  							sender.sendMessage("ATM: " + ChatColor.RED + "Please enter value higher than 0");
  							return true;
  						}
  						if (plugin.ATM(args[1], sendername, "deposit", Double.parseDouble(args[2]), "")) {
  							plugin.addTransaction(sendername, args[1], TransactionTypes.DEPOSIT, Double.parseDouble(args[2]));
  							sender.sendMessage("ATM: " + ChatColor.GREEN + plugin.Method.format(Double.parseDouble(args[2])) + " added to " + args[1]);
  						} else {
							plugin.addTransaction(sendername, args[1], TransactionTypes.TRANSACTION_CANCELED, Double.parseDouble(args[2]));
  							sender.sendMessage("ATM: " + ChatColor.RED + "Couldn't deposit, are you sure you have enough money?");
  						}
  					} else {
  						sender.sendMessage("ATM: " + ChatColor.RED + "You don't have access to this account!");
  					}
//WITHDRAW
  				} else if (args[0].equalsIgnoreCase("withdraw") && args.length >= 3) {
  					if (!plugin.playerPermission((Player)sender,PermissionNodes.WITHDRAW)) {
  						sender.sendMessage("You don't have permission to use this command");
  						return true;
  					}
  					
  					if (plugin.accessAccount(args[1], (Player)sender, true)) {
  						if (Double.parseDouble(args[2]) <= 0.00) {
  							sender.sendMessage("ATM: " + ChatColor.RED + "Please enter value higher than 0");
  							return true;
  						}
  						String password = "";
  						if (args.length >= 4) {
  							password = args[3];
  						}
  						if (plugin.ATM(args[1], sendername, "withdraw", Double.parseDouble(args[2]), password)) {
  							plugin.addTransaction(sendername, args[1], TransactionTypes.WITHDRAW, Double.parseDouble(args[2]));
  							sender.sendMessage("ATM: " + ChatColor.GREEN + plugin.Method.format(Double.parseDouble(args[2])) + " withdrawed from " + args[1]);
  						} else {
  							sender.sendMessage("ATM: " + ChatColor.RED + "Couldn't withdraw, are you sure you have enough money on account?");
  						}
  					} else {
  						sender.sendMessage("ATM: " + ChatColor.RED + "You don't have access to this account!");
  					}
//TRANSFER
  				} else if (args[0].equalsIgnoreCase("transfer") && args.length >= 4) {
  					if (!plugin.playerPermission((Player)sender,PermissionNodes.TRANSFER)) {
  						sender.sendMessage("You don't have permission to use this command");
  						return true;
  					}
  					
  					if (plugin.accessAccount(args[1], (Player)sender, true)) {
  						if (Double.parseDouble(args[3]) <= 0.00) {
  							sender.sendMessage("ATM: " + ChatColor.RED + "Please enter value higher than 0");
  							return true;
  						}
  						if (plugin.accountExists(args[2])) {
							String password = "";
							if (args.length >= 5) {
								password = args[4];
							}
							if (plugin.ATM(args[1], args[2], "transfer", Double.parseDouble(args[3]), password)) {
								plugin.addTransaction(sendername, args[1], TransactionTypes.TRANSFER_WITHDRAW, Double.parseDouble(args[3]));
								plugin.addTransaction("SYSTEM", args[2], TransactionTypes.TRANSFER_DEPOSIT, Double.parseDouble(args[3]));
								sender.sendMessage("ATM: " + ChatColor.GREEN + plugin.Method.format(Double.parseDouble(args[3])) + " transfered from " + args[1] + " to " + args[2]);
							} else {
								plugin.addTransaction(sendername, args[1], TransactionTypes.TRANSACTION_CANCELED, Double.parseDouble(args[3]));
								sender.sendMessage("ATM: " + ChatColor.RED + "Couldn't transfer, are you sure you have enough money on account?");
							}
  						} else {
  							sender.sendMessage("ATM: " + ChatColor.RED + "Reciever account not found!");
  						}
  					} else {
  						sender.sendMessage("ATM: " + ChatColor.RED + "You don't have access to this account!");
  					}
//LOAN - CREATE
  				} else if (args[0].equalsIgnoreCase("loan") && args.length >= 2) {
  					if (!plugin.playerPermission((Player)sender,PermissionNodes.LOAN)) {
  						sender.sendMessage("You don't have permission to use this command");
  						return true;
  					}
  					
  					if (!plugin.LoanSystem.LoanActive) {
  						sender.sendMessage("ATM: Loans not activated");
  					}
  					if (!plugin.LoanSystem.haveLoan(sendername) && plugin.getSaved(sendername).getBounty() == 0.00) {
  						plugin.addTransaction(sendername, null, TransactionTypes.LOAN_START, Double.parseDouble(args[1]));
  						if (plugin.LoanSystem.addLoan(sendername, Double.parseDouble(args[1]))) {
  							sender.sendMessage("ATM: " + ChatColor.GREEN + plugin.Method.format(Double.parseDouble(args[1])) + " loaned.");
  						} else {
  							sender.sendMessage("ATM: " + ChatColor.RED + "Couldn't create loan!");
  						}
  					} else if (plugin.getSaved(sendername).getBounty() > 0.00) {
  						sender.sendMessage("ATM: " + ChatColor.RED + "You have a bounty on your head!");
  					} else {
  						sender.sendMessage("ATM: " + ChatColor.RED + "You already have a loan, pay it first!");
  					}
//LOAN - STATUS
  				} else if (args[0].equalsIgnoreCase("loan") && args.length == 1) {
  					if (!plugin.playerPermission((Player)sender,PermissionNodes.LOAN)) {
  						sender.sendMessage("You don't have permission to use this command");
  						return true;
  					}
  					
  					if (!plugin.LoanSystem.LoanActive) {
  						sender.sendMessage("ATM: Loans not activated");
  					}
  					if (plugin.LoanSystem.haveLoan(sendername)) {
  						Loan playerLoan = plugin.LoanSystem.getLoan(sendername);
  						sender.sendMessage("ATM: " + ChatColor.GREEN + "Found loan.");
  						sender.sendMessage(ChatColor.GOLD + "Amount+rates: " + ChatColor.WHITE + plugin.Method.format(playerLoan.totalamount));
  						sender.sendMessage(ChatColor.GOLD + "Remaining: " + ChatColor.WHITE + plugin.Method.format(playerLoan.remaining));
  					} else {
  						sender.sendMessage("ATM: " + ChatColor.RED + "You don't have a loan.");
  					}
//PAY
  				} else if (args[0].equalsIgnoreCase("pay") && args.length >= 2) {
  					if (!plugin.playerPermission((Player)sender,PermissionNodes.LOAN)) {
  						sender.sendMessage("You don't have permission to use this command");
  						return true;
  					}
  					
  					if (!plugin.LoanSystem.LoanActive) {
  						sender.sendMessage("ATM: Loans not activated");
  					}
  					if (plugin.LoanSystem.haveLoan(sendername)) {
  						double paid = plugin.LoanSystem.payment(sendername, Double.parseDouble(args[1]));
  						if (paid > 0.00) {
  							plugin.addTransaction(sendername, null, TransactionTypes.LOAN_PAYMENT, paid);
  							sender.sendMessage("ATM: " + ChatColor.GREEN + plugin.Method.format(paid) + " paid off your loan.");
  						} else {
  							sender.sendMessage("ATM: " + ChatColor.RED + "Couldn't pay!");
  						}
  					} else {
  						sender.sendMessage("ATM: " + ChatColor.RED + "You don't have a loan");
  					}
//CLOSE
  				} else if (args[0].equalsIgnoreCase("close") && args.length >= 2) {
  					if (!plugin.playerPermission((Player)sender,PermissionNodes.CLOSE)) {
  						sender.sendMessage("You don't have permission to use this command");
  						return true;
  					}
  					
  					if (plugin.accessAccount(args[1], (Player)sender, true)) {
  						String password = "";
  						if (args.length >= 3) {
  							password = args[2];
  						}
  						double money = plugin.getBalance(args[1]);
  						if (plugin.closeAccount(args[1], sendername, password)) {
  							if (money > 0) {
  								plugin.addTransaction(sendername, args[1], TransactionTypes.WITHDRAW, money);
  								sender.sendMessage("ATM: " + plugin.Method.format(money) + " withdrawed");
  							}
  							plugin.addTransaction(sendername, args[1], TransactionTypes.CLOSE, 0.00);
  							sender.sendMessage("ATM: Account closed");
  						} else {
  							sender.sendMessage("ATM: Wrong accountname and/or password");
  						}
  					} else {
  						sender.sendMessage("ATM: " + ChatColor.RED + "You don't have access to this account!");
  					}
//SELECT
  				} else if (args[0].equalsIgnoreCase("select")) {
  					if (!plugin.playerPermission((Player)sender,PermissionNodes.ADMIN)) {
  						sender.sendMessage("You don't have permission to use this command");
  						return true;
  					} else {
  						UserSaves mySave = plugin.getSaved(sendername);
  						if (mySave.isSelecting()) {
  							mySave.isSelecting(false);
							sender.sendMessage("ATM: " + ChatColor.GREEN + "No longer selecting area");
						} else {
  							mySave.isSelecting(true);
							sender.sendMessage("ATM: " + ChatColor.GREEN + "Selecting area active, send command again to deactivate");
						}
  					}
//SETAREA
  				} else if (args[0].equalsIgnoreCase("setarea") && args.length >= 2) {
  					if (!plugin.playerPermission((Player)sender,PermissionNodes.ADMIN)) {
  						sender.sendMessage("You don't have permission to use this command");
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
//REMOVEAREA
  				} else if (args[0].equalsIgnoreCase("removearea") && args.length >= 2) {
  					if (!plugin.playerPermission((Player)sender,PermissionNodes.ADMIN)) {
  						sender.sendMessage("You don't have permission to use this command");
  						return true;
  					} else {
  						if (plugin.removeArea(args[1])) {
  							sender.sendMessage("ATM: " + ChatColor.GREEN + "Area removed");
  						} else {
  							sender.sendMessage("ATM: " + ChatColor.RED + "Something went wrong. Please try again.");
  						}
  					}
//HELP
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
	
	/**
	 * Show help to a player
	 * 
	 * @param player - The player
	 * @param page - Page number
	 * @since 0.5
	 * @see #showHelp(CommandSender sender, int page)
	 */
	public void showHelp(Player player, int page) {
		List<String> commands = new ArrayList<String>();
		if (plugin.playerPermission(player, PermissionNodes.EXTENDED) || plugin.playerPermission(player, PermissionNodes.ADMIN)) {
			commands.add(CommandList.OPEN.getDescription());
			commands.add(CommandList.INFO.getDescription());
			commands.add(CommandList.BALANCE.getDescription());
			commands.add(CommandList.LIST.getDescription());
			commands.add(CommandList.DEPOSIT.getDescription());
			commands.add(CommandList.WITHDRAW.getDescription());
			commands.add(CommandList.TRANSFER.getDescription());
			commands.add(CommandList.ADDOWNER.getDescription());
			commands.add(CommandList.REMOVEOWNER.getDescription());
			commands.add(CommandList.ADDUSER.getDescription());
			commands.add(CommandList.REMOVEUSER.getDescription());
			commands.add(CommandList.PASSWORD.getDescription());
			commands.add(CommandList.CLOSE.getDescription());
			commands.add(CommandList.LOAN.getDescription());
			commands.add(CommandList.PAY.getDescription());
			if (plugin.playerPermission(player, PermissionNodes.ADMIN)) {
				commands.add(CommandList.SELECT.getDescription());
				commands.add(CommandList.SETAREA.getDescription());
				commands.add(CommandList.REMOVEAREA.getDescription());
			}
		} else if (plugin.playerPermission(player, PermissionNodes.BASIC)) {
			commands.add(CommandList.OPEN.getDescription());
			commands.add(CommandList.INFO.getDescription());
			commands.add(CommandList.BALANCE.getDescription());
			commands.add(CommandList.LIST.getDescription());
			commands.add(CommandList.DEPOSIT.getDescription());
			commands.add(CommandList.WITHDRAW.getDescription());
			if (plugin.playerPermission(player, PermissionNodes.TRANSFER)) {
				commands.add(CommandList.TRANSFER.getDescription());
			}
			if (plugin.playerPermission(player, PermissionNodes.USER)) {
				commands.add(CommandList.ADDUSER.getDescription());
				commands.add(CommandList.REMOVEUSER.getDescription());
				commands.add(CommandList.ADDOWNER.getDescription());
				commands.add(CommandList.REMOVEOWNER.getDescription());
			}
			if (plugin.playerPermission(player, PermissionNodes.PASSWORD)) {
				commands.add(CommandList.PASSWORD.getDescription());
			}
			if (plugin.playerPermission(player, PermissionNodes.CLOSE)) {
				commands.add(CommandList.CLOSE.getDescription());
			}
			if (plugin.playerPermission(player, PermissionNodes.LOAN)) {
				commands.add(CommandList.LOAN.getDescription());
				commands.add(CommandList.PAY.getDescription());
			}
		} else {
			if (plugin.playerPermission(player, PermissionNodes.OPEN)) {
				commands.add(CommandList.OPEN.getDescription());
				commands.add(CommandList.INFO.getDescription());
				commands.add(CommandList.BALANCE.getDescription());
			}
			if (plugin.playerPermission(player, PermissionNodes.LIST)) {
				commands.add(CommandList.LIST.getDescription());
			}
			if (plugin.playerPermission(player, PermissionNodes.DEPOSIT)) {
				commands.add(CommandList.DEPOSIT.getDescription());
			}
			if (plugin.playerPermission(player, PermissionNodes.WITHDRAW)) {
				commands.add(CommandList.WITHDRAW.getDescription());
			}
			if (plugin.playerPermission(player, PermissionNodes.TRANSFER)) {
				commands.add(CommandList.TRANSFER.getDescription());
			}
			if (plugin.playerPermission(player, PermissionNodes.USER)) {
				commands.add(CommandList.ADDUSER.getDescription());
				commands.add(CommandList.REMOVEUSER.getDescription());
				commands.add(CommandList.ADDOWNER.getDescription());
				commands.add(CommandList.REMOVEOWNER.getDescription());
			}
			if (plugin.playerPermission(player, PermissionNodes.PASSWORD)) {
				commands.add(CommandList.PASSWORD.getDescription());
			}
			if (plugin.playerPermission(player, PermissionNodes.CLOSE)) {
				commands.add(CommandList.CLOSE.getDescription());
			}
			if (plugin.playerPermission(player, PermissionNodes.LOAN)) {
				commands.add(CommandList.LOAN.getDescription());
				commands.add(CommandList.PAY.getDescription());
			}
		}
		
		int pages = (int)Math.max(1, Math.ceil(commands.size()/7)+1);
		//Only show pages that exists
		if (page > pages) {
			page = pages;
		} else if (page < 0) {
			page = 1;
		}
		player.sendMessage(ChatColor.DARK_GREEN + "Bank Account Help - Page " + page + " of " + pages);
		player.sendMessage(ChatColor.DARK_GREEN + "This is used mainly to shared bank accounts");
		player.sendMessage(ChatColor.GOLD + "/account help [page]");
		if (commands.size() > 7) {
			int start = (page-1)*7;
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
	
	/**
	 * Show help to a CommandSender
	 * 
	 * @param sender - The CommandSender (Player)
	 * @param page - Page number
	 * @since 0.5
	 * @see #showHelp(Player player, int page)
	 */
	public void showHelp(CommandSender sender, int page) { showHelp((Player)sender,page); }
}
