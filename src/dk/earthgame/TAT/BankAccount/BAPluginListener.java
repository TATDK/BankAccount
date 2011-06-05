package dk.earthgame.TAT.BankAccount;

import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;

import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijikokun.register.payment.Methods;

public class BAPluginListener extends ServerListener {
	private BankAccount plugin;
	Methods Methods;

	public BAPluginListener(BankAccount plugin) {
	    this.plugin = plugin;
	    this.Methods = new Methods();
	}
	
	Plugin checkPlugin(String pluginname) {
		return plugin.getServer().getPluginManager().getPlugin(pluginname);
	}

	@Override
	public void onPluginDisable(PluginDisableEvent event) {
		String plugin = event.getPlugin().getDescription().getName();
		//Economy
		if (this.Methods != null && this.Methods.hasMethod()) {
			boolean check = this.Methods.checkDisabled(event.getPlugin());

			if(check) {
				this.plugin.consoleInfo("Payment method was disabled.");
				this.plugin.consoleWarning("Stopping BankAccount - Reason: Missing economy plugin!");
				this.plugin.getServer().getPluginManager().disablePlugin(event.getPlugin());
			}
		}
		//Permissions
		if (this.plugin.settings.Permissions != null && plugin.equalsIgnoreCase("Permissions")) {
			this.plugin.settings.Permissions = null;
			this.plugin.consoleWarning("Lost connection with " + plugin + "!");
		}
		if (this.plugin.settings.GroupManager != null && plugin.equalsIgnoreCase("GroupManager")) {
			this.plugin.settings.GroupManager = null;
			this.plugin.consoleWarning("Lost connection with " + plugin + "!");
		}
	}

	@Override
	public void onPluginEnable(PluginEnableEvent event) {
		String plugin = event.getPlugin().getDescription().getName();
		//Economy
		if (!this.Methods.hasMethod()) {
			if(this.Methods.setMethod(event.getPlugin())) {
				this.plugin.Method = this.Methods.getMethod();
				this.plugin.consoleInfo("Payment method found (" + this.plugin.Method.getName() + " version: " + this.plugin.Method.getVersion() + ")");
				this.plugin.foundEconomy();
			}
		}
		
		//Permissions
		if (this.plugin.settings.Permissions == null && plugin.equalsIgnoreCase("Permissions") && this.plugin.settings.UsePermissions) {
			Plugin test = checkPlugin("Permissions");
			if (test != null) {
				((Permissions)test).getDatabase();
				this.plugin.settings.Permissions = ((Permissions)test).getHandler();
				this.plugin.consoleInfo("Established connection with " + plugin + "!");
			}
		}
		if (this.plugin.settings.GroupManager == null && plugin.equalsIgnoreCase("GroupManager") && this.plugin.settings.UseGroupManager) {
			Plugin test = checkPlugin("GroupManager");
			if (test != null) {
				this.plugin.settings.GroupManager = (GroupManager)test;
				this.plugin.consoleInfo("Established connection with " + plugin + "!");
				if (this.plugin.settings.checkJobId > 0) {
					this.plugin.getServer().getScheduler().cancelTask(this.plugin.settings.checkJobId);
				}
			}
		}
	}
}