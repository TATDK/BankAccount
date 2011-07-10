package dk.earthgame.TAT.BankAccount.System;

/**
 * Special exception on errors with BankAccount
 * 
 * @since 0.6
 * @author TAT
 */
public class BankAccountException extends Exception {
	private static final long serialVersionUID = -8199732558167918085L;

	/**
     * Constructs a new BankAccountException.
     */
	public BankAccountException() {
		super("An intern error happend");
	}
	
	/**
     * Constructs a new BankAccountException.
     * @param e The error message to report.
     */
    public BankAccountException(String e) {
        super(e);
    }
}
