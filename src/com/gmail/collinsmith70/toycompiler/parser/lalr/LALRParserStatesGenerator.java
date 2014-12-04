package com.gmail.collinsmith70.toycompiler.parser.lalr;

import com.gmail.collinsmith70.toycompiler.parser.Grammar;
import com.gmail.collinsmith70.toycompiler.parser.LAProductionRuleInstance;
import com.gmail.collinsmith70.toycompiler.parser.LRParserTables;
import com.gmail.collinsmith70.toycompiler.parser.NonterminalSymbol;
import com.gmail.collinsmith70.toycompiler.parser.ProductionRule;
import com.gmail.collinsmith70.toycompiler.parser.ProductionRuleInstance;
import com.gmail.collinsmith70.toycompiler.parser.ReduceReduceConflict;
import com.gmail.collinsmith70.toycompiler.parser.ShiftReduceConflict;
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
import java.util.ArrayList;
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
				// A matching items exists within generated closures
				if (temp.addAllFollowSymbols(follow)) {
					// This production rule is producing new follow
					// terminals
					temp = new LAProductionRuleInstance(child, follow);
					closeOver(temp, grammar, kernelItems, closureItems, FIRST, temp.getFollowSet(), closureExisted);
				} else if (closureExisted.add(child)) {
					// No new terminals could be added to the follow
					// set, we've already closed over this production
					// rule, but children must be updated with the
					// new follow set, so close over one last time
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

	public static LRParserTables compile(Grammar g, Map<Set<ProductionRuleInstance>, State<LAProductionRuleInstance>> states) {
		int numTables = states.size();

		int[] reduceTable = new int[numTables];
		int[] shiftSwitchTable = new int[numTables];
		int[] gotoSwitchTable = new int[numTables];

		ArrayList<Integer[]> gotoTableList = new ArrayList<>();
		ArrayList<Integer[]> shiftTableList = new ArrayList<>();
		final Integer[] EMPTY_ELEMENT = new Integer[] { Integer.MIN_VALUE, Integer.MIN_VALUE };

		Map<Set<ProductionRuleInstance>, ShiftReduceConflict> shiftReduceConflictsMap = new HashMap<>();
		Map<Set<ProductionRuleInstance>, ReduceReduceConflict> reduceReduceConflictsMap = new HashMap<>();

		int stateId;
		Symbol nextSymbol;
		Symbol currentSymbol = null;
		State<LAProductionRuleInstance> nextState = null;
		for (State<LAProductionRuleInstance> s : states.values()) {
			stateId = s.getId();
			reduceTable[stateId] = Integer.MIN_VALUE;
			shiftSwitchTable[stateId] = Integer.MIN_VALUE;
			gotoSwitchTable[stateId] = Integer.MIN_VALUE;

			for (LAProductionRuleInstance p : s) {
				if (!p.hasNext()) {
					if (reduceTable[stateId] == Integer.MIN_VALUE) {
						reduceTable[stateId] = p.getInstance().getProductionRule().getId();
						currentSymbol = p.currentSymbol();
					} else {
						reduceReduceConflictsMap.putIfAbsent(s.getKernelItems().keySet(), new ReduceReduceConflict(s));
					}

					continue;
				}

				nextSymbol = p.peekNextSymbol();
				nextState = s.getTransition(nextSymbol);
				if (nextSymbol instanceof NonterminalSymbol) {
					if (gotoSwitchTable[stateId] == Integer.MIN_VALUE) {
						gotoSwitchTable[stateId] = gotoTableList.size();
					}

					gotoTableList.add(new Integer[] { nextSymbol.getId(), nextState.getId() });
				} else {
					if (shiftSwitchTable[stateId] == Integer.MIN_VALUE) {
						shiftSwitchTable[stateId] = shiftTableList.size();
					}

					shiftTableList.add(new Integer[] { nextSymbol.getId(), nextState.getId() });
				}
			}

			if (reduceTable[stateId] != Integer.MIN_VALUE && shiftSwitchTable[stateId] != Integer.MIN_VALUE) {
				shiftReduceConflictsMap.putIfAbsent(s.getKernelItems().keySet(), new ShiftReduceConflict(s));
			}

			if (shiftSwitchTable[stateId] != Integer.MIN_VALUE) {
				shiftTableList.add(EMPTY_ELEMENT);
			}

			if (gotoSwitchTable[stateId] != Integer.MIN_VALUE) {
				gotoTableList.add(EMPTY_ELEMENT);
			}
		}

		int[][] shiftTable = new int[shiftTableList.size()][2];
		for (int i = 0; i < shiftTable.length; i++) {
			shiftTable[i][LRParserTables.SYM] = shiftTableList.get(i)[LRParserTables.SYM];
			shiftTable[i][LRParserTables.NXT] = shiftTableList.get(i)[LRParserTables.NXT];
		}

		int[][] gotoTable = new int[gotoTableList.size()][2];
		for (int i = 0; i < gotoTable.length; i++) {
			gotoTable[i][LRParserTables.SYM] = gotoTableList.get(i)[LRParserTables.SYM];
			gotoTable[i][LRParserTables.NXT] = gotoTableList.get(i)[LRParserTables.NXT];
		}

		int productionId;
		int numProduction = g.getProductionRules().size();
		int[] nonterminalIdTable = new int[numProduction];
		int[] rhsSizeTable = new int[numProduction];
		for (ProductionRule p : g.getProductionRules()) {
			productionId = p.getId();
			nonterminalIdTable[productionId] = p.getNonterminalSymbol().getId();
			rhsSizeTable[productionId] = p.getRHS().size();
		}

		int[] shiftReduceConflicts = shiftReduceConflictsMap.values().stream()
			.mapToInt(conflict -> conflict.getState().getId())
			.toArray();

		int[] reduceReduceConflicts = reduceReduceConflictsMap.values().stream()
			.mapToInt(conflict -> conflict.getState().getId())
			.toArray();

		return new LRParserTables(
			new LRParserTables.ShiftTable(shiftSwitchTable, shiftTable),
			new LRParserTables.ReduceTable(reduceTable),
			new LRParserTables.GotoTable(gotoSwitchTable, gotoTable),
			new LRParserTables.ProductionRulesTable(nonterminalIdTable, rhsSizeTable),
			new LRParserTables.ShiftReduceConflictsTable(shiftReduceConflicts),
			new LRParserTables.ReduceReduceConflictsTable(reduceReduceConflicts));
	}
}
