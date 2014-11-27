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
	private static final String RHS_DELIMITER = "->";

	/**
	 * Ancestor ProductionRule of this ProductionRule (i.e., the first
	 * instance of this ProductionRule where {@link #POSITION} {@code = 0}).
	 */
	private final ProductionRule ANCESTOR;

	/**
	 * Current read position (in symbols) within the right-hand side of this
	 * ProductionRule. Each succeeding generation of this ProductionRule will
	 * have it's position incremented by {@code 1}. Initially set to
	 * {@code 0}.
	 */
	private final int POSITION; // TODO: change this to a short?

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
		this.ANCESTOR = this; // TODO: Fix leaking "this" in constructor
		this.POSITION = 0;
		this.NONTERMINAL = nonterminal;
		this.RHS = Objects.requireNonNull(rhs, "Productions must have a non-null RHS");
	}

	/**
	 * Private constructor which is used to generate the succeeding generation
	 * of this ProductionRule with {@link #POSITION} {@code = p.}
	 * {@link #POSITION}{@code +1}.
	 *
	 * @param p parent ProductionRule to generate this successor from
	 */
	private ProductionRule(ProductionRule p) {
		assert p != null;
		this.ANCESTOR = p.ANCESTOR;
		this.POSITION = p.POSITION+1;
		this.NONTERMINAL = p.NONTERMINAL;
		this.RHS = p.RHS;
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
	 * Returns the ancestor of this ProductionRule (i.e., the ProductionRule
	 * where the read position is set to {@code 0}).
	 *
	 * @return the ancestor of this ProductionRule
	 */
	public ProductionRule getAncestor() {
		return ANCESTOR;
	}

	/**
	 * Returns whether or not this ProductionRule can generate another
	 * generation. A ProductionRule can generate another generation if the
	 * read position can advance past another symbol. I.e., a ProductionRule
	 * can generate a succeeding generation if the current read position is
	 * &lt; the number of Symbols on the right-hand side of this
	 * ProductionRule.
	 *
	 * @return {@code true} if it is, otherwise {@code false}
	 */
	public boolean hasNext() {
		return POSITION < RHS.size();
	}

	/**
	 * Returns the successor to this ProductionRule with the read position
	 * incremented by {@code 1}.
	 *
	 * @return the successor to this ProductionRule
	 */
	public ProductionRule next() {
		assert hasNext() : "The succeeding generation of this production is redundant. Read position is already at the end.";
		return new ProductionRule(this);
	}

	/**
	 * Returns the current Symbol at the read position in the right-hand side
	 * of this ProductionRule.
	 *
	 * @return the current Symbol at the read position in the right-hand side
	 *	of this ProductionRule or {@code null} if no Symbol has been read yet
	 */
	public Symbol currentSymbol() {
		if (POSITION == 0) {
			return null;
		}

		return RHS.get(POSITION-1);
	}

	/**
	 * Returns the Symbol which is at the next read position (i.e.,
	 * {@code POSITION+1}) in the right-hand side of this ProductionRule.
	 *
	 * @return the Symbol which is at the next read position (i.e.,
	 *	{@code POSITION+1}) in the right-hand side of this ProductionRule or
	 *	{@code null} if the read position is beyond the number of symbols
	 *	in the RHS of this ProductionRule.
	 */
	public Symbol peekNextSymbol() {
		if (!hasNext()) {
			return null;
		}

		return RHS.get(POSITION);
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
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<Symbol> iterator() {
		return RHS.listIterator(POSITION);
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

		ProductionRule other = (ProductionRule)this;
		if (this.NONTERMINAL != other.NONTERMINAL || this.POSITION != other.POSITION || this.size() != other.size()) {
			return false;
		}

		return this.RHS.equals(other.RHS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.NONTERMINAL, this.POSITION, this.RHS);
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
			if (i == POSITION) {
				sb.append(" .");
			}

			sb.append(String.format(" %s", translator.get(s)));
			i++;
		}

		if (POSITION == RHS.size()) {
			sb.append(" .");
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
		StringBuilder sb = new StringBuilder(String.format("%d %s", NONTERMINAL, RHS_DELIMITER));

		int i = 0;
		for (Symbol s : RHS) {
			if (i == POSITION) {
				sb.append(" .");
			}

			sb.append(String.format(" %d", s));
			i++;
		}

		if (POSITION == RHS.size()) {
			sb.append(" .");
		}

		return sb.toString();
	}
}
