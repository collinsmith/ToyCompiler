package com.gmail.collinsmith70.toycompiler.parser2.slr;

import com.gmail.collinsmith70.toycompiler.parser2.Grammar;
import com.gmail.collinsmith70.toycompiler.parser2.ParserStatesGenerator;
import com.gmail.collinsmith70.toycompiler.parser2.ProductionRuleInstance;
import com.gmail.collinsmith70.toycompiler.parser2.State;
import com.gmail.collinsmith70.toycompiler.parser2.Symbol;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public abstract class SLRParserStatesGenerator implements ParserStatesGenerator<SLRParserTables> {
	private static final Logger LOGGER = Logger.getLogger("ToyCompilerLogger");
	private FileHandler loggerFileHandler;

	public SLRParserStatesGenerator() {
		LOGGER.addHandler(new ConsoleHandler());
		LOGGER.setLevel(Level.ALL);
	}

	@Override
	public SLRParserTables generateParserTables(Grammar g) {
		Preconditions.checkNotNull(g);

		if (loggerFileHandler == null) {
			try {
				loggerFileHandler = new FileHandler(Paths.get(".", "logs", g.getName() + ".txt").toString(), true);
				loggerFileHandler.setFormatter(new SimpleFormatter());
				LOGGER.addHandler(loggerFileHandler);
			} catch (IOException e) {
				LOGGER.severe("Log file could not be generated!");
			}
		}

		LOGGER.info("Generating tables...");
		long dt = System.currentTimeMillis();
		Map<Set<ProductionRuleInstance>, State> states = generateParserStates(g);
		/*LOGGER.info(String.format("Tables generated in %dms; %d tables",
			System.currentTimeMillis()-dt,
			this.TABLES.size()
		));*/

		dt = System.currentTimeMillis();
		LOGGER.info("Generating SLR tables...");
		/*this.SLR_TABLES = generateSLRTables();
		LOGGER.info(String.format("SLR tables generated in %dms; %d tables (%d shift-reduce conflicts, %d reduce-reduce conflicts)",
			System.currentTimeMillis()-dt,
			this.TABLES.size(),
			numWithShiftReduce,
			numWithReduceReduce
		));*/

		return null;
	}

	private Map<Set<ProductionRuleInstance>, State> generateParserStates(Grammar g) {
		Map<Set<ProductionRuleInstance>, State> states = new LinkedHashMap<>();

		Deque<State.Metadata> deque = new LinkedBlockingDeque<>();
		deque.offerLast(new State.Metadata(null, null, g.getNonterminalProductionRules().get(g.getInitialNonterminalSymbol())));

		while (!deque.isEmpty()) {
			generateChildState(deque.pollFirst(), states, deque);
		}

		return states;
	}

	private void generateChildState(State.Metadata metadata, Map<Set<ProductionRuleInstance>, State> states, Deque<State.Metadata> deque) {
		State parent;
		Symbol symbol;
		ImmutableSet<ProductionRuleInstance> items;
		State.Metadata metadata = deque.pollFirst();

	}
}
