package dk.earthgame.TAT.BankAccount.System;

public enum TransactionTypes {
	OPEN				(1),
	DEPOSIT				(2),
	WITHDRAW			(3),
	TRANSFER_DEPOSIT	(4),
	TRANSFER_WITHDRAW	(5),
	CLOSE				(6),
	USER_ADD			(7),
	USER_REMOVE			(8),
	PASSWORD			(9),
	LOAN_START			(10),
	LOAN_PAYMENT		(11),
	LOAN_PAID			(12),
	LOAN_MISSING		(13),
	BOUNTY_START		(14),
	BOUNTY_END			(15);
	
	private final int code;
	TransactionTypes(int code) {
		this.code = code;
	}
	public int get() { return code; }
};