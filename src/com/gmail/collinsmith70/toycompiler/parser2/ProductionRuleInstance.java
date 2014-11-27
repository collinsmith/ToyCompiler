package com.gmail.collinsmith70.toycompiler.parser2;

import com.google.common.base.Preconditions;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * A production rule instance is a ProductionRule which maintains a link to it's
 * ancestor as well as a position field which tracks where the current read
 * position is. This class is made immutable so that succeeding generations with
 * different read positions will require new classes to be created.
 *
 * @author Collin Smith <strong>collinsmith70@gmail.com</strong>
 */
public class ProductionRuleInstance extends ProductionRule implements Iterable<Symbol> {
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

	public ProductionRuleInstance(ProductionRule ancestor) {
		super(ancestor);
		this.ANCESTOR = ancestor;
		this.POSITION = 0;
	}

	/**
	 * Private constructor which is used to generate the succeeding generation
	 * of this ProductionRule with {@link #POSITION} {@code = p.}
	 * {@link #POSITION}{@code +1}.
	 *
	 * @param p parent ProductionRule to generate this successor from
	 */
	private ProductionRuleInstance(ProductionRuleInstance p) {
		super(p.ANCESTOR);
		this.ANCESTOR = p.ANCESTOR;
		this.POSITION = p.POSITION+1;
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
		return POSITION < ANCESTOR.getRHS().size();
	}

	/**
	 * Returns the successor to this ProductionRule with the read position
	 * incremented by {@code 1}.
	 *
	 * @return the successor to this ProductionRule
	 */
	public ProductionRuleInstance next() {
		assert hasNext() : "The succeeding generation of this production is redundant. Read position is already at the end.";
		return new ProductionRuleInstance(this);
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

		return ANCESTOR.getRHS().get(POSITION-1);
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

		return ANCESTOR.getRHS().get(POSITION);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<Symbol> iterator() {
		return ANCESTOR.getRHS().listIterator(POSITION);
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

		ProductionRuleInstance other = (ProductionRuleInstance)this;
		if (!this.equals(other)) {
			return false;
		}

		return this.POSITION != other.POSITION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.ANCESTOR, this.POSITION);
	}

	/**
	 * Returns a String representation of this ProductionRule replacing the
	 * Symbol identifiers with their String counter-parts. This method is
	 * useful for debugging because it produces output which is easier to
	 * understand than only Symbol identifiers.
	 *
	 * @see #toString()
	 *
	 * @param symbolTable a map using Symbols as keys to retrieve String
	 *	representations of those Symbols.
	 *
	 * @return a String representation of this ProductionRule with Symbols
	 *	translated into their String representations
	 */
	public String toString(Map<Symbol, String> symbolTable) {
		Preconditions.checkNotNull(symbolTable);

		StringBuilder sb = new StringBuilder(String.format("%16s %s", symbolTable.get(ANCESTOR.getNonterminalSymbol()), ProductionRule.RHS_DELIMITER));

		int i = 0;
		for (Symbol s : ANCESTOR) {
			if (i == POSITION) {
				sb.append(" .");
			}

			sb.append(String.format(" %s", symbolTable.get(s)));
			i++;
		}

		if (POSITION == ANCESTOR.size()) {
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
		StringBuilder sb = new StringBuilder(String.format("%d %s", ANCESTOR.getNonterminalSymbol(), ProductionRule.RHS_DELIMITER));

		int i = 0;
		for (Symbol s : ANCESTOR) {
			if (i == POSITION) {
				sb.append(" .");
			}

			sb.append(String.format(" %d", s));
			i++;
		}

		if (POSITION == ANCESTOR.size()) {
			sb.append(" .");
		}

		return sb.toString();
	}
}
