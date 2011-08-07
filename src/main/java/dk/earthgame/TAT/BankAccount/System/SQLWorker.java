package dk.earthgame.TAT.BankAccount.System;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import dk.earthgame.TAT.BankAccount.BankAccount;
import dk.earthgame.TAT.BankAccount.Enum.TransactionTypes;

/**
 * Worker for communication between BankAccount and SQL database
 * @author TAT
 * @since 0.6
 */
public class SQLWorker {
    private BankAccount plugin;
    
    public SQLWorker(BankAccount instantiate) {
        plugin = instantiate;
    }
    
    /**
     * Get the first string found in coloum from table
     * 
     * @param coloum Coloum in table
     * @param table Table in database
     * @since 0.6
     * @return First found result
     * @throws SQLException
     */
    public String getString(String coloum, String table) throws SQLException {
        return getString(coloum,table,"");
    }
    
    /**
     * Get the first string found in coloum from table with a special WHERE statement
     * 
     * @param coloum Coloum in table
     * @param table Table in database
     * @param where WHERE statement (don't include WHERE)
     * @since 0.6
     * @return First found result
     * @throws SQLException
     */
    public String getString(String coloum, String table, String where) throws SQLException {
        String query = "SELECT `" + coloum + "` FROM `" + table + "`";
        if (!where.equalsIgnoreCase("")) {
            query +=  "WHERE " + where;
        }
        ResultSet rs;
        rs = plugin.settings.selectStmt.executeQuery(query);
        while(rs.next()) {
            return rs.getString(coloum);
        }
        return null;
    }
    
    /**
     * Get the first int found in coloum from table
     * 
     * @param coloum Coloum in table
     * @param table Table in database
     * @since 0.6
     * @return First found result
     * @throws SQLException
     */
    public int getInt(String coloum, String table) throws SQLException {
        return getInt(coloum,table,"");
    }
    
    /**
     * Get the first int found in coloum from table with a special WHERE statement
     * 
     * @param coloum Coloum in table
     * @param table Table in database
     * @param where WHERE statement (don't include WHERE)
     * @since 0.6
     * @return First found result
     * @throws SQLException
     */
    public int getInt(String coloum, String table, String where) throws SQLException {
        String query = "SELECT `" + coloum + "` FROM `" + table + "`";
        if (!where.equalsIgnoreCase("")) {
            query +=  "WHERE " + where;
        }
        ResultSet rs;
        rs = plugin.settings.selectStmt.executeQuery(query);
        while(rs.next()) {
            return rs.getInt(coloum);
        }
        return 0;
    }
    
    /**
     * Get the first double found in coloum from table
     * 
     * @param coloum Coloum in table
     * @param table Table in database
     * @since 0.6
     * @return First found result
     * @throws SQLException
     */
    public double getDouble(String coloum, String table) throws SQLException {
        return getDouble(coloum,table,"");
    }
    
    /**
     * Get the first double found in coloum from table with a special WHERE statement
     * 
     * @param coloum Coloum in table
     * @param table Table in database
     * @param where WHERE statement (don't include WHERE)
     * @since 0.6
     * @return First found result
     * @throws SQLException
     */
    public double getDouble(String coloum, String table, String where) throws SQLException {
        String query = "SELECT `" + coloum + "` FROM `" + table + "`";
        if (!where.equalsIgnoreCase("")) {
            query +=  "WHERE " + where;
        }
        ResultSet rs;
        rs = plugin.settings.selectStmt.executeQuery(query);
        while(rs.next()) {
            return rs.getDouble(coloum);
        }
        return 0;
    }
    
    /**
     * Execute a SELECT SQL query
     * 
     * @param query Whole query (inclusive SELECT)
     * @return ResultSet
     * @throws SQLException
     */
    public ResultSet executeSelect(String query) throws SQLException {
        return plugin.settings.selectStmt.executeQuery(query);
    }
    
    /**
     * Execute a INSERT SQL query
     * 
     * @param query Whole query (inclusive INSERT)
     * @return 0 on error
     * @throws SQLException
     */
    public int executeInsert(String query) throws SQLException {
        return plugin.settings.updateStmt.executeUpdate(query);
    }
    
    /**
     * Execute a UPDATE SQL query
     * 
     * @param query Whole query (inclusive UPDATE)
     * @return Number of rows effected
     * @throws SQLException
     */
    public int executeUpdate(String query) throws SQLException {
        return plugin.settings.updateStmt.executeUpdate(query);
    }
    
    /**
     * Execute a DELETE SQL query
     * 
     * @param query Whole query (inclusive DELETE)
     * @return Number of rows effected
     * @throws SQLException
     */
    public int executeDelete(String query) throws SQLException {
        return plugin.settings.updateStmt.executeUpdate(query);
    }
    
    /**
     * Add a transaction to the database without account
     * 
     * @param player Username of the player
     * @param type Type of transaction (dk.earthgame.TAT.BankAccount.Enum.TransactionTypes)
     * @param amount amount of money the transaction (0.00 if money isn't a part of the transaction)
     * @since 0.6
     */
    public void addTransactionPlayer(String player, TransactionTypes type, Double amount) {
        addTransaction(player, "", type, amount);
    }
    
    /**
     * Add a transaction to the database without player
     * 
     * @param account Name of account
     * @param type Type of transaction (dk.earthgame.TAT.BankAccount.Enum.TransactionTypes)
     * @param amount amount of money the transaction (0.00 if money isn't a part of the transaction)
     * @since 0.6
     */
    public void addTransactionAccount(String account, TransactionTypes type, Double amount) {
        addTransaction("", account, type, amount);
    }
    
    /**
     * Add a transaction to the database
     * 
     * @param player Username of the player
     * @param account Name of account
     * @param type Type of transaction (dk.earthgame.TAT.BankAccount.Enum.TransactionTypes)
     * @param amount amount of money the transaction (0.00 if money isn't a part of the transaction)
     * @since 0.5
     */
    public void addTransaction(String player, String account, TransactionTypes type, Double amount) {
        if (plugin.settings.transactions) {
            try {
                int time = Math.round(new Date().getTime()/1000);
                plugin.settings.stmt.executeUpdate("INSERT INTO `" + plugin.settings.SQL_transaction_table + "` (`player`,`account`,`type`,`amount`,`time`) VALUES ('" + player + "','" + account + "','" + type.get() + "','" + amount + "','" + time +"')");
            } catch(SQLException e) {
                if (!e.getMessage().equalsIgnoreCase(null))
                    plugin.console.warning("Error #16-2: " + e.getMessage());
                else
                    plugin.console.warning("Error #16-1: " + e.getErrorCode() + " - " + e.getSQLState());
            }
        }
    }
}