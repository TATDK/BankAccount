package dk.earthgame.TAT.BankAccount.System;

/**
 * Transaction types used by BankAccount
 * 
 * @author TAT
 * @since 0.5
 */
public enum TransactionTypes {
	OPEN				(1), //Account created
	DEPOSIT				(2), //Deposit to account
	WITHDRAW			(3), //Withdraw from account
	TRANSFER_DEPOSIT	(4), //Transfer from this account
	TRANSFER_WITHDRAW	(5), //Transfer to this account
	CLOSE				(6), //Account closed
	USER_ADD			(7), //User added
	USER_REMOVE			(8), //User removed
	PASSWORD			(9), //Password set
	LOAN_START			(10),//Loan created
	LOAN_PAYMENT		(11),//Loan payment
	LOAN_PAID			(12),//Loan finished and paid
	LOAN_MISSING		(13),//Loan could not finish, missing money
	BOUNTY_START		(14),//Bounty on user started
	BOUNTY_END			(15);//Bounty on user ended
	
	private int code;
	TransactionTypes(int code) {
		this.code = code;
	}
	/**
	 * Get the transaction type number used in the database
	 * @return Transaction type number
	 */
	public int get() { return code; }
};