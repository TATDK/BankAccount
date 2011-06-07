package dk.earthgame.TAT.BankAccount.System;

import java.util.logging.Logger;

import org.bukkit.plugin.PluginDescriptionFile;

public class Console {
	protected final Logger log = Logger.getLogger("Minecraft");
	private PluginDescriptionFile pdfFile;
	
	public Console(PluginDescriptionFile description) {
		pdfFile = description;
	}
	
	/**
	 * Output Info to log on behalf of BankAccount
	 * 
	 * @param message
	 * @since 0.5
	 */
	public void info(String message) {
		log.info("[" + pdfFile.getName() + "] " + message);
	}
	
	/**
	 * Output Info to log on behalf of BankAccount
	 * @param integer
	 * @since 0.5.1
	 */
	public void info(int integer) { info(""+integer); }

	/**
	 * Output Warning to log on behalf of BankAccount
	 * 
	 * @param message
	 * @since 0.5
	 */
	public void warning(String message) {
		log.warning("[" + pdfFile.getName() + "] " + message);
	}
	
	/**
	 * Output Warning to log on behalf of BankAccount
	 * @param integer
	 * @since 0.5.1
	 */
	public void warning(int integer) { warning(""+integer); }
	
	/**
	 * Output specific message as info on behalf of BankAccount
	 * Does not include [BankAccount]
	 * @param message
	 * @since 0.5.1
	 */
	public void message(String message) {
		log.info(message);
	}
	
	/**
	 * Output BankAccount enabled message
	 * @since 0.5.1
	 */
	public void enabled() {
		message(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
	}
	
	/**
	 * Output BankAccount disabled message
	 * @since 0.5.1
	 */
	public void disabled() {
		message(pdfFile.getName() + " is disabled!");
	}
}
