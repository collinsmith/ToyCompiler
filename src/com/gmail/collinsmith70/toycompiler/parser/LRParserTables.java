package com.gmail.collinsmith70.toycompiler.parser;

import com.gmail.collinsmith70.toycompiler.lexer.Token;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class LRParserTables {
	public static final int SYM = 0;
	public static final int NXT = 1;

	private final ShiftTable SHIFT;
	private final ReduceTable REDUCE;
	private final GotoTable GOTO;
	private final ProductionRulesTable PRODUCTION_RULES;

	private final ShiftReduceConflictsTable SHIFT_REDUCE_CONFLICTS;
	private final ReduceReduceConflictsTable REDUCE_REDUCE_CONFLICTS;

	public LRParserTables(
		ShiftTable shiftTable,
		ReduceTable reduceTable,
		GotoTable gotoTable,
		ProductionRulesTable productionRulesTable,
		ShiftReduceConflictsTable shiftReduceConflicts,
		ReduceReduceConflictsTable reduceReduceConflicts
	) {
		if (shiftTable.SWITCH.length != reduceTable.REDUCE.length || reduceTable.REDUCE.length != gotoTable.SWITCH.length) {
			throw new IllegalArgumentException("Table sizes do not match!");
		}

		this.SHIFT = shiftTable;
		this.REDUCE = reduceTable;
		this.GOTO = gotoTable;
		this.PRODUCTION_RULES = productionRulesTable;

		this.SHIFT_REDUCE_CONFLICTS = shiftReduceConflicts;
		this.REDUCE_REDUCE_CONFLICTS = reduceReduceConflicts;
	}

	public int shift(int stateId, int symbolId) {
		int id = SHIFT.SWITCH[stateId];
		if (id == Integer.MIN_VALUE) {
			return Integer.MIN_VALUE;
		}

		int cache;
		while ((cache = SHIFT.SHIFT[id][SYM]) != Integer.MIN_VALUE && cache != symbolId) {
			id++;
		}

		return cache == symbolId ? SHIFT.SHIFT[id][NXT] : Integer.MIN_VALUE;
	}

	public int reduce(int stateId) {
		return REDUCE.REDUCE[stateId];
	}

	public int transition(int stateId, int symbolId) {
		int id = GOTO.SWITCH[stateId];
		if (id == Integer.MIN_VALUE) {
			return Integer.MIN_VALUE;
		}

		int cache;
		while ((cache = GOTO.GOTO[id][SYM]) != Integer.MIN_VALUE && cache != symbolId) {
			id++;
		}

		return cache == symbolId ? GOTO.GOTO[id][NXT] : Integer.MIN_VALUE;
	}

	public int nonterminal(int productionId) {
		return PRODUCTION_RULES.NONTERMINAL[productionId];
	}

	public int rhsSize(int productionId) {
		return PRODUCTION_RULES.RHS_SIZE[productionId];
	}

	public int tokenId(Token t) {
		if (t == null) {
			return Integer.MIN_VALUE;
		}

		return t.getTokenType().getId();
	}

	public void output(Grammar g) {
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(".", "output", g.getName() + ".compiled"), Charset.forName("US-ASCII"), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
			writer.append(String.format("%-8s: switch = %d%n",	"Shift",	SHIFT.SWITCH.length));
			writer.append(String.format("%-8s  shift = %d%n",	"",		SHIFT.SHIFT.length));
			writer.append(String.format("%-8s: reduce = %d%n",	"Reduce",	REDUCE.REDUCE.length));
			writer.append(String.format("%-8s: switch = %d%n",	"Goto",	GOTO.SWITCH.length));
			writer.append(String.format("%-8s  goto = %d%n",	"",		GOTO.GOTO.length));
			writer.append(String.format("%n%n"));

			writer.append(String.format("%d tables%n", SHIFT.SWITCH.length));
			writer.append(String.format("%d shift-reduce conflicts%n", SHIFT_REDUCE_CONFLICTS.SHIFT_REDUCE_CONFLICT.length));
			if (SHIFT_REDUCE_CONFLICTS.SHIFT_REDUCE_CONFLICT.length > 0) {
				writer.append(String.format("\t%s%n", Arrays.toString(SHIFT_REDUCE_CONFLICTS.SHIFT_REDUCE_CONFLICT)));
			}
			writer.append(String.format("%d reduce-reduce conflicts%n", REDUCE_REDUCE_CONFLICTS.REDUCE_REDUCE_CONFLICT.length));
			if (REDUCE_REDUCE_CONFLICTS.REDUCE_REDUCE_CONFLICT.length > 0) {
				writer.append(String.format("\t%s%n", Arrays.toString(REDUCE_REDUCE_CONFLICTS.REDUCE_REDUCE_CONFLICT)));
			}
			writer.append(String.format("%n%n"));

			writer.append(String.format("%-30s|%-12s|%-30s%n", "SHIFT", "REDUCE", "GOTO"));
			writer.append(String.format("%-6s%-6s%-6s%-6s%-6s|%-6s%-6s|%-6s%-6s%-6s%-6s%-6s%n", "", "switch", "", "symbol", "next", "", "reduce", "", "switch", "", "symbol", "next"));

			// TODO this will only print "shift" number of tables and not
			// the entirety of the tables
			int i;
			for (i = 0; i < SHIFT.SWITCH.length; i++) {
				writer.append(String.format("%-6s%-6s%-6s%-6s%-6s|%-6s%-6s|%-6s%-6s%-6s%-6s%-6s%n",
					String.format("S%d", i), convertValue(SHIFT.SWITCH[i]), i+1, convertValue(SHIFT.SHIFT[i+1][SYM]), convertValue(SHIFT.SHIFT[i+1][NXT]),
					String.format("S%d", i), convertValue(REDUCE.REDUCE[i]),
					String.format("S%d", i), convertValue(GOTO.SWITCH[i]), i+1, convertValue(GOTO.GOTO[i+1][SYM]), convertValue(GOTO.GOTO[i+1][NXT])
				));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String convertValue(int i) {
		if (i == Integer.MIN_VALUE) {
			return "";
		}

		return Integer.toString(i);
	}

	public static class ShiftTable {
		private final int[] SWITCH;
		private final int[][] SHIFT;

		public ShiftTable(int[] switchTable, int[][] shiftTable) {
			this.SWITCH = switchTable;
			this.SHIFT = shiftTable;
		}
	}

	public static class ReduceTable {
		private final int[] REDUCE;

		public ReduceTable(int[] reduceTable) {
			this.REDUCE = reduceTable;
		}
	}

	public static class GotoTable {
		private final int[] SWITCH;
		private final int[][] GOTO;

		public GotoTable(int[] switchTable, int[][] gotoTable) {
			this.SWITCH = switchTable;
			this.GOTO = gotoTable;
		}
	}

	public static class ProductionRulesTable {
		private final int[] NONTERMINAL;
		private final int[] RHS_SIZE;

		public ProductionRulesTable(int[] nonterminalIdTable, int[] rhsSizeTable) {
			this.NONTERMINAL = nonterminalIdTable;
			this.RHS_SIZE = rhsSizeTable;
		}
	}

	public static class ShiftReduceConflictsTable {
		private final int[] SHIFT_REDUCE_CONFLICT;

		public ShiftReduceConflictsTable(int[] shiftReduceConflict) {
			this.SHIFT_REDUCE_CONFLICT = shiftReduceConflict;
		}
	}

	public static class ReduceReduceConflictsTable {
		private final int[] REDUCE_REDUCE_CONFLICT;

		public ReduceReduceConflictsTable(int[] reduceReduceConflict) {
			this.REDUCE_REDUCE_CONFLICT = reduceReduceConflict;
		}
	}
}
