package com.gmail.collinsmith70.toycompiler.parser2.slr;

import com.gmail.collinsmith70.toycompiler.parser2.Grammar;
import com.gmail.collinsmith70.toycompiler.parser2.NonterminalSymbol;
import com.gmail.collinsmith70.toycompiler.parser2.ParserStatesGenerator;
import com.gmail.collinsmith70.toycompiler.parser2.ProductionRule;
import com.gmail.collinsmith70.toycompiler.parser2.ProductionRuleInstance;
import com.gmail.collinsmith70.toycompiler.parser2.State;
import com.gmail.collinsmith70.toycompiler.parser2.Symbol;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

public class SLRParserStatesGenerator implements ParserStatesGenerator {
	public SLRParserStatesGenerator() {
		//...
	}

	@Override
	public Map<Set<ProductionRuleInstance>, State> generateParserTables(Grammar g) {
		Preconditions.checkNotNull(g);

		g.getLogger().info("Creating production rule instances...");
		long dt = System.currentTimeMillis();
		BiMap<NonterminalSymbol, ImmutableSet<ProductionRuleInstance>> productionRuleInstances = HashBiMap.create();
		g.getProductionRulesMap().entrySet().stream()
			.forEach(entry -> productionRuleInstances.put(entry.getKey(), createProductionRuleInstances(entry.getValue())));
		final ImmutableBiMap<NonterminalSymbol, ImmutableSet<ProductionRuleInstance>> immutableProductionRuleInstances = ImmutableBiMap.copyOf(productionRuleInstances);
		g.getLogger().info(String.format("Production rule instances generated in %dms; %d production rules",
			System.currentTimeMillis()-dt,
			immutableProductionRuleInstances.size()
		));

		g.getLogger().info("Generating parser states...");
		dt = System.currentTimeMillis();
		Map<Set<ProductionRuleInstance>, State> states = generateParserStates(g, immutableProductionRuleInstances);
		g.getLogger().info(String.format("Parser states generated in %dms; %d states",
			System.currentTimeMillis()-dt,
			states.size()
		));

		/*dt = System.currentTimeMillis();
		g.getLogger().info("Generating SLR tables...");
		this.SLR_TABLES = generateSLRTables();
		g.getLogger().info(String.format("SLR tables generated in %dms; %d tables (%d shift-reduce conflicts, %d reduce-reduce conflicts)",
			System.currentTimeMillis()-dt,
			this.TABLES.size(),
			numWithShiftReduce,
			numWithReduceReduce
		));*/

		return states;
	}

	private Map<Set<ProductionRuleInstance>, State> generateParserStates(Grammar g, Map<NonterminalSymbol, ImmutableSet<ProductionRuleInstance>> productionRuleInstances) {
		Map<Set<ProductionRuleInstance>, State> states = new LinkedHashMap<>();

		Deque<State.Metadata> deque = new LinkedBlockingDeque<>();
		deque.offerLast(new State.Metadata(
			null,
			null,
			productionRuleInstances.get(g.getInitialNonterminalSymbol())
		));

		while (!deque.isEmpty()) {
			generateChildState(deque.pollFirst(), states, deque, g, productionRuleInstances);
		}

		return states;
	}

	private void generateChildState(State.Metadata metadata, Map<Set<ProductionRuleInstance>, State> states, Deque<State.Metadata> deque, Grammar g, Map<NonterminalSymbol, ImmutableSet<ProductionRuleInstance>> productionRuleInstances) {
		State parent = metadata.getParent();
		Symbol symbol = metadata.getSymbol();
		ImmutableSet<ProductionRuleInstance> kernelItems = metadata.getKernelItems();
		//State.Metadata metadata = deque.pollFirst();

		/*for (Entry<Set<ProductionRuleInstance>, State> entry : states.entrySet()) {
			if (kernelItems.containsAll(entry.getKey())) {
				System.out.println("avoided!");
				parent.putTransition(symbol, entry.getValue());
				return;
			}
		}*/

		State existingState = states.get(kernelItems);
		if (existingState != null) {
			parent.putTransition(symbol, existingState);
			return;
		}

		Set<ProductionRuleInstance> closureItems = new HashSet<>();
		kernelItems.stream()
			.forEach(kernelItem -> closeOver(kernelItem, g, kernelItems, closureItems, productionRuleInstances));

		// TODO: change from list to set?
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

	private void closeOver(ProductionRuleInstance p, Grammar g, Set<ProductionRuleInstance> kernelItems, Set<ProductionRuleInstance> closureItems, Map<NonterminalSymbol, ImmutableSet<ProductionRuleInstance>> productionRuleInstances) {
		if (!p.hasNext()) {
			return;
		}

		Symbol lookahead = p.peekNextSymbol();
		// TODO: verify this works, there may have been type erasure, can still verify using symbol id
		if (lookahead instanceof NonterminalSymbol) {
			productionRuleInstances.get(lookahead).stream()
				.filter(closureItem -> !(kernelItems.contains(closureItem) || closureItems.contains(closureItem)))
				.forEach(closureItem -> {
					closureItems.add(closureItem);
					closeOver(closureItem, g, kernelItems, closureItems, productionRuleInstances);
				});
		}
	}

	private ImmutableSet<ProductionRuleInstance> createProductionRuleInstances(Set<ProductionRule> productionRules) {;
		return ImmutableSet.copyOf(
			productionRules.stream()
				.map(productionRule -> productionRule.createInstance())
				.iterator()
		);
	}

	public static void outputTables(Grammar g, Map<Set<ProductionRuleInstance>, State> states) {
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(".", "output", g.getName() + ".tables"), Charset.forName("US-ASCII"), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
			for (State state : states.values()) {
				writer.write(String.format("State %d: V%s", state.getId(), state.getViablePrefixes()));
				if (state.getParent() != null) {
					writer.write(String.format(" = goto(%d, %s)",
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

	private static void writeProduction(BufferedWriter writer, Grammar g, State s, ProductionRuleInstance p, boolean isKernelItem) throws IOException {
		if (!p.hasNext()) {
			writer.write(String.format("%s\t%-32s reduce(%d)%n",
				isKernelItem ? "I:" : "",
				p,
				g.getProductionRules().indexOf(p.getAncestor())
			));

			return;
		}

		Symbol onSymbol = p.peekNextSymbol();
		writer.write(String.format("%s\t%-32s goto(A%d, %s)",
			isKernelItem ? "I:" : "",
			p,
			s.getTransitionFor(onSymbol).getId(),
			onSymbol
		));

		if (!(onSymbol instanceof NonterminalSymbol)) {
			writer.write(String.format("\tshift"));
		}

		writer.write(String.format("%n"));
	}
}
