package com.gmail.collinsmith70.toycompiler.parser.slr;

import com.gmail.collinsmith70.toycompiler.parser.AbstractParserGenerator;
import com.gmail.collinsmith70.toycompiler.parser.Production;
import com.gmail.collinsmith70.toycompiler.parser.Table;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

public class SLRParserGenerator extends AbstractParserGenerator {
	protected final SLRTables SLR_TABLES;
	protected final Map<Set<Production>, Table> TABLES;

	private int numWithShiftReduce;
	private int numWithReduceReduce;
	private int numUnrepeatedTables;

	public SLRParserGenerator(Path p) throws IOException {
		super(p);

		numWithShiftReduce = Integer.MIN_VALUE;
		numWithReduceReduce = Integer.MIN_VALUE;
		numUnrepeatedTables = Integer.MIN_VALUE;

		getLogger().info("Generating tables...");
		long dt = System.currentTimeMillis();
		this.TABLES = generateParserTables();
		getLogger().info(String.format("Tables generated in %dms; %d tables",
			System.currentTimeMillis()-dt,
			this.TABLES.size()
		));

		dt = System.currentTimeMillis();
		getLogger().info("Generating SLR tables...");
		this.SLR_TABLES = generateSLRTables();
		getLogger().info(String.format("SLR tables generated in %dms; %d tables (%d shift-reduce conflicts, %d reduce-reduce conflicts)",
			System.currentTimeMillis()-dt,
			this.TABLES.size(),
			numWithShiftReduce,
			numWithReduceReduce
		));
	}

	private Map<Set<Production>, Table> generateParserTables() {
		Map<Set<Production>, Table> tables = new LinkedHashMap<>();

		numWithReduceReduce = numUnrepeatedTables = 0;

		Queue<Table.Metadata> queue = new LinkedList<>();
		do {
			generateTable(tables, queue);
		} while (!queue.isEmpty());


		return tables;
	}

	private void generateTable(Map<Set<Production>, Table> tables, Queue<Table.Metadata> queue) {
		Table parent;
		Integer symbol;
		ImmutableSet<Production> productions;
		Table.Metadata metadata = queue.poll();
		if (metadata == null) {
			parent = null;
			symbol = null;
			productions = NONTERMINALS.get(super.getInitialNonterminal());
		} else {
			parent = metadata.getParent();
			symbol = metadata.getNextSymbol();
			productions = metadata.getNextInitialProductions();

			Table existingTable = tables.get(productions);
			if (existingTable != null) {
				parent.putTransition(symbol, existingTable);
				numUnrepeatedTables++;
				return;
			}
		}

		Set<Production> closures = new HashSet<>();
		productions.stream()
			.forEach((initialProduction) -> closeOverProduction(productions, closures, initialProduction));

		List<Integer> viablePrefix;
		if (parent != null) {
			viablePrefix = new LinkedList<>(parent.getViablePrefix());
			viablePrefix.add(symbol);
		} else {
			viablePrefix = new LinkedList<>();
		}

		int tableId = tables.size();
		Table t = new Table(
			tableId,
			parent,
			ImmutableList.copyOf(viablePrefix),
			productions,
			ImmutableSet.copyOf(closures)
		);

		tables.put(productions, t);
		if (parent != null) {
			parent.putTransition(symbol, t);
		}

		Queue<Production> remainingProductions = new LinkedList<>(productions);
		remainingProductions.addAll(closures);

		Production p;
		Set<Production> reduces = new HashSet<>();
		while (!remainingProductions.isEmpty()) {
			p = remainingProductions.poll();
			if (p.hasNext()) {
				Integer nextSymbol = p.peek();
				Set<Production> productionsWithSameNextSymbol = new HashSet<>();
				remainingProductions.stream()
					.filter((sibling) -> (sibling.hasNext() && Objects.equals(sibling.peek(), nextSymbol))).forEach((sibling) -> {
						productionsWithSameNextSymbol.add(sibling);
					});

				remainingProductions.removeAll(productionsWithSameNextSymbol);
				productionsWithSameNextSymbol.add(p);

				Set<Production> nextInitialProductions = new HashSet<>();
				productionsWithSameNextSymbol.stream()
					.forEach((productionWithSameNextSymbol) -> nextInitialProductions.add(productionWithSameNextSymbol.next()));

				Table.Metadata nextTable = t.getMetadataForChild(nextSymbol, ImmutableSet.copyOf(nextInitialProductions));
				queue.offer(nextTable);
			} else {
				reduces.add(p);
			}
		}

		if (1 < reduces.size()) {
			numWithReduceReduce++;
			getLogger().warning(String.format("Table A%d has %d reduce productions:", tableId, reduces.size()));
			reduces.stream()
				.forEach((reduce) -> getLogger().warning(String.format("\t%s", reduce.toString(SYMBOLS.inverse()))));
		}
	}

	private void closeOverProduction(Set<Production> initials, Set<Production> closures, Production p) {
		if (!p.hasNext()) {
			return;
		}

		Integer nextSymbol = p.peek();
		if (isNonterminal(nextSymbol)) {
			NONTERMINALS.get(nextSymbol).stream()
				.filter((closure) -> !(initials.contains(closure) || closures.contains(closure))).map((closure) -> {
					closures.add(closure);
					return closure;
				}).forEach((closure) -> closeOverProduction(initials, closures, closure));
		}
	}

	public void outputTables() {
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(".", "output", "toy.tables.txt"), AbstractParserGenerator.CHARSET, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
			for (Table t : TABLES.values()) {
				writer.write(String.format("A%d: V%s", t.getId(), t.getViablePrefix()));
				if (t.getParent() != null) {
					writer.write(String.format(" = goto(A%d, %d)",
						t.getParent().getId(),
						t.getViablePrefix().get(t.getViablePrefix().size()-1)
					));
				}

				writer.write(String.format("%n"));

				for (Production p : t.getInitialProductions()) {
					writeProduction(writer, t, p, true);
				}

				for (Production p : t.getClosureProductions()) {
					writeProduction(writer, t, p, false);
				}

				writer.write(String.format("%n"));
				writer.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeProduction(BufferedWriter writer, Table t, Production p, boolean isInitialProduction) throws IOException {
		if (!p.hasNext()) {
			writer.write(String.format("%s\t%-32s reduce(%d)%n",
				isInitialProduction ? "I:" : "",
				p,
				PRODUCTIONS.indexOf(p.getAncestor())
			));

			return;
		}

		Integer onSymbol = p.peek();
		writer.write(String.format("%s\t%-32s goto(A%d, %s)",
			isInitialProduction ? "I:" : "",
			p,
			t.getTransitionFor(onSymbol).getId(),
			onSymbol
		));

		if (!isNonterminal(onSymbol)) {
			writer.write(String.format("\tshift"));
		}

		writer.write(String.format("%n"));
	}

	private SLRTables generateSLRTables() {
		int numTables = TABLES.size();

		int[] _reduce = new int[numTables];
		int[] gotoSwitch = new int[numTables];
		int[] shiftSwitch = new int[numTables];

		ArrayList<Integer[]> gotoList = new ArrayList<>();
		ArrayList<Integer[]> shiftList = new ArrayList<>();
		final Integer[] EMPTY_ELEMENT = new Integer[] { Integer.MIN_VALUE, Integer.MIN_VALUE };

		numWithShiftReduce = 0;
		Integer currentSymbol = null;

		int tableId;
		Table nextTable = null;
		Integer nextSymbol = null;
		for (Table t : TABLES.values()) {
			tableId = t.getId();
			_reduce[tableId] = Integer.MIN_VALUE;
			gotoSwitch[tableId] = Integer.MIN_VALUE;
			shiftSwitch[tableId] = Integer.MIN_VALUE;
			for (Production p : t) {
				if (!p.hasNext()) {
					if (_reduce[tableId] == Integer.MIN_VALUE) {
						_reduce[tableId] = PRODUCTIONS.indexOf(p.getAncestor());
						currentSymbol = p.current();
					} else {
						// reduce-reduce conflict indicated in previous stage
					}

					continue;
				}

				nextSymbol = p.peek();
				nextTable = t.getTransitionFor(nextSymbol);
				if (!isNonterminal(nextSymbol)) {
					if (shiftSwitch[tableId] == Integer.MIN_VALUE) {
						shiftSwitch[tableId] = shiftList.size();
					}

					shiftList.add(new Integer[] { nextSymbol, nextTable.getId() });
				} else {
					if (gotoSwitch[tableId] == Integer.MIN_VALUE) {
						gotoSwitch[tableId] = gotoList.size();
					}

					gotoList.add(new Integer[] { nextSymbol, nextTable.getId() });
				}
			}

			if (_reduce[tableId] != Integer.MIN_VALUE && shiftSwitch[tableId] != Integer.MIN_VALUE) {
				numWithShiftReduce++;
				System.out.format("Table A%d has a shift-reduce conflict:%n", tableId);
				for (Production p : t) {
					if (Objects.equals(p.current(), currentSymbol)) {
						System.out.format("\t%s%n", p.toString(SYMBOLS.inverse()));
					}
				}
			}

			if (shiftSwitch[tableId] != Integer.MIN_VALUE) {
				shiftList.add(EMPTY_ELEMENT);
			}

			if (gotoSwitch[tableId] != Integer.MIN_VALUE) {
				gotoList.add(EMPTY_ELEMENT);
			}
		}

		int[][] _shift = new int[shiftList.size()][2];
		for (int i = 0; i < _shift.length; i++) {
			_shift[i][SLRTables.SYM] = shiftList.get(i)[SLRTables.SYM];
			_shift[i][SLRTables.NXT] = shiftList.get(i)[SLRTables.NXT];
		}

		int[][] _goto = new int[gotoList.size()][2];
		for (int i = 0; i < _goto.length; i++) {
			_goto[i][SLRTables.SYM] = gotoList.get(i)[SLRTables.SYM];
			_goto[i][SLRTables.NXT] = gotoList.get(i)[SLRTables.NXT];
		}

		Production p;
		int numProduction = PRODUCTIONS.size();
		int[] _lhs = new int[numProduction];
		int[] _rhs = new int[numProduction];
		for (int i = 0; i < numProduction; i++) {
			p = PRODUCTIONS.get(i);
			_lhs[i] = p.getNonterminal();
			_rhs[i] = p.size();
		}

		return SLRTables.build(
			new SLRTables.ShiftTable(shiftSwitch, _shift),
			new SLRTables.ReduceTable(_reduce),
			new SLRTables.GotoTable(gotoSwitch, _goto),
			new SLRTables.ProductionTable(_lhs, _rhs),
			numWithShiftReduce,
			numWithReduceReduce
		);
	}

	public SLRTables getGeneratedTables() {
		return SLR_TABLES;
	}
}
