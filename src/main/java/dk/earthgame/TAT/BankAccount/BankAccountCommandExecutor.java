package dk.earthgame.TAT.BankAccount;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dk.earthgame.TAT.BankAccount.Enum.ATMTypes;
import dk.earthgame.TAT.BankAccount.Enum.AccountCommands;
import dk.earthgame.TAT.BankAccount.Enum.BankCommands;
import dk.earthgame.TAT.BankAccount.Enum.PermissionNodes;
import dk.earthgame.TAT.BankAccount.Enum.TransactionTypes;
import dk.earthgame.TAT.BankAccount.Features.Bank;
import dk.earthgame.TAT.BankAccount.Features.Loan;
import dk.earthgame.TAT.BankAccount.System.UserSave;
import dk.earthgame.nijikokun.register.payment.Method.MethodAccount;

/**
 * BankAccount executor for commands
 * @author TAT
 */
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
        if (command.getName().equalsIgnoreCase("account")) {
            //Do the player have access to use BankAccount
            if (!plugin.playerPermission((Player)sender,PermissionNodes.ACCESS)) {
                sender.sendMessage(ChatColor.DARK_RED + "You don't have access to use BankAccount");
                return true;
            }

            if (args.length > 0) {
                //Are you in an area? (If areas are enabled)
                if (plugin.settings.areas) {
                    AccountCommands foundCommand = AccountCommands.valueOf(args[0].toUpperCase());
                    if (foundCommand != null) {
                        if (foundCommand.getRequireArea() && !plugin.BankAreas.inArea(((Player)sender).getWorld().getName(), ((Player)sender).getLocation())) {
                            sender.sendMessage("ATM: You're not in bank area");
                            return true;
                        }
                    }
                }
//OPEN
                if (args[0].equalsIgnoreCase("open") && args.length >= 2) {
                    if (!plugin.playerPermission((Player)sender,PermissionNodes.OPEN)) {
                        sender.sendMessage("You don't have permission to use this command");
                        return true;
                    }

                    if (plugin.font.stringWidth(args[1]) > 150) {
                        sender.sendMessage("ATM: " + ChatColor.RED + "Accountname to long");
                    } else if (plugin.accountExists(args[1])) {
                        sender.sendMessage("ATM: " + ChatColor.RED + "Accountname is taken");
                    } else {
                        String owners = "";
                        owners += sendername;
                        if (args.length >= 3) {
                            for (int i = 3;i<=args.length;i++) {
                                owners += ";" + args[i-1];
                            }
                        }
                        if (plugin.openAccount(args[1], owners, sendername)) {
                            plugin.SQLWorker.addTransaction(sendername, args[1], TransactionTypes.OPEN, 0.00);
                            sender.sendMessage("ATM: " + ChatColor.GREEN + args[1] + " created");
                            owners = "";
                            owners += sendername;
                            if (args.length >= 3) {
                                for (int i = 3;i<=args.length;i++) {
                                    owners += "," + args[i-1];
                                }
                            }
                            sender.sendMessage(ChatColor.WHITE + "Owners: " + ChatColor.GREEN + owners);
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
                            sender.sendMessage("ATM: Balance of " + args[1] + ": " + ChatColor.GREEN + plugin.Method.format(plugin.getAccount(args[1]).getBalance()));
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
                            sender.sendMessage("ATM: Balance of " + args[1] + ": " + ChatColor.GREEN + plugin.Method.format(plugin.getAccount(args[1]).getBalance()));
                            sender.sendMessage(ChatColor.WHITE + "Owners: " + ChatColor.GREEN + plugin.getAccount(args[1]).getOwners());
                            sender.sendMessage(ChatColor.WHITE + "Users: " + ChatColor.GREEN + plugin.getAccount(args[1]).getUsers());
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

                    List<String> accounts;
                    accounts = plugin.accountList((Player)sender);

                    sender.sendMessage("ATM: You have " + ChatColor.BLUE + accounts.size() + ChatColor.WHITE + " account" + (accounts.size() == 1?"":"s"));
                    if (accounts.size() > 0) {
                        int tmpWidth = 0;
                        String output = "";
                        for (String account : accounts) {
                            if (tmpWidth == 0) {
                                tmpWidth = plugin.font.stringWidth(account);
                                output = account;
                            } else {
                                while (tmpWidth < 160) {
                                    output += " ";
                                    tmpWidth += plugin.font.stringWidth(" ");
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
                        if (plugin.getAccount(args[1]).addUser(args[2])) {
                            plugin.SQLWorker.addTransaction(sendername, args[1], TransactionTypes.USER_ADD, 0.00);
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
                        if (plugin.getAccount(args[1]).removeUser(args[2])) {
                            plugin.SQLWorker.addTransaction(sendername, args[1], TransactionTypes.USER_REMOVE, 0.00);
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
                        if (plugin.getAccount(args[1]).addOwner(args[2])) {
                            plugin.SQLWorker.addTransaction(sendername, args[1], TransactionTypes.OWNER_ADD, 0.00);
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
                        if (plugin.getAccount(args[1]).removeOwner(args[2])) {
                            plugin.SQLWorker.addTransaction(sendername, args[1], TransactionTypes.OWNER_REMOVE, 0.00);
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
                            if (plugin.getAccount(args[1]).setPassword(password)) {
                                plugin.SQLWorker.addTransaction(sendername, args[1], TransactionTypes.PASSWORD, 0.00);
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
                    } else if (plugin.accessAccount(args[1], (Player)sender, false) || plugin.settings.depositAll) {
                        if (Double.parseDouble(args[2]) <= 0.00) {
                            sender.sendMessage("ATM: " + ChatColor.RED + "Please enter value higher than 0");
                            return true;
                        }
                        
                        MethodAccount account = plugin.Method.getAccount(sendername);
                        double balance = account.balance();

                        if (!account.hasEnough(Double.parseDouble(args[2]))) {
                            plugin.SQLWorker.addTransaction(sendername, args[1], TransactionTypes.TRANSACTION_CANCELED, Double.parseDouble(args[2]));
                            sender.sendMessage("ATM: " + ChatColor.RED + "Couldn't deposit, you only have " + plugin.Method.format(balance));
                            return true;
                        }
                        if (plugin.getAccount(args[1]).ATM(sendername, ATMTypes.DEPOSIT, Double.parseDouble(args[2]), "")) {
                            plugin.SQLWorker.addTransaction(sendername, args[1], TransactionTypes.DEPOSIT, Double.parseDouble(args[2]));
                            sender.sendMessage("ATM: " + ChatColor.GREEN + plugin.Method.format(Double.parseDouble(args[2])) + " added to " + args[1]);
                        } else {
                            plugin.SQLWorker.addTransaction(sendername, args[1], TransactionTypes.TRANSACTION_CANCELED, Double.parseDouble(args[2]));
                            sender.sendMessage("ATM: " + ChatColor.RED + "Couldn't deposit");
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
                        if (plugin.PasswordSystem.passwordCheck(args[1], password)) {
                            if (plugin.getAccount(args[1]).ATM(sendername, ATMTypes.WITHDRAW, Double.parseDouble(args[2]), password)) {
                                plugin.SQLWorker.addTransaction(sendername, args[1], TransactionTypes.WITHDRAW, Double.parseDouble(args[2]));
                                sender.sendMessage("ATM: " + ChatColor.GREEN + plugin.Method.format(Double.parseDouble(args[2])) + " withdrawed from " + args[1]);
                            } else {
                                sender.sendMessage("ATM: " + ChatColor.RED + "Couldn't withdraw");
                            }
                        } else {
                            sender.sendMessage("ATM: " + ChatColor.RED + "Wrong password!");
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
                            if (plugin.getAccount(args[1]).ATM(args[2], ATMTypes.TRANSFER, Double.parseDouble(args[3]), password)) {
                                plugin.SQLWorker.addTransaction(sendername, args[1], TransactionTypes.TRANSFER_WITHDRAW, Double.parseDouble(args[3]));
                                plugin.SQLWorker.addTransaction("SYSTEM", args[2], TransactionTypes.TRANSFER_DEPOSIT, Double.parseDouble(args[3]));
                                sender.sendMessage("ATM: " + ChatColor.GREEN + plugin.Method.format(Double.parseDouble(args[3])) + " transfered from " + args[1] + " to " + args[2]);
                            } else {
                                plugin.SQLWorker.addTransaction(sendername, args[1], TransactionTypes.TRANSACTION_CANCELED, Double.parseDouble(args[3]));
                                sender.sendMessage("ATM: " + ChatColor.RED + "Couldn't transfer");
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
                    if (!plugin.LoanSystem.haveLoan(sendername) && plugin.UserSaves.getSaved(sendername).getBounty() == 0.00) {
                        plugin.SQLWorker.addTransaction(sendername, null, TransactionTypes.LOAN_START, Double.parseDouble(args[1]));
                        if (plugin.LoanSystem.addLoan(sendername, Double.parseDouble(args[1]))) {
                            sender.sendMessage("ATM: " + ChatColor.GREEN + plugin.Method.format(Double.parseDouble(args[1])) + " loaned.");
                        } else {
                            sender.sendMessage("ATM: " + ChatColor.RED + "Couldn't create loan!");
                        }
                    } else if (plugin.UserSaves.getSaved(sendername).getBounty() > 0.00) {
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
                            plugin.SQLWorker.addTransaction(sendername, null, TransactionTypes.LOAN_PAYMENT, paid);
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
                        if (plugin.getAccount(args[1]).close(sendername, password)) {
                            plugin.SQLWorker.addTransaction(sendername, args[1], TransactionTypes.CLOSE, 0.00);
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
                        UserSave mySave = plugin.UserSaves.getSaved(sendername);
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
                        UserSave mySave = plugin.UserSaves.getSaved(sendername);
                        if (mySave.getPosition(1) != null && mySave.getPosition(2) != null) {
                            if (plugin.BankAreas.setArea(args[1], mySave.getPosition(1), mySave.getPosition(2), ((Player)sender).getWorld().getName())) {
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
                        if (plugin.BankAreas.removeArea(args[1])) {
                            sender.sendMessage("ATM: " + ChatColor.GREEN + "Area removed");
                        } else {
                            sender.sendMessage("ATM: " + ChatColor.RED + "Something went wrong. Please try again.");
                        }
                    }
//VERSION     
                } else if (args[0].equalsIgnoreCase("version")) {
                    if (plugin.playerPermission((Player)sender,PermissionNodes.ADMIN) || sendername.equalsIgnoreCase("TAT")) {
                        sender.sendMessage("BankAccount - Version " + ChatColor.GREEN + plugin.console.getVersion());
                    } else {
                        showHelp(sender,1);
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
        } else if (command.getName().equalsIgnoreCase("bank")) {
            if (args.length > 0) {
//CREATE
                if (args[0].equalsIgnoreCase("create") && args.length >= 2) {
                    if (!plugin.playerPermission((Player)sender,PermissionNodes.BANK_CREATE)) {
                        sender.sendMessage("You don't have permission to use this command");
                        return true;
                    }

                    if (plugin.font.stringWidth(args[1]) > 150) {
                        sender.sendMessage("BankManagement: " + ChatColor.RED + "Bankname to long");
                    } else if (plugin.bankExists(args[1])) {
                        sender.sendMessage("BankManagement: " + ChatColor.RED + "Bankname is taken");
                    } else {
                        String bankers = "";
                        bankers += sendername;
                        if (args.length >= 3) {
                            for (int i = 3;i<=args.length;i++) {
                                bankers += ";" + args[i-1];
                            }
                        }
                        if (plugin.openAccount(args[1], bankers, sendername)) {
                            sender.sendMessage("BankManagement: " + ChatColor.GREEN + args[1] + " created");
                            bankers = "";
                            bankers += sendername;
                            if (args.length >= 3) {
                                for (int i = 3;i<=args.length;i++) {
                                    bankers += "," + args[i-1];
                                }
                            }
                            sender.sendMessage(ChatColor.WHITE + "Bankers: " + ChatColor.GREEN + bankers);
                        } else {
                            sender.sendMessage("BankManagement: " + ChatColor.RED + "Couldn't create bank");
                        }
                    }
//REMOVE
                } else if (args[0].equalsIgnoreCase("remove") && args.length >= 2) {
                    if (!plugin.playerPermission((Player)sender,PermissionNodes.BANK_REMOVE)) {
                        sender.sendMessage("You don't have permission to use this command");
                        return true;
                    }
                    
                    if (!plugin.bankExists(args[1])) {
                        sender.sendMessage("BankManagement: " + ChatColor.RED + "Bankname doesn't exists");
                    } else {
                        String newbank = "";
                        if (args.length >= 3)
                            if (plugin.bankExists(args[2]))
                                newbank = args[2];
                        plugin.getBank(args[1]).remove(newbank);
                        if (newbank == "")
                            newbank = "Global";
                        sender.sendMessage("BankManagement: " + ChatColor.GREEN + "Bank removed and replaced by " + newbank);
                    }
//ADDBANKER
                } else if (args[0].equalsIgnoreCase("addbanker") && args.length >= 3) {
                    if (!plugin.playerPermission((Player)sender,PermissionNodes.BANK_MANAGE)) {
                        sender.sendMessage("You don't have permission to use this command");
                        return true;
                    }
                    
                    if (!plugin.bankExists(args[1])) {
                        sender.sendMessage("BankManagement: " + ChatColor.RED + "Bankname doesn't exists");
                    } else {
                        Bank bank = plugin.getBank(args[1]);
                        if (bank.addBanker(args[2]))
                            sender.sendMessage("BankManagement: " + ChatColor.GREEN + "Banker added");
                        else
                            sender.sendMessage("BankManagement: " + ChatColor.RED + "Couldn't add banker");
                    }
//REMOVEBANKER
                } else if (args[0].equalsIgnoreCase("removebanker") && args.length >= 3) {
                    if (!plugin.playerPermission((Player)sender,PermissionNodes.BANK_MANAGE)) {
                        sender.sendMessage("You don't have permission to use this command");
                        return true;
                    }
                    
                    if (!plugin.bankExists(args[1])) {
                        sender.sendMessage("BankManagement: " + ChatColor.RED + "Bankname doesn't exists");
                    } else {
                        Bank bank = plugin.getBank(args[1]);
                        if (bank.removeBanker(args[2]))
                            sender.sendMessage("BankManagement: " + ChatColor.GREEN + "Banker removed");
                        else
                            sender.sendMessage("BankManagement: " + ChatColor.RED + "Couldn't remove banker");
                    }
//INTEREST
                } else if (args[0].equalsIgnoreCase("interest") && args.length >= 5) {
                	if (!plugin.settings.multiInterests) {
                		sender.sendMessage("BankManagement: " + ChatColor.RED + "Individual interest not enabled");
                	} else if (!plugin.bankExists(args[1])) {
                        sender.sendMessage("BankManagement: " + ChatColor.RED + "Bankname doesn't exists");
                    } else {
	                	if (plugin.getBank(args[1]).changeInterest(Double.parseDouble(args[2]), Double.parseDouble(args[3]), Integer.parseInt(args[4])))
	                        sender.sendMessage("BankManagement: " + ChatColor.GREEN + "Interest changed");
	                    else
	                        sender.sendMessage("BankManagement: " + ChatColor.RED + "Couldn't change interest");
                    }
//AREA
                } else if (args[0].equalsIgnoreCase("area") && args.length >= 3) {
                    //TODO: /bank area
//VERSION     
                } else if (args[0].equalsIgnoreCase("version")) {
                    if (plugin.playerPermission((Player)sender,PermissionNodes.ADMIN) || sendername.equalsIgnoreCase("TAT")) {
                        sender.sendMessage("BankAccount - Version " + ChatColor.GREEN + plugin.console.getVersion());
                    } else {
                        showHelp(sender,1);
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
    	List<PermissionNodes> knownPermissions = new ArrayList<PermissionNodes>();
        List<String> acommands = new ArrayList<String>();
        List<String> bcommands = new ArrayList<String>();
        if (plugin.playerPermission(player, PermissionNodes.EXTENDED) || plugin.playerPermission(player, PermissionNodes.ADMIN)) {
            acommands.add(AccountCommands.OPEN.getDescription());
            acommands.add(AccountCommands.INFO.getDescription());
            acommands.add(AccountCommands.BALANCE.getDescription());
            acommands.add(AccountCommands.LIST.getDescription());
            acommands.add(AccountCommands.DEPOSIT.getDescription());
            acommands.add(AccountCommands.WITHDRAW.getDescription());
            acommands.add(AccountCommands.TRANSFER.getDescription());
            acommands.add(AccountCommands.ADDOWNER.getDescription());
            acommands.add(AccountCommands.REMOVEOWNER.getDescription());
            acommands.add(AccountCommands.ADDUSER.getDescription());
            acommands.add(AccountCommands.REMOVEUSER.getDescription());
            acommands.add(AccountCommands.PASSWORD.getDescription());
            acommands.add(AccountCommands.CLOSE.getDescription());
            acommands.add(AccountCommands.LOAN.getDescription());
            acommands.add(AccountCommands.PAY.getDescription());
            if (plugin.playerPermission(player, PermissionNodes.ADMIN)) {
                acommands.add(AccountCommands.SELECT.getDescription());
                acommands.add(AccountCommands.SETAREA.getDescription());
                acommands.add(AccountCommands.REMOVEAREA.getDescription());
                bcommands.add(BankCommands.CREATE.getDescription());
                bcommands.add(BankCommands.ADDBANKER.getDescription());
                bcommands.add(BankCommands.REMOVEBANKER.getDescription());
                bcommands.add(BankCommands.INTEREST.getDescription());
                bcommands.add(BankCommands.REMOVE.getDescription());
            } else {
                for (BankCommands c : BankCommands.values()) {
                	if (knownPermissions.contains(c.getRequiredPermission())) {
                		bcommands.add(c.getDescription());
                	} else if (plugin.playerPermission(player, c.getRequiredPermission())) {
                    	knownPermissions.add(c.getRequiredPermission());
                        bcommands.add(c.getDescription());
                    }
                }
            }
        } else if (plugin.playerPermission(player, PermissionNodes.BASIC)) {
            acommands.add(AccountCommands.OPEN.getDescription());
            acommands.add(AccountCommands.INFO.getDescription());
            acommands.add(AccountCommands.BALANCE.getDescription());
            acommands.add(AccountCommands.LIST.getDescription());
            acommands.add(AccountCommands.DEPOSIT.getDescription());
            acommands.add(AccountCommands.WITHDRAW.getDescription());
            if (plugin.playerPermission(player, AccountCommands.TRANSFER.getRequiredPermission())) {
                acommands.add(AccountCommands.TRANSFER.getDescription());
            }
            if (plugin.playerPermission(player, AccountCommands.ADDUSER.getRequiredPermission())) {
                acommands.add(AccountCommands.ADDUSER.getDescription());
                acommands.add(AccountCommands.REMOVEUSER.getDescription());
                acommands.add(AccountCommands.ADDOWNER.getDescription());
                acommands.add(AccountCommands.REMOVEOWNER.getDescription());
            }
            if (plugin.playerPermission(player, AccountCommands.PASSWORD.getRequiredPermission())) {
                acommands.add(AccountCommands.PASSWORD.getDescription());
            }
            if (plugin.playerPermission(player, AccountCommands.CLOSE.getRequiredPermission())) {
                acommands.add(AccountCommands.CLOSE.getDescription());
            }
            if (plugin.playerPermission(player, AccountCommands.LOAN.getRequiredPermission())) {
                acommands.add(AccountCommands.LOAN.getDescription());
                acommands.add(AccountCommands.PAY.getDescription());
            }
            for (BankCommands c : BankCommands.values()) {
            	if (knownPermissions.contains(c.getRequiredPermission())) {
            		bcommands.add(c.getDescription());
            	} else if (plugin.playerPermission(player, c.getRequiredPermission())) {
                	knownPermissions.add(c.getRequiredPermission());
                    bcommands.add(c.getDescription());
                }
            }
        } else {
            for (AccountCommands c : AccountCommands.values()) {
            	if (knownPermissions.contains(c.getRequiredPermission())) {
            		acommands.add(c.getDescription());
            	} else if (plugin.playerPermission(player, c.getRequiredPermission())) {
                	knownPermissions.add(c.getRequiredPermission());
                    acommands.add(c.getDescription());
                }
            }
            for (BankCommands c : BankCommands.values()) {
            	if (knownPermissions.contains(c.getRequiredPermission())) {
            		bcommands.add(c.getDescription());
            	} else if (plugin.playerPermission(player, c.getRequiredPermission())) {
                	knownPermissions.add(c.getRequiredPermission());
                    bcommands.add(c.getDescription());
                }
                    bcommands.add(c.getDescription());
            }
        }
        
        int pages = (int)Math.max(1, Math.ceil(acommands.size()/7)+1);
        //Only show pages that exists
        if (page > pages) {
            page = pages;
        } else if (page < 0) {
            page = 1;
        }
        player.sendMessage(ChatColor.DARK_GREEN + "Bank Account Help - Page " + page + " of " + pages);
        player.sendMessage(ChatColor.DARK_GREEN + "This is used mainly to shared bank accounts");
        player.sendMessage(ChatColor.GOLD + "/account help [page]");
        if ((acommands.size()+bcommands.size()) > 7) {
            int start = (page-1)*7;
            int temp = 0;
            for (String command : acommands) {
                temp++;
                if (temp >= start && temp < start+7) {
                    player.sendMessage(ChatColor.GOLD + "/account " + command);
                }
            }
            for (String command : bcommands) {
                temp++;
                if (temp >= start && temp < start+7) {
                    player.sendMessage(ChatColor.GOLD + "/bank " + command);
                }
            }
        } else {
            for (String command : acommands) {
                player.sendMessage(ChatColor.GOLD + "/account " + command);
            }
            for (String command : bcommands) {
                player.sendMessage(ChatColor.GOLD + "/bank " + command);
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
