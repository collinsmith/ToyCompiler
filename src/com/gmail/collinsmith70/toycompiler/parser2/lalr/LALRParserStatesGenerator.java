package com.gmail.collinsmith70.toycompiler.parser2.lalr;

import com.gmail.collinsmith70.toycompiler.parser2.Grammar;
import com.gmail.collinsmith70.toycompiler.parser2.NonterminalSymbol;
import com.gmail.collinsmith70.toycompiler.parser2.ProductionRule;
import com.gmail.collinsmith70.toycompiler.parser2.ProductionRuleInstance;
import com.gmail.collinsmith70.toycompiler.parser2.State;
import com.gmail.collinsmith70.toycompiler.parser2.Symbol;
import com.gmail.collinsmith70.toycompiler.parser2.TerminalSymbol;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.LinkedBlockingDeque;

public class LALRParserStatesGenerator {
	public LALRParserStatesGenerator() {
		//...
	}

	public Map<Set<LAProductionRuleInstance>, State<LAProductionRuleInstance>> generateParserTables(Grammar g) {
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

		g.getLogger().info("Computing FIRST(1) sets...");
		dt = System.currentTimeMillis();
		final Map<NonterminalSymbol, Set<TerminalSymbol>> FIRST = computeFirstSets(g);
		g.getLogger().info(String.format("FIRST(1) sets computed in %dms",
			System.currentTimeMillis()-dt
		));

		g.getLogger().info("Generating parser states...");
		dt = System.currentTimeMillis();
		Map<Set<LAProductionRuleInstance>, State<LAProductionRuleInstance>> states = generateParserStates(g, immutableProductionRuleInstances, FIRST);
		g.getLogger().info(String.format("Parser states generated in %dms; %d states",
			System.currentTimeMillis()-dt,
			states.size()
		));

		return states;
	}

	private Map<NonterminalSymbol, Set<TerminalSymbol>> computeFirstSets(Grammar g) {
		Map<NonterminalSymbol, Set<TerminalSymbol>> first = new HashMap<>(g.getNumTerminalSymbols()+g.getNumNonterminalSymbols());
		for (ProductionRule p : g.getProductionRules()) {
			first.put(p.getNonterminalSymbol(), generateFirstSet(p, g, new HashSet<>(), new HashSet<>()));
		}

		return ImmutableMap.copyOf(first);
	}

	private Set<TerminalSymbol> generateFirstSet(ProductionRule p, Grammar g, Set<TerminalSymbol> firstSet, Set<ProductionRule> checked) {
		Symbol first = p.getRHS().get(0);
		if (checked.contains(p)) {
			return firstSet;
		}

		checked.add(p);
		if (first instanceof TerminalSymbol) {
			firstSet.add((TerminalSymbol)first);
		} else {
			for (ProductionRule child : g.getProductionRulesMap().get((NonterminalSymbol)first)) {
				generateFirstSet(child, g, firstSet, checked);
			}
		}

		return firstSet;
	}

	private ImmutableSet<ProductionRuleInstance> createProductionRuleInstances(Set<ProductionRule> productionRules) {
		return ImmutableSet.copyOf(
			productionRules.stream()
				.map(productionRule -> new ProductionRuleInstance(productionRule))
				.iterator()
		);
	}

	private ImmutableSet<LAProductionRuleInstance> createLAProductionRuleInstances(Set<ProductionRuleInstance> productionRuleInstances) {
		return ImmutableSet.copyOf(
			productionRuleInstances.stream()
				.map(productionRuleInstance -> new LAProductionRuleInstance(productionRuleInstance))
				.iterator()
		);
	}

	protected Map<Set<LAProductionRuleInstance>, State<LAProductionRuleInstance>> generateParserStates(
		Grammar g,
		Map<NonterminalSymbol, ImmutableSet<ProductionRuleInstance>> productionRuleInstances,
		Map<NonterminalSymbol, Set<TerminalSymbol>> FIRST
	) {
		Map<Set<LAProductionRuleInstance>, State<LAProductionRuleInstance>> states = new LinkedHashMap<>();

		ImmutableSet<ProductionRuleInstance> initialProductionRuleInstances = productionRuleInstances.get(g.getInitialNonterminalSymbol());
		ImmutableSet<LAProductionRuleInstance> initialLAProductionRuleInstances = createLAProductionRuleInstances(initialProductionRuleInstances);
		for (LAProductionRuleInstance p : initialLAProductionRuleInstances) {
			p.getFollowSet().add(TerminalSymbol.EMPTY_STRING);
		}

		Deque<State.Metadata<LAProductionRuleInstance>> deque = new LinkedBlockingDeque<>();
		deque.offerLast(new State.Metadata<>(
			null,
			null,
			initialLAProductionRuleInstances
		));

		while (!deque.isEmpty()) {
			generateChildState(deque.pollFirst(), states, deque, g, productionRuleInstances, FIRST);
		}

		return states;
	}

	protected void generateChildState(
		State.Metadata<LAProductionRuleInstance> metadata,
		Map<Set<LAProductionRuleInstance>, State<LAProductionRuleInstance>> states,
		Deque<State.Metadata<LAProductionRuleInstance>> deque,
		Grammar g,
		Map<NonterminalSymbol, ImmutableSet<ProductionRuleInstance>> productionRuleInstances,
		Map<NonterminalSymbol, Set<TerminalSymbol>> FIRST
	) {
		State<LAProductionRuleInstance> parent = metadata.getParent();
		Symbol symbol = metadata.getSymbol();
		ImmutableSet<LAProductionRuleInstance> kernelItems = metadata.getKernelItems();

		State<LAProductionRuleInstance> existingState = states.get(kernelItems);
		if (existingState != null) {
			parent.putTransition(symbol, existingState);
			// TODO: replace this (n^2) with something better?
			for (LAProductionRuleInstance repeatedItem : kernelItems) {
				for (LAProductionRuleInstance existingItem : existingState.getKernelItems()) {
					if (repeatedItem.equals(existingItem)) {
						existingItem.getFollowSet().addAll(repeatedItem.getFollowSet());
						break;
					}
				}
			}

			return;
		}

		Set<LAProductionRuleInstance> closureItems = new HashSet<>();
		kernelItems.stream()
			.forEachOrdered(kernelItem -> closeOver(kernelItem, g, kernelItems, closureItems, productionRuleInstances, FIRST));

		List<Symbol> viablePrefixes;
		if (parent != null) {
			viablePrefixes = new LinkedList<>(parent.getViablePrefixes());
			viablePrefixes.add(symbol);
		} else {
			viablePrefixes = new LinkedList<>();
		}

		int stateId = states.size();
		State<LAProductionRuleInstance> state = new State<>(
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

		Deque<LAProductionRuleInstance> remainingProductions = new LinkedBlockingDeque<>(kernelItems);
		remainingProductions.addAll(closureItems);

		LAProductionRuleInstance p;
		Set<LAProductionRuleInstance> reductions = new HashSet();
		while (!remainingProductions.isEmpty()) {
			p = remainingProductions.pollFirst();
			if (p.getInstance().hasNext()) {
				Symbol lookahead = p.peekNextSymbol();
				Set<LAProductionRuleInstance> productionsWithSameLookahead = new HashSet<>();
				remainingProductions.stream()
					.filter(sibling -> sibling.peekNextSymbol().equals(lookahead))
					.forEachOrdered(sibling -> {
						productionsWithSameLookahead.add(sibling);
					});

				remainingProductions.removeAll(productionsWithSameLookahead);
				productionsWithSameLookahead.add(p);

				Set<LAProductionRuleInstance> nextKernelItems = new HashSet<>();
				productionsWithSameLookahead.stream()
					.forEachOrdered(production -> {
						//System.out.println(production);
						LAProductionRuleInstance p2 = production.getChild();
						//System.out.println(p2);
						nextKernelItems.add(p2);
					});

				State.Metadata<LAProductionRuleInstance> childMetadata = state.getChildMetadata(lookahead, ImmutableSet.copyOf(nextKernelItems));
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

	protected void closeOver(
		LAProductionRuleInstance p,
		Grammar g,
		Set<LAProductionRuleInstance> kernelItems,
		Set<LAProductionRuleInstance> closureItems,
		Map<NonterminalSymbol, ImmutableSet<ProductionRuleInstance>> productionRuleInstances,
		Map<NonterminalSymbol, Set<TerminalSymbol>> FIRST
	) {
		Symbol l1 = p.lookahead(1);
		if (l1 == null) {
			return;
		}

		if (!(l1 instanceof NonterminalSymbol)) {
			return;
		}

		Symbol l2 = p.lookahead(2);
		Set<Symbol> childFollowSet;
		if (l2 == null) {
			childFollowSet = p.getFollowSet();
		} else {
			childFollowSet = new HashSet<>();
			if (l2 instanceof TerminalSymbol) {
				childFollowSet.add(l2);
			} else {
				childFollowSet.addAll(FIRST.get(l2));
			}
		}

		// TODO: Implement MappedSet, which will increase performance greatly
		//LAProductionRuleInstance temp;
		Set<ProductionRuleInstance> children = productionRuleInstances.get(l1);
		nextChild: for (ProductionRuleInstance child : children) {
			if (kernelItems.contains(child)) {
				for (LAProductionRuleInstance temp : kernelItems) {
					if (temp.equals(child)) {
						temp.getFollowSet().addAll(childFollowSet);
						continue nextChild;
					}
				}
			}

			/*temp = kernelItems.get(child);
			if (temp != null) {
				temp.getFollowSet().addAll(childFollowSet);
				continue;
			}*/

			if (closureItems.contains(child)) {
				for (LAProductionRuleInstance temp : closureItems) {
					if (temp.equals(child)) {
						temp.getFollowSet().addAll(childFollowSet);
						continue nextChild;
					}
				}
			}

			/*temp = closureItems.get(child);
			if (temp != null) {
				temp.getFollowSet().addAll(childFollowSet);
				continue;
			}*/

			LAProductionRuleInstance temp = new LAProductionRuleInstance(child, p);
			temp.getFollowSet().addAll(childFollowSet);
			closureItems.add(temp);
			closeOver(temp.getChild(), g, kernelItems, closureItems, productionRuleInstances, FIRST);
		}
	}

	public static void outputStates(
		Grammar g,
		Map<Set<LAProductionRuleInstance>, State<LAProductionRuleInstance>> states
	) {
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(".", "output", g.getName() + ".lalr.tables"), Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
			for (State<LAProductionRuleInstance> state : states.values()) {
				writer.write(String.format("State %d: V%s", state.getId(), state.getViablePrefixes()));
				if (state.getParent() != null) {
					writer.write(String.format(" = goto(S%d, %s)",
						state.getParent().getId(),
						state.getViablePrefixes().get(state.getViablePrefixes().size()-1)
					));
				}

				writer.write(String.format("%n"));

				for (LAProductionRuleInstance p : state.getKernelItems()) {
					writeProduction(writer, g, state, p, true);
				}

				for (LAProductionRuleInstance p : state.getClosureItems()) {
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
		State s,
		LAProductionRuleInstance p,
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

		StringJoiner sj = new StringJoiner(",", "FIRST={", "}");
		for (Symbol followSymbol : p.getFollowSet()) {
			sj.add(g.getSymbolsTable().inverse().get(followSymbol));
		}

		Symbol lookahead = p.peekNextSymbol();
		writer.write(String.format("%-4s %-48s %-32s %-32s %s%n",
			isKernelItem ? "I:" : "",
			p.toString(g.getSymbolsTable().inverse()),
			sj.toString(),
			String.format("goto(S%d, %s)",
				s.getTransition(lookahead).getId(),
				g.getSymbolsTable().inverse().get(lookahead)
			),
			(lookahead instanceof NonterminalSymbol) ? "" : "shift"
		));
	}
}
