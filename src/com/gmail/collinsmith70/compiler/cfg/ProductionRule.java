package com.gmail.collinsmith70.compiler.cfg;

import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class ProductionRule implements Iterable<Symbol>, Comparable<ProductionRule> {
	private static final char RHS_DELIMITER = '\u2192';

	private final int ID;
	private final NonterminalSymbol NONTERMINAL_SYMBOL;
	private final ImmutableList<Symbol> RHS;

	public ProductionRule(int id, NonterminalSymbol nonterminalSymbol, ImmutableList<Symbol> rhs) {
		this.ID = id;
		this.NONTERMINAL_SYMBOL = Objects.requireNonNull(nonterminalSymbol);
		this.RHS = Objects.requireNonNull(rhs);
	}

	public int getId() {
		return ID;
	}

	public NonterminalSymbol getNonterminalSymbol() {
		return NONTERMINAL_SYMBOL;
	}

	public ImmutableList<Symbol> getRHS() {
		return RHS;
	}

	@Override
	public Iterator<Symbol> iterator() {
		return RHS.listIterator();
	}

	@Override
	public int compareTo(ProductionRule otherProductionRule) {
		return Integer.compare(this.ID, otherProductionRule.ID);
	}

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

		ProductionRule otherProductionRule = (ProductionRule)obj;
		return this.NONTERMINAL_SYMBOL.equals(otherProductionRule.NONTERMINAL_SYMBOL)
			&& this.RHS.equals(otherProductionRule.RHS);
	}

	@Override
	public int hashCode() {
		// Hashing the size() as well, because a few cases were found which
		// two non-equal production were returning the same hashCode values
		return Objects.hash(NONTERMINAL_SYMBOL, RHS.size(), RHS);
	}

	public String toString(Map<Symbol, String> symbolTable) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("%16s", symbolTable.get(NONTERMINAL_SYMBOL)));
		sb.append(' ');
		sb.append(RHS_DELIMITER);
		for (Symbol s : RHS) {
			sb.append(' ');
			sb.append(symbolTable.get(s));
		}

		return sb.toString();
	}
}
