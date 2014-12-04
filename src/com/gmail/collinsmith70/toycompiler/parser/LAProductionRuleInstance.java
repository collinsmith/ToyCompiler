package com.gmail.collinsmith70.toycompiler.parser;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class LAProductionRuleInstance implements Iterable<Symbol>, Instanceable<LAProductionRuleInstance> {
	private final ProductionRuleInstance INSTANCE;
	private final LAProductionRuleInstance PARENT;
	private final Set<TerminalSymbol> FOLLOW;

	public LAProductionRuleInstance(ProductionRuleInstance instance, Set<TerminalSymbol> followSet) {
		this.INSTANCE = Objects.requireNonNull(instance, "Instance objects for LAProductionRuleInstance objects cannot be null.");
		this.PARENT = null;
		this.FOLLOW = Objects.requireNonNull(followSet, "Lookahead sets for LAProductionRuleInstance objects cannot be null.");
	}

	private LAProductionRuleInstance(LAProductionRuleInstance p) {
		this.INSTANCE = p.INSTANCE.next();
		this.PARENT = p;
		this.FOLLOW = new HashSet<>(p.FOLLOW);
	}

	@Override
	public boolean hasNext(int n) {
		return INSTANCE.hasNext(n);
	}

	@Override
	public LAProductionRuleInstance next() {
		assert hasNext() : "The succeeding generation of this production is redundant. Read position is already at the end.";
		return new LAProductionRuleInstance(this);
	}

	@Override
	public Symbol currentSymbol() {
		return INSTANCE.currentSymbol();
	}

	@Override
	public Symbol lookahead(int n) {
		return INSTANCE.lookahead(n);
	}

	public ProductionRuleInstance getInstance() {
		return INSTANCE;
	}

	public LAProductionRuleInstance getParent() {
		return PARENT;
	}

	public Set<TerminalSymbol> getFollowSet() {
		//return ImmutableSet.copyOf(FOLLOW);
		return FOLLOW;
	}

	public boolean addFollowSymbol(TerminalSymbol s) {
		return FOLLOW.add(s);
	}

	public boolean addAllFollowSymbols(Collection<? extends TerminalSymbol> c) {
		/*if (FOLLOW instanceof ImmutableSet) {
			boolean changed = !FOLLOW.containsAll(c);
			if (!changed) {
				return false;
			}

			FOLLOW = ImmutableSet.<TerminalSymbol>builder()
				.addAll(FOLLOW)
				.addAll(c)
				.build();
			return true;
		}*/

		return FOLLOW.addAll(c);
	}

	@Override
	public Iterator<Symbol> iterator() {
		return INSTANCE.iterator();
	}

	@Override
	public boolean equals(Object obj) {
		return INSTANCE.equals(obj);
	}

	@Override
	public int hashCode() {
		return INSTANCE.hashCode();
	}

	public String toString(Map<Symbol, String> symbolTable) {
		return INSTANCE.toString(symbolTable);
	}

	@Override
	public String toString() {
		return INSTANCE.toString();
	}
}
