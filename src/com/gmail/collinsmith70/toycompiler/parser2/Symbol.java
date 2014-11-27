package com.gmail.collinsmith70.toycompiler.parser2;

/**
 * This class represents lexical elements used in specifying the production
 * rules constituting a formal grammar. Each symbol should have an associated
 * identifier, which is unique to that symbol.
 *
 * @author Collin Smith <strong>collinsmith70@gmail.com</strong>
 */
public class Symbol {
	/**
	 * Unique identifier for this Symbol.
	 */
	private final int ID;

	/**
	 * Constructs a Symbol with the specified unique identifier.
	 *
	 * @param id unique identifier representing this Symbol
	 */
	public Symbol(int id) {
		this.ID = id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Integer.hashCode(ID);
	}

	/**
	 * {@inheritDoc}
	 */
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

		Symbol s = (Symbol)obj;
		return ID == s.ID;
	}

	/**
	 * Returns a String representation of this Symbol formated as simply the
	 * unique identifier which should be associated with this Symbol.
	 *
	 * @return a String representation of this Symbol
	 */
	@Override
	public String toString() {
		return Integer.toString(ID);
	}
}
