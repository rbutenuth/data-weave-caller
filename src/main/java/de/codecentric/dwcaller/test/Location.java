package de.codecentric.dwcaller.test;

import java.util.Map;

public class Location {
	private int index;
	private int line;
	private int column;
	
	public Location(Map<String, Object> data) {
		index = nullToMinusOone(data.get("index"));
		line = nullToMinusOone(data.get("line"));
		column = nullToMinusOone(data.get("column"));
	}

	private int nullToMinusOone(Object object) {
		if (object instanceof Number) {
			return ((Number)object).intValue() + 1;
		}
		return -1;
	}

	/**
	 * @return Position in file, 1 based.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @return Line number, 1 based.
	 */
	public int getLine() {
		return line;
	}

	/**
	 * @return Column number, 1 based.
	 */
	public int getColumn() {
		return column;
	}
}
