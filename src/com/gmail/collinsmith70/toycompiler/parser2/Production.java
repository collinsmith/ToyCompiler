package com.gmail.collinsmith70.toycompiler.parser2;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class Production implements Iterable<Symbol> {
	private static final String RHS_DELIMITER = "->";

	private final Production ANCESTOR;
	private final int POSITION;
	private final NonterminalSymbol NONTERMINAL;
	private final ImmutableList<Symbol> RHS;

	public Production(NonterminalSymbol nonterminal, ImmutableList<Symbol> rhs) {
		this.ANCESTOR = this;
		this.POSITION = 0;
		this.NONTERMINAL = nonterminal;
		this.RHS = Objects.requireNonNull(rhs, "Productions must have a non-null RHS");
	}
	
	private Production(Production p) {
		assert p != null;
		this.ANCESTOR = p.ANCESTOR;
		this.POSITION = p.POSITION+1;
		this.NONTERMINAL = p.NONTERMINAL;
		this.RHS = p.RHS;
	}
	
	public NonterminalSymbol getNonterminalSymbol() {
		return NONTERMINAL;
	}
	
	public Production getAncestor() {
		return ANCESTOR;
	}
	
	public boolean hasNext() {
		return POSITION < RHS.size();
	}
	
	public Production next() {
		return new Production(this);
	}
	
	public Symbol currentSymbol() {
		if (POSITION == 0) {
			return null;
		}
		
		return RHS.get(POSITION-1);
	}

	public Symbol peekNextSymbol() {
		if (!hasNext()) {
			return null;
		}
		
		return RHS.get(POSITION);
	}
	
	public int size() {
		return RHS.size();
	}
	
	@Override
	public Iterator<Symbol> iterator() {
		return RHS.listIterator(POSITION);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		
		if (!(obj instanceof Production)) {
			return false;			
		}
		
		Production other = (Production)this;
		if (this.NONTERMINAL != other.NONTERMINAL || this.POSITION != other.POSITION || this.size() != other.size()) {
			return false;
		}
		
		return this.RHS.equals(other.RHS);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.NONTERMINAL, this.POSITION, this.RHS);
	}
	
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
