package com.gmail.collinsmith70.toycompiler.parser;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class ProductionRule implements Iterable<Symbol>, Comparable<ProductionRule> {
	//public static final String RHS_DELIMITER = "->";
	public static final String RHS_DELIMITER = "\u2192";

	private final int ID;
	private final NonterminalSymbol NONTERMINAL;
	private final ImmutableList<Symbol> RHS;

	public ProductionRule(NonterminalSymbol nonterminal, ImmutableList<Symbol> rhs) {
		this(nonterminal, rhs, Integer.MIN_VALUE);
	}

	public ProductionRule(NonterminalSymbol nonterminal, ImmutableList<Symbol> rhs, int id) {
		this.ID = id;
		this.NONTERMINAL = Objects.requireNonNull(nonterminal, "Production rules must have a non-null nonterminal symbol");
		this.RHS = Objects.requireNonNull(rhs, "Production rules must have a non-null RHS");
	}

	public int getId() {
		return ID;
	}

	public NonterminalSymbol getNonterminalSymbol() {
		return NONTERMINAL;
	}

	public ImmutableList<Symbol> getRHS() {
		return RHS;
	}

	@Override
	public Iterator<Symbol> iterator() {
		return RHS.listIterator(0);
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

		ProductionRule other = (ProductionRule)obj;
		return this.NONTERMINAL.equals(other.NONTERMINAL)
			&& this.RHS.size() != other.RHS.size()
			&& this.RHS.equals(other.RHS);
	}

	@Override
	public int hashCode() {
		return Objects.hash(NONTERMINAL, RHS.size(), RHS);
	}

	public String toString(Map<Symbol, String> symbolTable) {
		Preconditions.checkNotNull(symbolTable);

		StringBuilder sb = new StringBuilder();
		sb.append(String.format("%16s", symbolTable.get(NONTERMINAL)));
		sb.append(' ');
		sb.append(RHS_DELIMITER);
		for (Symbol s : RHS) {
			sb.append(' ');
			sb.append(symbolTable.get(s));
		}

		return sb.toString();
	}

	@Override
	public int compareTo(ProductionRule o) {
		return Integer.compare(ID, o.ID);
	}
}
