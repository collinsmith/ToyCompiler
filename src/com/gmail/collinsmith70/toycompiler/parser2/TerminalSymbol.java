package com.gmail.collinsmith70.toycompiler.parser2;

/**
 * Terminal symbols are the elementary symbols of the language defined by a
 * formal grammar. Terminal symbols are literal symbols which should appear in
 * the inputs to, or outputs from, the production rules of a formal grammar and
 * which cannot be changed using the rules of the grammar (this is the reason
 * for the name "terminal").
 *
 * @author Collin Smith <strong>collinsmith70@gmail.com</strong>
 */
public class TerminalSymbol extends Symbol<TerminalSymbol> {
	/**
	 * Constructs a TerminalSymbol with the specified unique identifier.
	 *
	 * @param id unique identifier representing this TerminalSymbol
	 */
	public TerminalSymbol(int id) {
		super(id);
	}

	/**
	 * Constructs a TerminalSymbol with the specified unique identifier and a
	 * reference to the parent TerminalSymbol of this TerminalSymbol.
	 *
	 * @param id unique identifier representing this TerminalSymbol
	 * @param parent reference to the TerminalSymbol parent of this
	 *	TerminalSymbol
	 */
	public TerminalSymbol(int id, TerminalSymbol parent) {
		super(id, parent);
	}
}
