package com.gmail.collinsmith70.compiler.cfg;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ContextFreeGrammar implements Iterable<ProductionRule> {
	private final String NAME;
	private final ImmutableBiMap<NonterminalSymbol, ImmutableSet<ProductionRule>> PRODUCTION_RULES;

	private ContextFreeGrammar() {
		NAME = "";
		PRODUCTION_RULES = null;
	}

	@Override
	public Iterator<ProductionRule> iterator() {
		List<Iterator<ProductionRule>> iterators = new ArrayList<>(PRODUCTION_RULES.size());
		for (Set<ProductionRule> productionRuleSet : PRODUCTION_RULES.values()) {
			iterators.add(productionRuleSet.iterator());
		}

		return Iterators.concat(iterators.iterator());
	}
}
