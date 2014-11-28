package com.gmail.collinsmith70.toycompiler.parser2;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * A production rule, or simply production, in computer science is a rewrite
 * rule specifying a symbol substitution that can be recursively performed to
 * generate new symbol sequences.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Production_(computer_science)">https://en.wikipedia.org/wiki/Production_(computer_science)</a>
 *
 * @author Collin Smith <strong>collinsmith70@gmail.com</strong>
 */
public class ProductionRule implements Iterable<Symbol> {
	/**
	 * String representing the delimiter to use when outputting the String
	 * representation of this ProductionRule.
	 */
	public static final String RHS_DELIMITER = "->";

	/**
	 * NonterminalSymbol generating this ProductionRule (i.e., producing the
	 * Symbols on the right-hand side ({@link #RHS})).
	 */
	private final NonterminalSymbol NONTERMINAL;

	/**
	 * List of Symbols which are produced by this ProductionRule. This list is
	 * made immutable to prevent issues with succeeding generations.
	 */
	private final ImmutableList<Symbol> RHS;

	/**
	 * Constructs a ProductionRule which given a NonterminalSymbol, will
	 * produce the list of Symbols (referenced as the right-hand side).
	 *
	 * @param nonterminal NonterminalSymbol generating this ProductionRule
	 * @param rhs Symbols that the NonterminalSymbol will generate according
	 *	to this ProductionRule
	 */
	public ProductionRule(NonterminalSymbol nonterminal, ImmutableList<Symbol> rhs) {
		this.NONTERMINAL = Objects.requireNonNull(nonterminal, "Production rules must have a non-null nonterminal symbol");
		this.RHS = Objects.requireNonNull(rhs, "Production rules must have a non-null RHS");
	}

	public ProductionRule(ProductionRule p) {
		this(Objects.requireNonNull(p).NONTERMINAL, Objects.requireNonNull(p).RHS);
		//Preconditions.checkNotNull(p);
		//this.NONTERMINAL = p.NONTERMINAL;
		//this.RHS = p.RHS;
	}

	/**
	 * Returns the NonterminalSymbol which produces this ProductionRule.
	 *
	 * @return the NonterminalSymbol which produces this ProductionRule
	 */
	public NonterminalSymbol getNonterminalSymbol() {
		return NONTERMINAL;
	}

	/**
	 * Returns an immutable list of symbols representing the right-hand side
	 * of this ProductionRule.
	 *
	 * @return an immutable list of symbols representing the right-hand side
	 *	of this ProductionRule
	 */
	public ImmutableList<Symbol> getRHS() {
		return RHS;
	}

	/**
	 * Returns the number of symbols in the right-hand side of this
	 * ProductionRule.
	 *
	 * @return the number of symbols in the right-hand side of this
	 *	ProductionRule.
	 */
	public int size() {
		return RHS.size();
	}

	/**
	 * Creates a ProductionRuleInstance from this ProductionRule and
	 * return it.
	 *
	 * @return a ProductionRuleInstance created from this ProductionRule
	 */
	public ProductionRuleInstance createInstance() {
		return new ProductionRuleInstance(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<Symbol> iterator() {
		return RHS.listIterator(0);
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

		if (!(obj instanceof ProductionRule)) {
			return false;
		}

		ProductionRule other = (ProductionRule)obj;
		if (!this.NONTERMINAL.equals(other.NONTERMINAL) || this.RHS.size() != other.RHS.size()) {
			return false;
		}

		return this.RHS.equals(other.RHS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hash(NONTERMINAL, RHS, RHS.size());
	}

	/**
	 * Returns a String representation of this ProductionRule replacing the
	 * Symbol identifiers with their String counter-parts. This method is
	 * useful for debugging because it produces output which is easier to
	 * understand than only Symbol identifiers.
	 *
	 * @see #toString()
	 *
	 * @param translator a map using Symbols as keys to retrieve String
	 *	representations of those Symbols.
	 *
	 * @return a String representation of this ProductionRule with Symbols
	 *	translated into their String representations
	 */
	public String toString(Map<Symbol, String> translator) {
		Preconditions.checkNotNull(translator);

		StringBuilder sb = new StringBuilder(String.format("%16s %s", translator.get(NONTERMINAL), RHS_DELIMITER));

		int i = 0;
		for (Symbol s : RHS) {
			sb.append(String.format(" %s", translator.get(s)));
			i++;
		}

		return sb.toString();
	}

	/**
	 * Returns a String representation of this ProductionRule.
	 *
	 * @see #toString(java.util.Map)
	 *
	 * @return a String representation of this ProductionRule
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(String.format("%s %s", NONTERMINAL, RHS_DELIMITER));

		int i = 0;
		for (Symbol s : RHS) {
			sb.append(String.format(" %s", s));
			i++;
		}

		return sb.toString();
	}
}
