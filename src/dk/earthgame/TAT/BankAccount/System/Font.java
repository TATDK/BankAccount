package dk.earthgame.TAT.BankAccount.System;

import java.util.HashMap;

/**
 * Class for font functions
 * @author TAT
 * @since 0.5.1
 */
public class Font {
	static HashMap<String, Integer> fontWidth = new HashMap<String, Integer>();
	public Font() {
		/*
		 * Widths is in pixels
		 * Got them from fontWidths.txt uploaded to the Bukkit forum by Edward Hand
		 * http://forums.bukkit.org/threads/formatting-plugin-output-text-into-columns.8481/
		 */
		fontWidth.clear();
		fontWidth.put(" ",4);
		fontWidth.put("!",2);
		fontWidth.put("\"",5);
		fontWidth.put("#",6);
		fontWidth.put("$",6);
		fontWidth.put("%",6);
		fontWidth.put("&",6);
		fontWidth.put("'",3);
		fontWidth.put("(",5);
		fontWidth.put(")",5);
		fontWidth.put("*",5);
		fontWidth.put("+",6);
		fontWidth.put(",",2);
		fontWidth.put("-",6);
		fontWidth.put(".",2);
		fontWidth.put("/",6);
		fontWidth.put("0",6);
		fontWidth.put("1",6);
		fontWidth.put("2",6);
		fontWidth.put("3",6);
		fontWidth.put("4",6);
		fontWidth.put("5",6);
		fontWidth.put("6",6);
		fontWidth.put("7",6);
		fontWidth.put("8",6);
		fontWidth.put("9",6);
		fontWidth.put(":",2);
		fontWidth.put(";",2);
		fontWidth.put("<",5);
		fontWidth.put("=",6);
		fontWidth.put(">",5);
		fontWidth.put("?",6);
		fontWidth.put("@",7);
		fontWidth.put("A",6);
		fontWidth.put("B",6);
		fontWidth.put("C",6);
		fontWidth.put("D",6);
		fontWidth.put("E",6);
		fontWidth.put("F",6);
		fontWidth.put("G",6);
		fontWidth.put("H",6);
		fontWidth.put("I",4);
		fontWidth.put("J",6);
		fontWidth.put("K",6);
		fontWidth.put("L",6);
		fontWidth.put("M",6);
		fontWidth.put("N",6);
		fontWidth.put("O",6);
		fontWidth.put("P",6);
		fontWidth.put("Q",6);
		fontWidth.put("R",6);
		fontWidth.put("S",6);
		fontWidth.put("T",6);
		fontWidth.put("U",6);
		fontWidth.put("V",6);
		fontWidth.put("W",6);
		fontWidth.put("X",6);
		fontWidth.put("Y",6);
		fontWidth.put("Z",6);
		fontWidth.put("_",6);
		fontWidth.put("'",3);
		fontWidth.put("a",6);
		fontWidth.put("b",6);
		fontWidth.put("c",6);
		fontWidth.put("d",6);
		fontWidth.put("e",6);
		fontWidth.put("f",5);
		fontWidth.put("g",6);
		fontWidth.put("h",6);
		fontWidth.put("i",2);
		fontWidth.put("j",6);
		fontWidth.put("k",5);
		fontWidth.put("l",3);
		fontWidth.put("m",6);
		fontWidth.put("n",6);
		fontWidth.put("o",6);
		fontWidth.put("p",6);
		fontWidth.put("q",6);
		fontWidth.put("r",6);
		fontWidth.put("s",6);
		fontWidth.put("t",4);
		fontWidth.put("u",6);
		fontWidth.put("v",6);
		fontWidth.put("w",6);
		fontWidth.put("x",6);
		fontWidth.put("y",6);
		fontWidth.put("z",6);
	}
	
	/**
	 * Get width of string in pixels
	 * @since 0.5.1
	 * @param text String
	 * @return Length of string in pixels
	 */
	public int stringWidth(String text) {
		if (fontWidth.isEmpty()) {
			return 0;
		}
		char[] chars = text.toCharArray();
		int width = 0;
		for (char current : chars) {
			if (fontWidth.containsKey(String.valueOf(current))) {
				width += fontWidth.get(String.valueOf(current));
			}
		}
		return width;
	}
}