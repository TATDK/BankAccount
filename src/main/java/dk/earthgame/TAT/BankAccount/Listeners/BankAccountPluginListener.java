package dk.earthgame.TAT.BankAccount.Listeners;

import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;

import com.nijikokun.bukkit.Permissions.Permissions;

import dk.earthgame.TAT.BankAccount.BankAccount;
import dk.earthgame.TAT.SignUpdater.SignUpdater;
import dk.earthgame.nijikokun.register.payment.Methods;

/**
 * BankAccount hook with other plugins
 * @author TAT
 */
public class BankAccountPluginListener extends ServerListener {
    private BankAccount plugin;
    public Methods Methods;

    public BankAccountPluginListener(BankAccount plugin) {
        this.plugin = plugin;
        Methods.setPreferred("iConomy");
    }

    Plugin checkPlugin(String pluginname) {
        return plugin.getServer().getPluginManager().getPlugin(pluginname);
    }

    @Override
    public void onPluginDisable(PluginDisableEvent event) {
        String pluginname = event.getPlugin().getDescription().getName();
        //Register (Economy API)
        if (Methods != null && Methods.hasMethod()) {
            boolean check = Methods.checkDisabled(event.getPlugin());

            if(check) {
                plugin.console.info("Payment method was disabled.");
                plugin.console.warning("Stopping BankAccount - Reason: Missing economy plugin!");
                plugin.getServer().getPluginManager().disablePlugin(event.getPlugin());
            }
        }
        //Permissions
        if (plugin.settings.Permissions != null && pluginname.equalsIgnoreCase("Permissions")) {
            plugin.settings.Permissions = null;
            plugin.console.warning("Lost connection with " + plugin + "!");
        }
        //GroupManager
        if (plugin.settings.GroupManager != null && pluginname.equalsIgnoreCase("GroupManager")) {
            plugin.settings.GroupManager = null;
            plugin.console.warning("Lost connection with " + plugin + "!");
        }
        //SignUpdater
        if (plugin.signupdater != null && pluginname.equalsIgnoreCase("SignUpdater")) {
            plugin.signupdater = null;
            plugin.ATMSign.enabled = false;
            plugin.BalanceSign.enabled = false;
            plugin.console.warning("Lost connection with " + plugin + "!");
        }
    }

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        String pluginname = event.getPlugin().getDescription().getName();
        //Register (Economy API)
        if (!Methods.hasMethod() && Methods.setMethod(plugin.getServer().getPluginManager())) {
            plugin.Method = Methods.getMethod();
            plugin.console.info("Payment method found (" + plugin.Method.getName() + " version: " + plugin.Method.getVersion() + ")");
            plugin.LoanSystem.startupRunner();
            plugin.Interest.startupInterest();
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    if (plugin.BalanceSign.enabled) {
                        plugin.BalanceSign.update();
                        plugin.console.info("Signs updated");
                    }
                }
            },20);
        }

        //Permissions
        if (plugin.settings.Permissions == null && pluginname.equalsIgnoreCase("Permissions") && plugin.settings.usePermissions) {
            Plugin test = checkPlugin(pluginname);
            if (test != null) {
                ((Permissions)test).getDatabase();
                plugin.settings.Permissions = ((Permissions)test).getHandler();
                plugin.console.info("Established connection with " + plugin + "!");
            }
        }
        //GroupManager
        if (plugin.settings.GroupManager == null && pluginname.equalsIgnoreCase("GroupManager") && plugin.settings.useGroupManager) {
            Plugin test = checkPlugin(pluginname);
            if (test != null) {
                plugin.settings.GroupManager = (GroupManager)test;
                plugin.console.info("Established connection with " + plugin + "!");
                if (plugin.settings.checkJobId > 0) {
                    plugin.getServer().getScheduler().cancelTask(this.plugin.settings.checkJobId);
                }
            }
        }

        //SignUpdater
        if (plugin.signupdater == null && pluginname.equalsIgnoreCase("SignUpdater")) {
            Plugin test = checkPlugin(pluginname);
            if (test != null) {
                plugin.signupdater = (SignUpdater)test;
                plugin.ATMSign.enabled = true;
                plugin.BalanceSign.enabled = true;
                plugin.console.info("Established connection with " + plugin + "!");
            }
        }
    }
}