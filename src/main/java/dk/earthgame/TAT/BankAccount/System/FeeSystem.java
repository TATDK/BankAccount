package dk.earthgame.TAT.BankAccount.System;

import dk.earthgame.TAT.BankAccount.Enum.FeeModes;

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
     * This is used when calculating on whole balance
     * 
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
     * Check if the player have enough money to pay the fee
     * This is used when calculating part of balance
     * 
     * @param balance
     * @since 0.5.2
     */
    public boolean CanAfford (double amount, double balance) {
        if (Fee(amount) != -1) {
            if (Fee(amount)+amount <= balance) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Calculate fee for an amount
     * 
     * @param amount
     * @since 0.5.2
     * @return Fee calculated - If -1 is returned, fee couldn't be calculated
     */
    public double Fee(double amount) {
        double fee = -1;
        if (Fee_Mode != FeeModes.NONE) {
            switch (Fee_Mode) {
                case PERCENTAGE:
                    fee = amount*(Fee_Percentage/100);
                    break;
                case STATIC:
                    if (amount >= Fee_Static) {
                        fee = Fee_Static;
                    }
                    break;
                case SMART1:
                    fee = amount*(Fee_Percentage/100);
                    amount -= amount*(Fee_Percentage/100);
                    if (amount >= Fee_Static) {
                        fee += Fee_Static;
                    }
                    break;
                case SMART2:
                    if (amount >= Fee_Static) {
                        amount -= Fee_Static;
                        fee = Fee_Static;
                    }
                    fee += amount*(Fee_Percentage/100);
                    break;
            }
        }
        return fee;
    }
    
    /**
     * Get setting FeeMode
     * @since 0.5.2
     * @return How to calculate the fee
     */
    public FeeModes getMode() { return Fee_Mode; }
    /**
     * Get setting percentage
     * @since 0.5.2
     * @return % of money
     */
    public double getPercentage() { return Fee_Percentage; }
    /**
     * Get setting static
     * @since 0.5.2
     * @return Static amount of money
     */
    public double getStatic() { return Fee_Static; }
}