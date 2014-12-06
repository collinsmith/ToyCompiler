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
	
	public static final int RDCE = 0;
	public static final int SWCH = 1;

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
		if (shiftTable.SWITCH.length != reduceTable.REDUCE_SWITCH.length || reduceTable.REDUCE_SWITCH.length != gotoTable.SWITCH.length) {
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
		return REDUCE.REDUCE_SWITCH[stateId][RDCE];
	}
	
	public int reduce(int stateId, int lookaheadSymbolId) {
		int id = REDUCE.REDUCE_SWITCH[stateId][SWCH];
		if (id == Integer.MIN_VALUE) {
			return Integer.MIN_VALUE;
		}

		int cache;
		while ((cache = REDUCE.LOOKAHEAD[id]) != Integer.MIN_VALUE && cache != lookaheadSymbolId) {
			id++;
		}

		return cache == lookaheadSymbolId ? REDUCE.REDUCE_SWITCH[stateId][RDCE] : Integer.MIN_VALUE;
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
			writer.append(String.format("%-8s: reduce = %d%n",	"Reduce",	REDUCE.REDUCE_SWITCH.length));
			writer.append(String.format("%-8s: lookahead = %d%n",	"",		REDUCE.LOOKAHEAD.length));
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

			writer.append(String.format("%-35s |%-35s |%-35s%n", "SHIFT", "REDUCE", "GOTO"));
			writer.append(String.format("%-6s %-6s |%-6s %-6s %-6s |%-6s %-6s %-6s |%-6s %-6s |%-6s %-6s |%-6s %-6s %-6s%n",
				"", "switch", "", "symbol", "next",
				"", "reduce", "switch", "", "LA",
				"", "switch", "", "symbol", "next"
			));

			int maxLen = Integer.max(SHIFT.SHIFT.length, Integer.max(REDUCE.LOOKAHEAD.length, GOTO.GOTO.length));
			for (int i = 0; i < maxLen; i++) {
				writer.append(String.format("%-6s %-6s |%-6s %-6s %-6s |%-6s %-6s %-6s |%-6s %-6s |%-6s %-6s |%-6s %-6s %-6s%n",
					i < SHIFT.SWITCH.length ? String.format("S%d", i) : "",
					i < SHIFT.SWITCH.length ? convertValue(SHIFT.SWITCH[i]) : "",
					i < SHIFT.SHIFT.length ? i : "",
					i < SHIFT.SHIFT.length ? convertValue(SHIFT.SHIFT[i][SYM]) : "",
					i < SHIFT.SHIFT.length ? convertValue(SHIFT.SHIFT[i][NXT]) : "",

					i < REDUCE.REDUCE_SWITCH.length ? String.format("S%d", i) : "",
					i < REDUCE.REDUCE_SWITCH.length ? convertValue(REDUCE.REDUCE_SWITCH[i][RDCE]) : "",
					i < REDUCE.REDUCE_SWITCH.length ? convertValue(REDUCE.REDUCE_SWITCH[i][SWCH]) : "",
					i < REDUCE.LOOKAHEAD.length ? i : "",
					i < REDUCE.LOOKAHEAD.length ? convertValue(REDUCE.LOOKAHEAD[i]) : "",

					i < GOTO.SWITCH.length ? String.format("S%d", i) : "",
					i < GOTO.SWITCH.length ? convertValue(GOTO.SWITCH[i]) : "",
					i < GOTO.GOTO.length ? i : "",
					i < GOTO.GOTO.length ? convertValue(GOTO.GOTO[i][SYM]) : "",
					i < GOTO.GOTO.length ? convertValue(GOTO.GOTO[i][NXT]) : ""
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
		private final int[][] REDUCE_SWITCH;
		private final int[] LOOKAHEAD;

		public ReduceTable(int[][] reduceSwitchTable, int[] lookaheadTable) {
			this.REDUCE_SWITCH = reduceSwitchTable;
			this.LOOKAHEAD = lookaheadTable;
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
