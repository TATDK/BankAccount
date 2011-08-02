package dk.earthgame.TAT.BankAccount.Enum;

/**
 * BankAccount /account commands
 * @author TAT
 * @since 0.5
 */
public enum AccountCommands {
    OPEN        ("open"       ,"open <accountname> [players]"                            ,true ,PermissionNodes.OPEN),
    INFO        ("info"       ,"info <accountname>"                                      ,true ,PermissionNodes.OPEN),
    LIST        ("list"       ,"list"                                                    ,true ,PermissionNodes.LIST),
    BALANCE     ("balance"    ,"balance <accountname>"                                   ,true ,PermissionNodes.OPEN),
    ADDOWNER    ("addowner"   ,"addowner <accountname> <player>"                         ,true ,PermissionNodes.USER),
    REMOVEOWNER ("removeowner","removeowner <accountname> <player>"                      ,true ,PermissionNodes.USER),
    DEPOSIT     ("deposit"    ,"deposit <accountname> <amount>"                          ,true ,PermissionNodes.DEPOSIT),
    WITHDRAW    ("withdraw"   ,"withdraw <accountname> <amount> [password]"              ,true ,PermissionNodes.WITHDRAW),
    TRANSFER    ("transfer"   ,"transfer <from account> <to account> <amount> [password]",true ,PermissionNodes.TRANSFER),
    CLOSE       ("close"      ,"close <accountname> [password]"                          ,true ,PermissionNodes.CLOSE),
    ADDUSER     ("adduser"    ,"adduser <accountname> <player>"                          ,true ,PermissionNodes.USER),
    REMOVEUSER  ("removeuser" ,"removeuser <accountname> <player>"                       ,true ,PermissionNodes.USER),
    PASSWORD    ("password"   ,"password <accountname> [password]"                       ,true ,PermissionNodes.PASSWORD),
    LOAN        ("loan"       ,"loan [amount]"                                           ,true ,PermissionNodes.LOAN),
    PAY         ("pay"        ,"pay <amount>"                                            ,true ,PermissionNodes.LOAN),
    SELECT      ("select"     ,"select"                                                  ,false,PermissionNodes.ADMIN),
    SETAREA     ("setarea"    ,"setarea <areaname> [bankgroup]"                          ,false,PermissionNodes.ADMIN),
    REMOVEAREA  ("removearea" ,"removearea <areaname> [bankgroup]"                       ,false,PermissionNodes.ADMIN),
    HELP        ("help"       , "help"                                                   ,false,PermissionNodes.ACCESS);

    private String command;
    private String description;
    private boolean requireArea;
    private PermissionNodes requiredPermission;
    AccountCommands(String command, String description,boolean requireArea,PermissionNodes requiredPermission) {
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