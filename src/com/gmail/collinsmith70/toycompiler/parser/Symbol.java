package com.gmail.collinsmith70.toycompiler.parser;

public abstract class Symbol implements Comparable<Symbol> {
	private final int ID;

	Symbol(int id) {
		this.ID = id;
	}

	public int getId() {
		return ID;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		if (!(obj instanceof Symbol)) {
			return false;
		}

		Symbol other = (Symbol)obj;
		return this.ID == other.ID;
	}

	@Override
	public int hashCode() {
		return ID;
	}

	@Override
	public int compareTo(Symbol o) {
		return Integer.compare(this.ID, o.ID);
	}

	@Override
	public String toString() {
		return Integer.toString(ID);
	}
}
