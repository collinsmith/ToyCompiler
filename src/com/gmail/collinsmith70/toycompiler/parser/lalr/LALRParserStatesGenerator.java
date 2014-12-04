package com.gmail.collinsmith70.toycompiler.parser.lalr;

import com.gmail.collinsmith70.toycompiler.parser.Grammar;
import com.gmail.collinsmith70.toycompiler.parser.LAProductionRuleInstance;
import com.gmail.collinsmith70.toycompiler.parser.NonterminalSymbol;
import com.gmail.collinsmith70.toycompiler.parser.ProductionRule;
import com.gmail.collinsmith70.toycompiler.parser.ProductionRuleInstance;
import com.gmail.collinsmith70.toycompiler.parser.State;
import com.gmail.collinsmith70.toycompiler.parser.Symbol;
import com.gmail.collinsmith70.toycompiler.parser.TerminalSymbol;
import com.google.common.base.Preconditions;
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
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.LinkedBlockingDeque;

public class LALRParserStatesGenerator {
	public LALRParserStatesGenerator() {
		//...
	}

	public Map<Set<ProductionRuleInstance>, State<LAProductionRuleInstance>> generateParserTables(Grammar g) {
		Preconditions.checkNotNull(g);

		long dt;
		/*g.getLogger().info("Creating production rule instances...");
		dt = System.currentTimeMillis();
		BiMap<NonterminalSymbol, ImmutableSet<ProductionRuleInstance>> productionRuleInstances = HashBiMap.create();
		g.getProductionRulesMap().entrySet().stream()
			.forEachOrdered(entry -> productionRuleInstances.put(entry.getKey(), createProductionRuleInstances(entry.getValue())));
		ImmutableBiMap<NonterminalSymbol, ImmutableSet<ProductionRuleInstance>> immutableProductionRuleInstances = ImmutableBiMap.copyOf(productionRuleInstances);
		g.getLogger().info(String.format("Production rule instances generated in %dms; %d production rules",
			System.currentTimeMillis()-dt,
			immutableProductionRuleInstances.size()
		));*/

		g.getLogger().info("Computing FIRST(1) sets...");
		dt = System.currentTimeMillis();
		final Map<NonterminalSymbol, Set<TerminalSymbol>> FIRST = computeFirstSets(g);
		g.getLogger().info(String.format("FIRST(1) sets computed in %dms",
			System.currentTimeMillis()-dt
		));

		outputFirstSets(g, FIRST);

		g.getLogger().info("Generating parser states...");
		dt = System.currentTimeMillis();
		Map<Set<ProductionRuleInstance>, State<LAProductionRuleInstance>> states = generateParserStates(g, FIRST);
		g.getLogger().info(String.format("Parser states generated in %dms; %d states",
			System.currentTimeMillis()-dt,
			states.size()
		));

		return states;
	}

	private ImmutableSet<ProductionRuleInstance> createProductionRuleInstances(Set<ProductionRule> productionRules) {
		return ImmutableSet.copyOf(
			productionRules.stream()
				.map(productionRule -> new ProductionRuleInstance(productionRule))
				.iterator()
		);
	}

	private ImmutableSet<LAProductionRuleInstance> createLAProductionRuleInstance(Set<ProductionRuleInstance> productionRuleInstances, Set<TerminalSymbol> followSet) {
		return ImmutableSet.copyOf(
			productionRuleInstances.stream()
				.map(productionRuleInstance -> new LAProductionRuleInstance(productionRuleInstance, followSet))
				.iterator()
		);
	}

	private Map<NonterminalSymbol, Set<TerminalSymbol>> computeFirstSets(Grammar g) {
		Map<NonterminalSymbol, Set<TerminalSymbol>> first = new HashMap<>(g.getNumTerminalSymbols()+g.getNumNonterminalSymbols());
		for (ProductionRule p : g.getProductionRules()) {
			//System.out.println("FIRST for " + p.toString(g.getSymbolsTable()));
			first.put(p.getNonterminalSymbol(), computeFirstSet(p, g, first.getOrDefault(p.getNonterminalSymbol(), new HashSet<>()), new HashSet<>()));
		}

		return ImmutableMap.copyOf(first);
	}

	private Set<TerminalSymbol> computeFirstSet(ProductionRule p, Grammar g, Set<TerminalSymbol> firstSet, Set<ProductionRule> checked) {
		Symbol first = p.getRHS().get(0);
		if (checked.contains(p)) {
			//System.out.println("ignoring containing " + p.toString(g.getSymbolsTable()));
			return firstSet;
		}

		checked.add(p);
		if (first instanceof TerminalSymbol) {
			firstSet.add((TerminalSymbol)first);
			//System.out.println("\t+" + g.getSymbolsTable().get(first));
		} else {
			for (ProductionRule child : g.getProductionRulesMap().get((NonterminalSymbol)first)) {
				computeFirstSet(child, g, firstSet, checked);
			}
		}

		return firstSet;
	}

	private Map<Set<ProductionRuleInstance>, State<LAProductionRuleInstance>> generateParserStates(
		Grammar g,
		Map<NonterminalSymbol, Set<TerminalSymbol>> FIRST
	) {
		Map<Set<ProductionRuleInstance>, State<LAProductionRuleInstance>> states = new LinkedHashMap<>();

		ImmutableSet<ProductionRule> initialProductionRules = g.getProductionRulesMap().get(g.getInitialNonterminalSymbol());
		ImmutableSet<ProductionRuleInstance> initialProductionRuleInstances = createProductionRuleInstances(initialProductionRules);
		ImmutableSet<LAProductionRuleInstance> initialLAProductionRuleInstances = createLAProductionRuleInstance(initialProductionRuleInstances, new HashSet<>());
		Map<ProductionRuleInstance, LAProductionRuleInstance> kernelItems = new LinkedHashMap<>();
		for (LAProductionRuleInstance p : initialLAProductionRuleInstances) {
			p.addFollowSymbol(TerminalSymbol.EMPTY_STRING);
			kernelItems.put(p.getInstance(), p);
		}

		initialProductionRules = null;
		initialProductionRuleInstances = null;
		initialLAProductionRuleInstances = null;

		Deque<State.Metadata<ProductionRuleInstance, LAProductionRuleInstance>> deque = new LinkedBlockingDeque<>();
		deque.offerLast(State.firstMetadata(ImmutableMap.copyOf(kernelItems)));
		kernelItems = null;

		while (!deque.isEmpty()) {
			generateChildState(deque.pollFirst(), states, deque, g, FIRST);
		}

		return states;
	}

	private void generateChildState(
		State.Metadata<ProductionRuleInstance, LAProductionRuleInstance> metadata,
		Map<Set<ProductionRuleInstance>, State<LAProductionRuleInstance>> states,
		Deque<State.Metadata<ProductionRuleInstance, LAProductionRuleInstance>> deque,
		Grammar grammar,
		Map<NonterminalSymbol, Set<TerminalSymbol>> FIRST
	) {
		State<LAProductionRuleInstance> parent = metadata.getParent();
		ImmutableMap<ProductionRuleInstance, LAProductionRuleInstance> kernelItems = metadata.getKernelItems();
		Symbol currentSymbol = metadata.getCurrent();

		State<LAProductionRuleInstance> existingState = states.get(kernelItems.keySet());//TODO: tagged
		if (existingState != null) {
			parent.putTransition(currentSymbol, existingState);
			for (LAProductionRuleInstance existingProductionRuleInstance : kernelItems.values()) {
				existingState.getKernelItems()
					.get(existingProductionRuleInstance.getInstance())
					.addAllFollowSymbols(existingProductionRuleInstance.getFollowSet());
			}

			return;
		}

		//System.out.format("State %d ---------------------------------------%n", states.size());
		Map<ProductionRuleInstance, LAProductionRuleInstance> closureItems = new HashMap<>();
		kernelItems.values().stream()
			.forEachOrdered(kernelItem -> closeOver(kernelItem, grammar, kernelItems, closureItems, FIRST, kernelItem.getFollowSet(), new HashSet<>()));

		ImmutableList<Symbol> viablePrefix;
		if (parent != null) {
			viablePrefix = ImmutableList.<Symbol>builder()
				.addAll(parent.getViablePrefix())
				.add(currentSymbol)
				.build();
		} else {
			viablePrefix = ImmutableList.of();
		}

		int stateId = states.size();
		State<LAProductionRuleInstance> state = new State<>(
			stateId,
			parent,
			viablePrefix,
			kernelItems,
			ImmutableMap.copyOf(closureItems));

		states.put(kernelItems.keySet(), state);
		if (parent != null) {
			// TODO: is this needed? putting also occurs near existingState
			parent.putTransition(currentSymbol, state);
		}

		Deque<LAProductionRuleInstance> remainingProductions = new LinkedBlockingDeque<>();
		remainingProductions.addAll(kernelItems.values());
		remainingProductions.addAll(closureItems.values());

		LAProductionRuleInstance p;
		Set<LAProductionRuleInstance> reductions = new HashSet<>();
		while (!remainingProductions.isEmpty()) {
			p = remainingProductions.pollFirst();
			if (p.hasNext()) {
				Symbol lookahead = p.peekNextSymbol();
				Set<LAProductionRuleInstance> productionsWithSameLookahead = new HashSet<>();
				remainingProductions.stream()
					.filter(sibling -> lookahead.equals(sibling.peekNextSymbol()))
					.forEachOrdered(sibling -> productionsWithSameLookahead.add(sibling));

				remainingProductions.removeAll(productionsWithSameLookahead);
				productionsWithSameLookahead.add(p);

				Map<ProductionRuleInstance, LAProductionRuleInstance> nextKernelItems = new HashMap<>();
				productionsWithSameLookahead.stream()
					.map(production -> production.next())
					.forEachOrdered(production -> nextKernelItems.put(production.getInstance(), production));

				State.Metadata<ProductionRuleInstance, LAProductionRuleInstance> childMetadata = state.createMetadata(ImmutableMap.copyOf(nextKernelItems));
				deque.offerLast(childMetadata);
			} else {
				reductions.add(p);
			}
		}

		if (1 < reductions.size()) {
			//numWithReduceReduce++;
			grammar.getLogger().severe(String.format("State %d has %d reduce productions:", stateId, reductions.size()));
			//reductions.stream()
			//	.forEachOrdered(reduceItem -> grammar.getLogger().warning(String.format("\t%s", reduceItem.toString(grammar.getSymbolsTable()))));
		}
	}

	private void closeOver(
		LAProductionRuleInstance p,
		Grammar grammar,
		Map<ProductionRuleInstance, LAProductionRuleInstance> kernelItems,
		Map<ProductionRuleInstance, LAProductionRuleInstance> closureItems,
		Map<NonterminalSymbol, Set<TerminalSymbol>> FIRST,
		Set<TerminalSymbol> follow,
		Set<ProductionRuleInstance> closureExisted
	) {
		Symbol l1 = p.lookahead(1);
		if (l1 == null) {
			// This production rule is a reduction
			return;
		}

		if (!(l1 instanceof NonterminalSymbol)) {
			// This production rule has been closed over
			return;
		}

		Symbol l2 = p.lookahead(2);
		if (l2 == null) {
			follow = new HashSet<>(follow);
		} else {
			follow = new HashSet<>();
			if (l2 instanceof TerminalSymbol) {
				follow.add((TerminalSymbol)l2);
			} else {
				follow.addAll(FIRST.get((NonterminalSymbol)l2));
			}
		}

		LAProductionRuleInstance temp;
		Set<ProductionRuleInstance> children = createProductionRuleInstances(grammar.getProductionRulesMap().get((NonterminalSymbol)l1));
		for (ProductionRuleInstance child : children) {
			//temp = kernelItems.get(child);

			temp = closureItems.get(child);
			if (temp != null) {
				//System.out.println("closure exists: " + temp.toString(grammar.getSymbolsTable()) + "; follow: " + temp.getFollowSet());
				// A matching items exists within generated closures
				if (temp.addAllFollowSymbols(follow)) {
					//System.out.println("\tupdate follow: " + temp.getFollowSet());
					// This production rule is producing new follow terminals
					temp = new LAProductionRuleInstance(child, follow);
					closeOver(temp, grammar, kernelItems, closureItems, FIRST, temp.getFollowSet(), closureExisted);
				} else if (closureExisted.add(child)) {
					// No new terminals could be added to the follow set, we've already closed over this production rule
					closeOver(temp, grammar, kernelItems, closureItems, FIRST, temp.getFollowSet(), closureExisted);
				}

				continue;
			}

			temp = new LAProductionRuleInstance(child, follow);
			assert closureItems.get(child) == null;
			closureItems.put(child, temp);
			closeOver(temp, grammar, kernelItems, closureItems, FIRST, temp.getFollowSet(), closureExisted);
		}
	}

	private void closeOver2(
		LAProductionRuleInstance p,
		Grammar grammar,
		Map<ProductionRuleInstance, LAProductionRuleInstance> kernelItems,
		Map<ProductionRuleInstance, LAProductionRuleInstance> closureItems,
		Map<NonterminalSymbol, Set<TerminalSymbol>> FIRST
	) {
		System.out.println(p.toString(grammar.getSymbolsTable()));
		Symbol l1 = p.lookahead(1);
		if (l1 == null) {
			System.out.println("\treduction, skipping " + p.toString(grammar.getSymbolsTable()));
			return;
		}

		if (!(l1 instanceof NonterminalSymbol)) {
			System.out.println("\tterminal symbol, nothing to close over, skipping " + p.toString(grammar.getSymbolsTable()) + "; FOLLOW = " + p.getFollowSet());
			return;
		}

		Symbol l2 = p.lookahead(2);
		Set<TerminalSymbol> childFollowSet;
		if (l2 == null) {
			// TODO: which?
			childFollowSet = new HashSet<>(p.getFollowSet());
			//childFollowSet = p.getFollowSet();
		} else {
			childFollowSet = new HashSet<>();
			if (l2 instanceof TerminalSymbol) {
				childFollowSet.add((TerminalSymbol)l2);
			} else {
				childFollowSet.addAll(FIRST.get((NonterminalSymbol)l2));
			}
		}

		LAProductionRuleInstance temp, map;
		Set<ProductionRuleInstance> children = createProductionRuleInstances(grammar.getProductionRulesMap().get((NonterminalSymbol)l1));
		for (ProductionRuleInstance child : children) {
			temp = kernelItems.get(child);
			if (temp != null) {
				// TODO: maybe check if added, if same follow,...
				temp.addAllFollowSymbols(childFollowSet);
				System.out.println("\tkernel exists, skipping " + child.toString(grammar.getSymbolsTable()));
				continue;
			}

			temp = closureItems.get(child);
			if (temp != null) {
				System.out.println("\texisting closure = " + temp.toString(grammar.getSymbolsTable()) + "; current followset = " + temp.getFollowSet());
				System.out.print("\t\tadd " + childFollowSet + " into " + temp.getFollowSet());
				if (temp.addAllFollowSymbols(childFollowSet)) {
					System.out.println("->" + temp.getFollowSet());
				} else {
					System.out.println("->NO CHANGE: " + temp.getFollowSet());
					System.out.println("\t\t" + temp.toString(grammar.getSymbolsTable()) + " follow = " + temp.getFollowSet());
					continue;
				}
			}

			// TODO: childFollowSet reference or copy?
			System.out.println("\texisting at " + child.toString(grammar.getSymbolsTable()) + " = " + (closureItems.get(child) == null ? "null" : closureItems.get(child).toString(grammar.getSymbolsTable())));
			temp = closureItems.get(child);
			if (temp != null) {
				temp.addAllFollowSymbols(childFollowSet);
			} else {
				temp = new LAProductionRuleInstance(child, childFollowSet);
				closureItems.put(child, temp);
				System.out.println("\tputting " + temp.toString(grammar.getSymbolsTable())+ " at " + child.toString(grammar.getSymbolsTable()) + "; FOLLOW = " + temp.getFollowSet());
			}

			closeOver2(temp, grammar, kernelItems, closureItems, FIRST);
		}
	}

	public static void outputStates(
		Grammar g,
		Map<Set<ProductionRuleInstance>, State<LAProductionRuleInstance>> states
	) {
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(".", "output", g.getName() + ".lalr.tables"), Charset.forName("US-ASCII"), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
			for (State<LAProductionRuleInstance> state : states.values()) {
				writer.write(String.format("State %d: V%s", state.getId(), ImmutableList.copyOf(state.getViablePrefix().stream().map(symbol -> g.getSymbolsTable().get(symbol)).iterator())));
				if (state.getParent() != null) {
					writer.write(String.format(" = goto(S%d, %s)",
						state.getParent().getId(),
						state.getViablePrefix().get(state.getViablePrefix().size()-1)
					));
				}

				writer.write(String.format("%n"));

				for (LAProductionRuleInstance p : state.getKernelItems().values()) {
					writeProduction(writer, g, state, p, true);
				}

				for (LAProductionRuleInstance p : state.getClosureItems().values()) {
					writeProduction(writer, g, state, p, false);
				}

				writer.write(String.format("%n"));
				writer.flush();
			}
		} catch (IOException e) {
			g.getLogger().throwing(LALRParserStatesGenerator.class.toString(), "outputStates()", e);
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
				p.toString(g.getSymbolsTable()),
				p.getInstance().getProductionRule().getId()
			));

			return;
		}

		StringJoiner sj = new StringJoiner(",", "FOLLOW={", "}");
		for (Symbol followSymbol : p.getFollowSet()) {
			sj.add(g.getSymbolsTable().get(followSymbol));
		}

		Symbol lookahead = p.peekNextSymbol();
		writer.write(String.format("%-4s %-48s %-32s %-32s %s%n",
			isKernelItem ? "I:" : "",
			p.toString(g.getSymbolsTable()),
			sj.toString(),
			String.format("goto(S%d, %s)",
				s.getTransition(lookahead).getId(),
				g.getSymbolsTable().get(lookahead)
			),
			(lookahead instanceof NonterminalSymbol) ? "" : "shift"
		));
	}

	private void outputFirstSets(Grammar g, Map<NonterminalSymbol, Set<TerminalSymbol>> FIRST) {
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(".", "output", g.getName() + ".firstsets"), Charset.forName("US-ASCII"), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
			for (NonterminalSymbol s : FIRST.keySet()) {
				StringJoiner sj = new StringJoiner(", ", "{ ", " }");
				for (Symbol symbol : FIRST.get(s)) {
					sj.add(g.getSymbolsTable().get(symbol));
				}

				writer.write(String.format("FIRST(%s) = %s%n", g.getSymbolsTable().get(s), sj.toString()));
				writer.flush();
			}
		} catch (IOException e) {
			g.getLogger().throwing(LALRParserStatesGenerator.class.toString(), "outputFirstSets()", e);
		}
	}
}
