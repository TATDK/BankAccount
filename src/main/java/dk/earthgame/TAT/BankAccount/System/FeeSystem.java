package dk.earthgame.TAT.BankAccount.System;

import dk.earthgame.TAT.BankAccount.Settings.FeeModes;

/**
 * System for different fee situations
 * 
 * @since 0.5.2
 * @author TAT
 */
public class FeeSystem {
	private FeeModes Fee_Mode = FeeModes.NONE;
	private double Fee_Percentage;
	private double Fee_Static;
	
	/**
	 * @param Mode How to calculate the fee
	 * @param Percentage % of money
	 * @param Static Static amount of money
	 * @since 0.5.2
	 */
	public FeeSystem(FeeModes Mode,double Percentage,double Static) {
		Fee_Mode = Mode;
		Fee_Percentage = Percentage;
		Fee_Static = Static;
	}
	
	/**
	 * Check if the player have enough money to pay the fee
	 * @param balance
	 * @since 0.5.2
	 */
	public boolean CanAfford (double balance) {
		if (Fee(balance) != -1) {
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param balance
	 * @since 0.5.2
	 * @return Fee calculated
	 */
	public double Fee(double balance) {
		double fee = -1;
		if (Fee_Mode != FeeModes.NONE) {
			switch (Fee_Mode) {
				case PERCENTAGE:
					fee = balance*(Fee_Percentage/100);
					break;
				case STATIC:
					if (balance >= Fee_Static) {
						fee = Fee_Static;
					}
					break;
				case SMART1:
					fee = balance*(Fee_Percentage/100);
					balance -= balance*(Fee_Percentage/100);
					if (balance >= Fee_Static) {
						fee += Fee_Static;
					}
					break;
				case SMART2:
					if (balance >= Fee_Static) {
						balance -= Fee_Static;
						fee = Fee_Static;
					}
					fee += balance*(Fee_Percentage/100);
					break;
			}
		}
		return fee;
	}
	
	/**
	 * @since 0.5.2
	 * @return How to calculate the fee
	 */
	public FeeModes getMode() { return Fee_Mode; }
	/**
	 * @since 0.5.2
	 * @return % of money
	 */
	public double getPercentage() { return Fee_Percentage; }
	/**
	 * @since 0.5.2
	 * @return Static amount of money
	 */
	public double getStatic() { return Fee_Static; }
}