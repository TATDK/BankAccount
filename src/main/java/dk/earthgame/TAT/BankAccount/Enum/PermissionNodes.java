package dk.earthgame.TAT.BankAccount.Enum;

/**
 * BankAccount permissionnodes
 * @author TAT
 * @since 0.5
 */
public enum PermissionNodes {
    ACCESS              ("access"),      //Access to allow user to use BankAccount.
                                         //Overrides all other Bankaccount permissions
    ADMIN               ("admin"),       //Access to all commands and accounts (if SuperAdmin is enabled)
    AREA                ("area"),        //Access to area commands
    OPEN                ("open"),        //Access to open own accounts + show balance
    LIST                ("list"),        //Access to list own accounts
    USER                ("user"),        //Access to add/remove users/owners on own accounts
    CLOSE               ("close"),       //Access to close own accounts
    DEPOSIT             ("deposit"),     //Access to deposit to own accounts
    WITHDRAW            ("withdraw"),    //Access to withdraw from own accounts
    PASSWORD            ("password"),    //Access to set password
    TRANSFER            ("transfer"),    //Access to transfer from own accounts
    LOAN                ("loan"),        //Access to loan money
    EXTENDED            ("extended"),    //Access to all account commands except area commands
    BASIC               ("basic"),       //Access the same as: open,user,deposit,withdraw,list
    BALANCESIGN         ("sign.balance"),//Access to create BankAccount balancesigns
    ATMSIGN             ("sign.atm"),    //Access to create BankAccount ATMsigns
    BANK_CREATE         ("bank.create"), //Access to create new banks
    BANK_REMOVE         ("bank.remove"), //Access to remove any bank
    BANK_MANAGE         ("bank.manage"); //Access to manage all banks (add and remove bankers, control interest)
    
    private String node;
    PermissionNodes(String node) {
        this.node = "bankaccount."+node;
    }
    /**
     * Get the node inside the permissions plugin
     * @since 0.5
     * @return Permissionnode (example: bankaccount.access)
     */
    public String getNode() { return node; }
};