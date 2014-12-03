package com.gmail.collinsmith70.toycompiler.parser2;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

public abstract class AbstractParserStatesGenerator {
	abstract protected void generateChildState(
		State.Metadata<ProductionRuleInstance> metadata,
		Map<Set<ProductionRuleInstance>, State<ProductionRuleInstance>> states,
		Deque<State.Metadata<ProductionRuleInstance>> deque,
		Grammar g,
		Map<NonterminalSymbol, ImmutableSet<ProductionRuleInstance>> productionRuleInstances
	);

	abstract protected void closeOver(
		ProductionRuleInstance p,
		Grammar g,
		Set<ProductionRuleInstance> kernelItems,
		Set<ProductionRuleInstance> closureItems,
		Map<NonterminalSymbol, ImmutableSet<ProductionRuleInstance>> productionRuleInstances
	);

	public Map<Set<ProductionRuleInstance>, State<ProductionRuleInstance>> generateParserTables(Grammar g) {
		Preconditions.checkNotNull(g);

		g.getLogger().info("Creating production rule instances...");
		long dt = System.currentTimeMillis();
		BiMap<NonterminalSymbol, ImmutableSet<ProductionRuleInstance>> productionRuleInstances = HashBiMap.create();
		g.getProductionRulesMap().entrySet().stream()
			.forEachOrdered(entry -> productionRuleInstances.put(entry.getKey(), createProductionRuleInstances(entry.getValue())));
		final ImmutableBiMap<NonterminalSymbol, ImmutableSet<ProductionRuleInstance>> immutableProductionRuleInstances = ImmutableBiMap.copyOf(productionRuleInstances);
		g.getLogger().info(String.format("Production rule instances generated in %dms; %d production rules",
			System.currentTimeMillis()-dt,
			immutableProductionRuleInstances.size()
		));

		g.getLogger().info("Generating parser states...");
		dt = System.currentTimeMillis();
		Map<Set<ProductionRuleInstance>, State<ProductionRuleInstance>> states = generateParserStates(g, immutableProductionRuleInstances);
		g.getLogger().info(String.format("Parser states generated in %dms; %d states",
			System.currentTimeMillis()-dt,
			states.size()
		));

		return states;
	}

	protected Map<Set<ProductionRuleInstance>, State<ProductionRuleInstance>> generateParserStates(
		Grammar g,
		Map<NonterminalSymbol, ImmutableSet<ProductionRuleInstance>> productionRuleInstances
	) {
		Map<Set<ProductionRuleInstance>, State<ProductionRuleInstance>> states = new LinkedHashMap<>();

		Deque<State.Metadata<ProductionRuleInstance>> deque = new LinkedBlockingDeque<>();
		deque.offerLast(new State.Metadata<>(
			null,
			null,
			productionRuleInstances.get(g.getInitialNonterminalSymbol())
		));

		while (!deque.isEmpty()) {
			generateChildState(deque.pollFirst(), states, deque, g, productionRuleInstances);
		}

		return states;
	}

	private ImmutableSet<ProductionRuleInstance> createProductionRuleInstances(Set<ProductionRule> productionRules) {;
		return ImmutableSet.copyOf(
			productionRules.stream()
				.map(productionRule -> new ProductionRuleInstance(productionRule))
				.iterator()
		);
	}
}
