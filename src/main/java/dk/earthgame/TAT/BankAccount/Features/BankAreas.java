package dk.earthgame.TAT.BankAccount.Features;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import dk.earthgame.TAT.BankAccount.System.BankAccountException;

public class BankAreas {
	private dk.earthgame.TAT.BankAccount.BankAccount plugin;
	
	public BankAreas(dk.earthgame.TAT.BankAccount.BankAccount instantiate) {
		plugin = instantiate;
	}

	/**
     * Does an area exists
     * 
     * @param name Name of area
     * @since 0.5
     * @return If the area exists
     * @throws BankAccountException 
     */
    public boolean areaExists(String name) throws BankAccountException {
        ResultSet rs;
        int id = 0;
        try {
            if (plugin.settings.UseMySQL) {
                rs = plugin.settings.stmt.executeQuery("SELECT `id` FROM `" + plugin.settings.SQL_area_table + "` WHERE `areaname` = '" + name + "'");
            } else {
                rs = plugin.settings.stmt.executeQuery("SELECT `rowid` FROM `" + plugin.settings.SQL_area_table + "` WHERE `areaname` = '" + name + "'");
            }
            try {
                while (rs.next()) {
                    if (plugin.settings.UseMySQL) {
                        id = rs.getInt("id");
                    } else {
                        id = rs.getInt("rowid");
                    }
                }
            } catch (SQLException e1) {
                if (!e1.getMessage().equalsIgnoreCase(null))
                	plugin.console.warning("Error #14-5: " + e1.getMessage());
                else
                	plugin.console.warning("Error #14-4: " + e1.getErrorCode() + " - " + e1.getSQLState());
            }
        } catch (SQLException e) {
            if (!e.getMessage().equalsIgnoreCase(null))
            	plugin.console.warning("Error #14-3: " + e.getMessage());
            else
            	plugin.console.warning("Error #14-2: " + e.getErrorCode() + " - " + e.getSQLState());
            plugin.throwException("SQL error checking area for existence");
        } catch (Exception e) {
        	plugin.console.warning("Error #14-1: " + e.toString());
        	plugin.throwException("Intern error checking area for existence");
        }
        if (id > 0) {
            return true;
        }
        return false;
    }
    
    /**
     * Are the position inside an area
     * 
     * @param world Name of world
     * @param pos Position
     * @since 0.5
     * @return If the position is inside an area
     * @throws BankAccountException 
     */
    public boolean inArea(String world,Location pos) throws BankAccountException {
        try {
            ResultSet rs = plugin.settings.stmt.executeQuery("SELECT `x1`,`y1`,`z1`,`x2`,`y2`,`z2` FROM `" + plugin.settings.SQL_area_table + "` WHERE `world` = '" + world + "'");
            while (rs.next()) {
                Vector min = new Vector(
                    Math.min(rs.getInt("x1"), rs.getInt("x2")),
                    Math.min(rs.getInt("y1"), rs.getInt("y2")),
                    Math.min(rs.getInt("z1"), rs.getInt("z2"))
                );
                Vector max = new Vector(
                    Math.max(rs.getInt("x1"), rs.getInt("x2")),
                    Math.max(rs.getInt("y1"), rs.getInt("y2")),
                    Math.max(rs.getInt("z1"), rs.getInt("z2"))
                );
                
                if (pos.getBlockX() >= min.getBlockX() && pos.getBlockX() <= max.getBlockX() &&
                    pos.getBlockY() >= min.getBlockY() && pos.getBlockY() <= max.getBlockY() &&
                    pos.getBlockZ() >= min.getBlockZ() && pos.getBlockZ() <= max.getBlockZ()) {
                    return true;
                }
            }
        } catch(SQLException e) {
            if (!e.getMessage().equalsIgnoreCase(null))
            	plugin.console.warning("Error #15-3: " + e.getMessage());
            else
            	plugin.console.warning("Error #15-2: " + e.getErrorCode() + " - " + e.getSQLState());
            plugin.throwException("SQL error locating you in bank area");
        } catch (Exception e) {
        	plugin.console.warning("Error #15-1: " + e.toString());
        	plugin.throwException("Intern error locating you in bank area");
        }
        return false;
    }
    
    /**
     * Add an area
     * 
     * @param name Name of area
     * @param pos1 Position 1
     * @param pos2 Position 2
     * @param world Name of world
     * @since 0.5
     * @return If the area is successfully added
     * @throws BankAccountException 
     */
    public boolean setArea(String name,Location pos1,Location pos2,String world) throws BankAccountException {
        if (areaExists(name)) {
            return false;
        }
        try {
        	plugin.settings.stmt.executeUpdate("INSERT INTO `" + plugin.settings.SQL_area_table + "` (`areaname`,`world`,`x1`, `y1`, `z1`, `x2`, `y2`, `z2`) VALUES ('" + name + "','" + world + "','" + pos1.getBlockX() + "','" + pos1.getBlockY() + "','" + pos1.getBlockZ() + "','" + pos2.getBlockX() + "','" + pos2.getBlockY() + "','" + pos2.getBlockZ() + "')");
            return true;
        } catch (SQLException e) {
            if (!e.getMessage().equalsIgnoreCase(null))
            	plugin.console.warning("Error #12-3: " + e.getMessage());
            else
            	plugin.console.warning("Error #12-2: " + e.getErrorCode() + " - " + e.getSQLState());
            plugin.throwException("SQL error");
        } catch (Exception e) {
        	plugin.console.warning("Error #12-1: " + e.toString());
        	plugin.throwException();
        }
        return false;
    }

    /**
     * Remove an area
     * 
     * @param name Name of area
     * @since 0.5
     * @return If the area is successfully removed
     * @throws BankAccountException 
     */
    public boolean removeArea(String name) throws BankAccountException {
        try {
        	plugin.settings.stmt.executeUpdate("DELETE FROM `" + plugin.settings.SQL_area_table + "` WHERE `areaname` = '" + name + "'");
            return true;
        } catch(SQLException e) {
            if (!e.getMessage().equalsIgnoreCase(null))
            	plugin.console.warning("Error #13-3: " + e.getMessage());
            else
            	plugin.console.warning("Error #13-2: " + e.getErrorCode() + " - " + e.getSQLState());
            plugin.throwException("SQL error");
        } catch (Exception e) {
        	plugin.console.warning("Error #13-1: " + e.toString());
        	plugin.throwException();
        }
        return false;
    }
}
