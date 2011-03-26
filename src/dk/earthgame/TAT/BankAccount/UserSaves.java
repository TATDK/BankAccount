package dk.earthgame.TAT.BankAccount;

import org.bukkit.Location;

public class UserSaves {
	public double bounty;
	public boolean selecting;
	private static Location pos1;
	private static Location pos2;
	
	public void clearPositions() {
		pos1 = null;
		pos2 = null;
	}
	
	public int setPosition(Location pos) {
		if (pos1 == null) {
			pos1 = pos;
			return 1;
		} else if (pos2 == null) {
			pos2 = pos;
			return 2;
		} else {
			pos2 = null;
			pos1 = pos;
			return 1;
		}
	}
	
	public Location getPosition(int position) {
		if (position == 1 && pos1 != null) {
			return pos1;
		} else if (position == 2 && pos2 != null) {
			return pos2;
		} else {
			return null;
		}
	}
}
