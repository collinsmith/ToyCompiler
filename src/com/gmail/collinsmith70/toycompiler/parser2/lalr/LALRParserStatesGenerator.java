package com.gmail.collinsmith70.toycompiler.parser2.lalr;

import com.gmail.collinsmith70.toycompiler.parser2.AbstractParserStatesGenerator;
import com.gmail.collinsmith70.toycompiler.parser2.Grammar;
import com.gmail.collinsmith70.toycompiler.parser2.NonterminalSymbol;
import com.gmail.collinsmith70.toycompiler.parser2.ProductionRuleInstance;
import com.gmail.collinsmith70.toycompiler.parser2.State;
import com.gmail.collinsmith70.toycompiler.parser2.Symbol;
import com.google.common.collect.ImmutableSet;
import java.util.Deque;
import java.util.Map;
import java.util.Set;

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
					existingItem.addAllLookaheads(repeatedItem.getLookaheads());
				}
			}
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

	}
}
