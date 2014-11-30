package com.gmail.collinsmith70.toycompiler.parser2.lalr;

import com.gmail.collinsmith70.toycompiler.parser2.AbstractParserStatesGenerator;
import com.gmail.collinsmith70.toycompiler.parser2.Grammar;
import com.gmail.collinsmith70.toycompiler.parser2.NonterminalSymbol;
import com.gmail.collinsmith70.toycompiler.parser2.ProductionRuleInstance;
import com.gmail.collinsmith70.toycompiler.parser2.State;
import com.gmail.collinsmith70.toycompiler.parser2.Symbol;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

public class LALRParserStatesGenerator extends AbstractParserStatesGenerator {
	public LALRParserStatesGenerator() {
		//...
	}

	@Override
	protected void generateChildState(
		State.Metadata metadata,
		Map<Set<ProductionRuleInstance>, State> states,
		Deque<State.Metadata> deque,
		Grammar g,
		Map<NonterminalSymbol, ImmutableSet<ProductionRuleInstance>> productionRuleInstances
	) {
		State parent = metadata.getParent();
		Symbol symbol = metadata.getSymbol();
		ImmutableSet<ProductionRuleInstance> kernelItems = metadata.getKernelItems();

		State existingState = states.get(kernelItems);
		if (existingState != null) {
			parent.putTransition(symbol, existingState);
			// TODO: replace this (n^2) with something better?
			for (ProductionRuleInstance repeatedItem : kernelItems) {
				for (ProductionRuleInstance existingItem : existingState.getKernelItems()) {
					if (repeatedItem.equals(existingItem)) {
						existingItem.addAllLookaheads(repeatedItem.getLookaheads());
						break;
					}
				}
			}

			return;
		}

		Set<ProductionRuleInstance> closureItems = new HashSet<>();
		kernelItems.stream()
			.forEach(kernelItem -> closeOver(kernelItem, g, kernelItems, closureItems, productionRuleInstances));

		List<Symbol> viablePrefixes;
		if (parent != null) {
			viablePrefixes = new LinkedList<>(parent.getViablePrefixes());
			viablePrefixes.add(symbol);
		} else {
			viablePrefixes = new LinkedList<>();
		}

		int stateId = states.size();
		State state = new State(
			stateId,
			parent,
			ImmutableList.copyOf(viablePrefixes),
			kernelItems,
			ImmutableSet.copyOf(closureItems)
		);

		states.put(kernelItems, state);
		if (parent != null) {
			parent.putTransition(symbol, state);
		}

		Deque<ProductionRuleInstance> remainingProductions = new LinkedBlockingDeque<>(kernelItems);
		remainingProductions.addAll(closureItems);

		ProductionRuleInstance p;
		Set<ProductionRuleInstance> reductions = new HashSet();
		while (!remainingProductions.isEmpty()) {
			p = remainingProductions.pollFirst();
			if (p.hasNext()) {
				Symbol lookahead = p.peekNextSymbol();
				Set<ProductionRuleInstance> productionsWithSameLookahead = new HashSet<>();
				remainingProductions.stream()
					.filter(sibling -> (sibling.hasNext() && Objects.equals(sibling.peekNextSymbol(), lookahead)))
					.forEach(sibling -> {
						productionsWithSameLookahead.add(sibling);
					});

				remainingProductions.removeAll(productionsWithSameLookahead);
				productionsWithSameLookahead.add(p);

				Set<ProductionRuleInstance> nextKernelItems = new HashSet<>();
				productionsWithSameLookahead.stream()
					.forEach((productionWithSameNextSymbol) -> nextKernelItems.add(productionWithSameNextSymbol.next()));

				State.Metadata childMetadata = state.getChildMetadata(lookahead, ImmutableSet.copyOf(nextKernelItems));
				deque.offerLast(childMetadata);
			} else {
				reductions.add(p);
			}
		}

		if (1 < reductions.size()) {
			//numWithReduceReduce++;
			g.getLogger().warning(String.format("State %d has %d reduce productions:", stateId, reductions.size()));
			reductions.stream()
				.forEach(reduce -> g.getLogger().warning(String.format("\t%s", reduce.toString(g.getSymbolsTable().inverse()))));
		}
	}

	@Override
	protected void closeOver(
		ProductionRuleInstance p,
		Grammar g,
		Set<ProductionRuleInstance> kernelItems,
		Set<ProductionRuleInstance> closureItems,
		Map<NonterminalSymbol, ImmutableSet<ProductionRuleInstance>> productionRuleInstances
	) {
		if (!p.hasNext()) {
			return;
		}

		Symbol lookahead = p.peekNextSymbol();
		if (lookahead instanceof NonterminalSymbol) {
			for (ProductionRuleInstance closureItem : productionRuleInstances.get(lookahead)) {
				if (!(kernelItems.contains(closureItem) || closureItems.contains(closureItem))) {
					closureItems.add(closureItem);
					//closeOver(closureItem, g, kernelItems, closureItems, productionRuleInstances);
				}
			}
		}
	}
}
