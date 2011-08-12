package dk.earthgame.TAT.BankAccount.Enum;

public enum ATMModes {
	IDLE,              //Default
	DEPOSIT,           //Deposit
	WITHDRAW,          //Withdraw
	TRANSFER,          //Transfer
	WAITING_ACCOUNT,   //Waiting for player input on accountname
	WAITING_AMOUNT,    //Waiting for player input on amount
	WAITING_PASSWORD,  //Waiting for player input on password
	FINISH_COMPLETE,   //Showing complete (Going to idle in 3 sec)
	FINISH_FAILED      //Showing failed (Going to idle in 3 sec)
}