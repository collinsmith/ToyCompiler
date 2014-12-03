package com.gmail.collinsmith70.toycompiler.parser2.slr;

import com.gmail.collinsmith70.toycompiler.parser2.AbstractParserStatesGenerator;
import com.gmail.collinsmith70.toycompiler.parser2.Grammar;
import com.gmail.collinsmith70.toycompiler.parser2.NonterminalSymbol;
import com.gmail.collinsmith70.toycompiler.parser2.ProductionRuleInstance;
import com.gmail.collinsmith70.toycompiler.parser2.State;
import com.gmail.collinsmith70.toycompiler.parser2.Symbol;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

public class SLRParserStatesGenerator extends AbstractParserStatesGenerator {
	public SLRParserStatesGenerator() {
		//...
	}

	@Override
	protected void generateChildState(
		State.Metadata<ProductionRuleInstance> metadata,
		Map<Set<ProductionRuleInstance>, State<ProductionRuleInstance>> states,
		Deque<State.Metadata<ProductionRuleInstance>> deque,
		Grammar g,
		Map<NonterminalSymbol, ImmutableSet<ProductionRuleInstance>> productionRuleInstances
	) {
		State<ProductionRuleInstance> parent = metadata.getParent();
		Symbol symbol = metadata.getSymbol();
		ImmutableSet<ProductionRuleInstance> kernelItems = metadata.getKernelItems();

		State<ProductionRuleInstance> existingState = states.get(kernelItems);
		if (existingState != null) {
			parent.putTransition(symbol, existingState);
			return;
		}

		Set<ProductionRuleInstance> closureItems = new HashSet<>();
		kernelItems.stream()
			.forEachOrdered(kernelItem -> closeOver(kernelItem, g, kernelItems, closureItems, productionRuleInstances));

		// TODO: change from list to set?
		List<Symbol> viablePrefixes;
		if (parent != null) {
			viablePrefixes = new LinkedList<>(parent.getViablePrefixes());
			viablePrefixes.add(symbol);
		} else {
			viablePrefixes = new LinkedList<>();
		}

		int stateId = states.size();
		State state = new State<>(
			stateId,
			parent,
			ImmutableList.copyOf(viablePrefixes), // TODO: creating more lists than necessary?
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
		Set<ProductionRuleInstance> reductions = new HashSet<>();
		while (!remainingProductions.isEmpty()) {
			p = remainingProductions.pollFirst();
			if (p.hasNext()) {
				Symbol lookahead = p.peekNextSymbol();
				Set<ProductionRuleInstance> productionsWithSameLookahead = new HashSet<>();
				remainingProductions.stream()
					.filter(sibling -> (sibling.hasNext() && Objects.equals(sibling.peekNextSymbol(), lookahead)))
					.forEachOrdered(sibling -> {
						productionsWithSameLookahead.add(sibling);
					});

				remainingProductions.removeAll(productionsWithSameLookahead);
				productionsWithSameLookahead.add(p);

				Set<ProductionRuleInstance> nextKernelItems = new HashSet<>();
				productionsWithSameLookahead.stream()
					.forEachOrdered((productionWithSameNextSymbol) -> nextKernelItems.add(productionWithSameNextSymbol.next()));

				State.Metadata<ProductionRuleInstance> childMetadata = state.getChildMetadata(lookahead, ImmutableSet.copyOf(nextKernelItems));
				deque.offerLast(childMetadata);
			} else {
				reductions.add(p);
			}
		}

		if (1 < reductions.size()) {
			//numWithReduceReduce++;
			g.getLogger().warning(String.format("State %d has %d reduce productions:", stateId, reductions.size()));
			reductions.stream()
				.forEachOrdered(reduce -> g.getLogger().warning(String.format("\t%s", reduce.toString(g.getSymbolsTable().inverse()))));
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
			productionRuleInstances.get(lookahead).stream()
				.filter(closureItem -> !(kernelItems.contains(closureItem) || closureItems.contains(closureItem)))
				.forEachOrdered(closureItem -> {
					closureItems.add(closureItem);
					closeOver(closureItem, g, kernelItems, closureItems, productionRuleInstances);
				});
		}
	}

	public static void outputStates(
		Grammar g,
		Map<Set<ProductionRuleInstance>, State<ProductionRuleInstance>> states
	) {
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(".", "output", g.getName() + ".slr.tables"), Charset.forName("US-ASCII"), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
			for (State<ProductionRuleInstance> state : states.values()) {
				writer.write(String.format("State %d: V%s", state.getId(), state.getViablePrefixes()));
				if (state.getParent() != null) {
					writer.write(String.format(" = goto(S%d, %s)",
						state.getParent().getId(),
						state.getViablePrefixes().get(state.getViablePrefixes().size()-1)
					));
				}

				writer.write(String.format("%n"));

				for (ProductionRuleInstance p : state.getKernelItems()) {
					writeProduction(writer, g, state, p, true);
				}

				for (ProductionRuleInstance p : state.getClosureItems()) {
					writeProduction(writer, g, state, p, false);
				}

				writer.write(String.format("%n"));
				writer.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void writeProduction(
		BufferedWriter writer,
		Grammar g,
		State<ProductionRuleInstance> s,
		ProductionRuleInstance p,
		boolean isKernelItem
	) throws IOException {
		if (!p.hasNext()) {
			writer.write(String.format("%-4s %-48s reduce(%d)%n",
				isKernelItem ? "I:" : "",
				p.toString(g.getSymbolsTable().inverse()),
				g.getProductionRules().indexOf(p.getAncestor())
			));

			return;
		}

		Symbol lookahead = p.peekNextSymbol();
		writer.write(String.format("%-4s %-48s %-32s %s%n",
			isKernelItem ? "I:" : "",
			p.toString(g.getSymbolsTable().inverse()),
			String.format("goto(S%d, %s)",
				s.getTransition(lookahead).getId(),
				g.getSymbolsTable().inverse().get(lookahead)
			),
			(lookahead instanceof NonterminalSymbol) ? "" : "shift"
		));
	}
}
