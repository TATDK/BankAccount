package dk.earthgame.TAT.BankAccount.System;

/**
 * BankAccount commands
 * @author TAT
 * @since 0.5
 */
public enum CommandList {
	OPEN				("open","open <accountname> [players]",true),
	INFO				("info","info <accountname>",true),
	LIST				("list","list",true),
	BALANCE				("balance","balance <accountname>",true),
	ADDOWNER			("addowner","addowner <accountname> <player>",true),
	REMOVEOWNER			("removeowner","removeowner <accountname> <player>",true),
	DEPOSIT				("deposit","deposit <accountname> <amount>",true),
	WITHDRAW			("withdraw","withdraw <accountname> <amount> [password]",true),
	TRANSFER			("transfer","transfer <from account> <to account> <amount> [password]",true),
	CLOSE				("close","close <accountname> [password]",true),
	ADDUSER				("adduser","adduser <accountname> <player>",true),
	REMOVEUSER			("removeuser","removeuser <accountname> <player>",true),
	PASSWORD			("password","password <accountname> [password]",true),
	LOAN				("loan","loan [amount]",true),
	PAY					("pay","pay <amount>",true),
	SELECT				("select","select",false),
	SETAREA				("setarea","setarea <areaname> [bankgroup]",false),
	REMOVEAREA			("removearea","removearea <areaname> [bankgroup]",false),
	HELP				("help", "help", false);

	private String command;
	private String description;
	private boolean requireArea;
	CommandList(String command, String description,boolean requireArea) {
		this.command = command;
		this.description = description;
		this.requireArea = requireArea;
	}
	/**
	 * Get the command in simplest form
	 * 
	 * @since 0.5
	 * @return Command (example: loan)
	 */
	public String getCommand() { return command; }
	/**
	 * Get the description of command
	 * 
	 * @since 0.5
	 * @return Description (example: loan [amount])
	 */
	public String getDescription() { return description; }
	/**
	 * Check if the command needs to be sent inside area.
	 * Only if areas are enabled
	 * 
	 * @since 0.5
	 * @return boolean
	 */
	public boolean getRequireArea() { return requireArea; }
};