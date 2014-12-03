package com.gmail.collinsmith70.toycompiler.parser;

import com.google.common.base.Preconditions;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class ProductionRuleInstance implements Iterable<Symbol>, Instanceable<ProductionRuleInstance> {
	private final ProductionRule PRODUCTION_RULE;
	private final int POSITION;

	public ProductionRuleInstance(ProductionRule productionRule) {
		this.PRODUCTION_RULE = productionRule;
		this.POSITION = 0;
	}

	private ProductionRuleInstance(ProductionRuleInstance p) {
		assert p != null && p.hasNext(1);
		this.PRODUCTION_RULE = p.PRODUCTION_RULE;
		this.POSITION = p.POSITION+1;
	}

	public ProductionRule getProductionRule() {
		return PRODUCTION_RULE;
	}

	@Override
	public boolean hasNext(int n) {
		return POSITION+(n-1) < PRODUCTION_RULE.getRHS().size();
	}

	@Override
	public ProductionRuleInstance next() {
		assert hasNext() : "The succeeding generation of this production is redundant. Read position is already at the end.";
		return new ProductionRuleInstance(this);
	}

	@Override
	public Symbol currentSymbol() {
		if (POSITION == 0) {
			return null;
		}

		return PRODUCTION_RULE.getRHS().get(POSITION-1);
	}

	@Override
	public Symbol lookahead(int n) {
		if (!hasNext(n)) {
			return null;
		}

		return PRODUCTION_RULE.getRHS().get(POSITION+(n-1));
	}

	@Override
	public Iterator<Symbol> iterator() {
		return PRODUCTION_RULE.getRHS().listIterator(POSITION);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		if (!(obj instanceof ProductionRuleInstance)) {
			return false;
		}

		ProductionRuleInstance other = (ProductionRuleInstance)obj;
		return this.PRODUCTION_RULE.equals(other.PRODUCTION_RULE)
			&& this.POSITION == other.POSITION;
	}

	@Override
	public int hashCode() {
		return Objects.hash(PRODUCTION_RULE, POSITION);
	}

	public String toString(Map<Symbol, String> symbolTable) {
		Preconditions.checkNotNull(symbolTable);

		StringBuilder sb = new StringBuilder(symbolTable.get(PRODUCTION_RULE.getNonterminalSymbol()) + " " + ProductionRule.RHS_DELIMITER);

		int i = 0;
		for (Symbol s : PRODUCTION_RULE.getRHS()) {
			if (i == POSITION) {
				sb.append(" .");
			}

			sb.append(' ');
			sb.append(symbolTable.get(s));
			i++;
		}

		if (POSITION == PRODUCTION_RULE.getRHS().size()) {
			sb.append(" .");
		}

		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(PRODUCTION_RULE.getNonterminalSymbol().toString() + " " + ProductionRule.RHS_DELIMITER);

		int i = 0;
		for (Symbol s : PRODUCTION_RULE.getRHS()) {
			if (i == POSITION) {
				sb.append(" .");
			}

			sb.append(' ');
			sb.append(s.toString());
			i++;
		}

		if (POSITION == PRODUCTION_RULE.getRHS().size()) {
			sb.append(" .");
		}

		return sb.toString();
	}
}
