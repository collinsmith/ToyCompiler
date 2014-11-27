package com.gmail.collinsmith70.toycompiler.parser2;

/**
 * Nonterminal symbols (or syntactic variables) are replaced by groups of
 * terminal symbols ({@link TerminalSymbol}) according to the production rules.
 * The terminals and nonterminals of a particular grammar are two disjoint sets.
 * Nonterminal symbols are those symbols which can be replaced. They may also be
 * called simply syntactic variables. A formal grammar includes a start symbol,
 * a designated member of the set of nonterminals from which all the strings in
 * the language may be derived by successive applications of the production
 * rules. In fact, the language defined by a grammar is precisely the set of
 * terminal strings that can be so derived.
 *
 * @author Collin Smith <strong>collinsmith70@gmail.com</strong>
 */
public class NonterminalSymbol extends Symbol {
	/**
	 * Constructs a NonterminalSymbol with the specified unique identifier.
	 *
	 * @param id unique identifier representing this NonterminalSymbol
	 */
	public NonterminalSymbol(int id) {
		super(id);
	}

	/**
	 * Returns a String representation of this NonterminalSymbol formated as
	 * simply the unique identifier which should be associated with this
	 * NonterminalSymbol preceeded by an underscore.
	 *
	 * @return a String representation of this Symbol
	 */
	@Override
	public String toString() {
		return "_" + super.toString();
	}
}
