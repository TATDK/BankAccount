package dk.earthgame.TAT.BankAccount.Features;

import java.util.List;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import dk.earthgame.TAT.BankAccount.BankAccount;
import dk.earthgame.TAT.BankAccount.Enum.ATMModes;
import dk.earthgame.TAT.BankAccount.Enum.ATMTypes;
import dk.earthgame.TAT.SignUpdater.UpdaterPriority;

public class ATMMachine {
    private BankAccount plugin;
    private Sign sign;
    private String[] defaultInterface = {"Deposit","Withdraw","Transfer"};

    //Display
    private String[] currentInterface = defaultInterface;
    private int currentLine;

    //ATM modes
    private ATMModes mode = ATMModes.IDLE;
    private boolean deposit;
    private boolean withdraw;
    private boolean transfer;

    //Memory
    private String accountname;
    private String recieveraccount;
    private double amount;
    private Player user = null;

    public ATMMachine(BankAccount instantiate,Sign sign) {
        plugin = instantiate;
        this.sign = sign;
        if (plugin.enabled)
            Print();
    }

    /**
     * Reset ATM
     */
    public void reset() {
        deposit = false;
        withdraw = false;
        transfer = false;
        accountname = "";
        recieveraccount = "";
        amount = 0;
        if (user != null)
        	plugin.UserSaves.getSaved(user.getName()).usingATM = null;
        user = null;
        currentInterface = defaultInterface;
        currentLine = 0;
        mode = ATMModes.IDLE;
        Print();
    }

    /**
     * Check if the player have permission to use this ATM
     * @param player The player
     * @return true if the player have permission; else false
     */
    public boolean checkPlayer(Player player) {
        if (user != null)
            return player.getName().equalsIgnoreCase(user.getName());
        else
            return true;
    }

    /**
     * Scroll one down on interface
     * @param player The player
     */
    public void Scroll(Player player) {
        if (!checkPlayer(player)) {
            currentInterface = defaultInterface;
            currentLine = 0;
            user = null;
            Print();
            return;
        }

        if (currentLine == (currentInterface.length-1))
            currentLine = 0;
        else
            currentLine++;
        Print();
    }

    /**
     * Output interface to sign
     */
    private void Print() {
        String[] lines = {"[ATM]", "  ", "  ", "  "};
        switch (mode) {
            case WAITING_ACCOUNT:
                lines[2] = "Enter reciever";
                lines[3] = "account in chat";
                break;
            case WAITING_AMOUNT:
                lines[2] = "Enter amount";
                lines[3] = "in chat";
                break;
            case WAITING_PASSWORD:
                lines[2] = "Enter password";
                lines[3] = "in chat";
                break;
            case FINISH_COMPLETE:
                lines[2] = "Transaction";
                lines[3] = "complete";
                break;
            case FINISH_FAILED:
                lines[2] = "Transaction";
                lines[3] = "failed";
                break;
            default:
                if (currentLine == 0) {
                    //First line
                    lines[1] = "> " + currentInterface[0];
                    if (currentInterface.length > 1)
                        lines[2] += currentInterface[1];
                    if (currentInterface.length > 2)
                        lines[3] += currentInterface[2];
                } else if (currentLine == (currentInterface.length-1) && currentInterface.length > 2) {
                    //Last line
                    lines[1] += currentInterface[currentInterface.length-3];
                    lines[2] += currentInterface[currentInterface.length-2];
                    lines[3] = "> " + currentInterface[currentInterface.length-1];
                } else {
                    //Middle line
                    lines[1] += currentInterface[currentLine-1];
                    lines[2] = "> " + currentInterface[currentLine];
                    if ((currentInterface.length-1) > currentLine)
                        lines[3] += currentInterface[currentLine+1];
                }
                for (int i=1;i<4;i++) {
                    lines[i] += "               ";
                    lines[i] = lines[i].substring(0, 15);
                }
                break;
        }
        plugin.signupdater.AddSignUpdate(UpdaterPriority.NORMAL, sign, lines);
    }

    /**
     * Receiver for chat input
     * @param msg Chat message
     */
    public void GetChatMsg(String msg) {
        switch (mode) {
            case WAITING_ACCOUNT:
                recieveraccount = msg;
                mode = ATMModes.WAITING_AMOUNT;
                break;
            case WAITING_AMOUNT:
                if (deposit) {
                    Finish(plugin.getAccount(accountname).ATM(user.getName(), ATMTypes.DEPOSIT, Double.parseDouble(msg), ""));
                } else if (withdraw || transfer) {
                    amount = Double.parseDouble(msg);
                    if (plugin.getAccount(accountname).havePassword())
                        mode = ATMModes.WAITING_PASSWORD;
                    else {
                        if (withdraw)
                            Finish(plugin.getAccount(accountname).ATM(user.getName(), ATMTypes.WITHDRAW, amount, ""));
                        else
                            Finish(plugin.getAccount(accountname).ATM(user.getName(), ATMTypes.WITHDRAW, amount, ""));
                    }
                }
                break;
            case WAITING_PASSWORD:
                if (withdraw)
                    Finish(plugin.getAccount(accountname).ATM(user.getName(), ATMTypes.WITHDRAW, amount, msg));
                else if (transfer)
                    Finish(plugin.getAccount(accountname).ATM(recieveraccount, ATMTypes.DEPOSIT, amount, msg));
                return;
        }
        Print();
    }

    /**
     * Run finish commands and reset
     * @param result If the transaction is complete
     */
    private void Finish(boolean result) {
        //Reset
        deposit = false;
        withdraw = false;
        transfer = false;
        accountname = "";
        recieveraccount = "";
        amount = 0;
        plugin.UserSaves.getSaved(user.getName()).usingATM = null;
        user = null;
        //Show result
        if (result)
            mode = ATMModes.FINISH_COMPLETE;
        else
            mode = ATMModes.FINISH_FAILED;
        Print();
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                currentInterface = defaultInterface;
                currentLine = 0;
                mode = ATMModes.IDLE;
                Print();
            }
        },3*20);
    }

    /**
     * Select current menu item
     */
    public void Select(Player player) {
        if (!checkPlayer(player)) {
            currentInterface = defaultInterface;
            currentLine = 0;
            user = null;
            Print();
            return;
        }

        switch (mode) {
            case IDLE:
                user = player;
                if (currentInterface[currentLine].equalsIgnoreCase("deposit")) {
                    mode = ATMModes.DEPOSIT;
                    List<String> accounts = plugin.accountList(player);
                    currentInterface = accounts.toArray(new String[accounts.size()]);
                } else if (currentInterface[currentLine].equalsIgnoreCase("withdraw")) {
                    mode = ATMModes.WITHDRAW;
                    List<String> accounts = plugin.accountList(player);
                    currentInterface = accounts.toArray(new String[accounts.size()]);
                } else if (currentInterface[currentLine].equalsIgnoreCase("transfer")) {
                    mode = ATMModes.TRANSFER;
                    List<String> accounts = plugin.accountList(player);
                    currentInterface = accounts.toArray(new String[accounts.size()]);
                }
                currentLine = 0;
                Print();
                break;
            case DEPOSIT:
                accountname = currentInterface[currentLine];
                deposit = true;
                mode = ATMModes.WAITING_AMOUNT;
                plugin.UserSaves.getSaved(player.getName()).usingATM = this;
                Print();
                break;
            case WITHDRAW:
                accountname = currentInterface[currentLine];
                withdraw = true;
                mode = ATMModes.WAITING_AMOUNT;
                plugin.UserSaves.getSaved(player.getName()).usingATM = this;
                Print();
                break;
            case TRANSFER:
                transfer = true;
                mode = ATMModes.WAITING_ACCOUNT;
                plugin.UserSaves.getSaved(player.getName()).usingATM = this;
                Print();
                break;
        }
    }
}