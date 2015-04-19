package com.gmail.collinsmith70.compiler.cfg;

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

		Symbol otherSymbol = (Symbol)obj;
		return this.ID == otherSymbol.ID;
	}

	@Override
	public int hashCode() {
		return ID;
	}

	@Override
	public int compareTo(Symbol otherSymbol) {
		return Integer.compare(this.ID, otherSymbol.ID);
	}

	@Override
	public String toString() {
		return Integer.toString(ID);
	}
}
