package dk.earthgame.TAT.BankAccount.Enum;

/**
 * BankAccount /account commands
 * @author TAT
 * @since 0.5
 */
public enum BankCommands {
    CREATE      ("create"      ,"create <bankname> [bankers]"                                                 ,false,PermissionNodes.BANK_CREATE),
    REMOVE      ("remove"      ,"remove <bankname>"                                                           ,false,PermissionNodes.BANK_REMOVE),
    REMOVETO    ("remove"      ,"remove <bankname> [new bankname]"                                            ,false,PermissionNodes.BANK_REMOVE),
    ADDBANKER   ("addbanker"   ,"addbanker <bankname> <player>"                                               ,false,PermissionNodes.BANK_MANAGE),
    REMOVEBANKER("removebanker","removebanker <bankname> <player>"                                            ,false,PermissionNodes.BANK_MANAGE),
    INTEREST    ("interest"    ,"interest <bankname> <online interest> <offline interest> <required % online>",false,PermissionNodes.BANK_MANAGE);

    private String command;
    private String description;
    private boolean requireArea;
    private PermissionNodes requiredPermission;
    BankCommands(String command,String description,boolean requireArea,PermissionNodes requiredPermission) {
        this.command = command;
        this.description = description;
        this.requireArea = requireArea;
        this.requiredPermission = requiredPermission;
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
    /**
     * Get the required permission
     * 
     * @since 0.6
     * @return PermissionNodes
     */
    public PermissionNodes getRequiredPermission() { return requiredPermission; }
};