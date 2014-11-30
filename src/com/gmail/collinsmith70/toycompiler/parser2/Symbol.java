package com.gmail.collinsmith70.toycompiler.parser2;

/**
 * This class represents lexical elements used in specifying the production
 * rules constituting a formal grammar. Each symbol should have an associated
 * identifier, which is unique to that symbol.
 *
 * @author Collin Smith <strong>collinsmith70@gmail.com</strong>
 *
 * @param <E> the type associated with the parent of this Symbol
 */
public class Symbol<E extends Symbol> implements Comparable<Symbol> {
	/**
	 * Unique identifier for this Symbol.
	 */
	private final int ID;

	/**
	 * Reference to the parent of this Symbol.
	 */
	private final E PARENT;

	/**
	 * Constructs a Symbol with the specified unique identifier.
	 *
	 * @param id unique identifier representing this Symbol
	 */
	public Symbol(int id) {
		this(id, null);
	}

	/**
	 * Constructs a Symbol with the specified unique identifier and a
	 * reference to the parent Symbol of this Symbol.
	 *
	 * @param id unique identifier representing this Symbol
	 * @param parent reference to the Symbol parent of this Symbol
	 */
	public Symbol(int id, E parent) {
		this.ID = id;
		this.PARENT = parent;
	}

	/**
	 * Returns the unique identifier for this Symbol.
	 *
	 * @return the unique identifier for this Symbol
	 */
	public int getId() {
		return ID;
	}

	/**
	 * Returns a reference to the parent of this Symbol. This reference
	 * represents that this Symbol is not an actual Symbol, only another
	 * way of representing a Symbol, i.e., they are semantically equal.
	 *
	 * @return a reference to the parent Symbol of this Symbol
	 */
	public E getParent() {
		return PARENT;
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

		Symbol other = (Symbol)obj;
		return this.ID == other.ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return ID;
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(Symbol o) {
		return Integer.compare(this.ID, o.ID);
	}
}
