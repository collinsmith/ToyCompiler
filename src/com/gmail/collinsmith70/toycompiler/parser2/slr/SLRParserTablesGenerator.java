package com.gmail.collinsmith70.toycompiler.parser2.slr;

import com.gmail.collinsmith70.toycompiler.parser2.Grammar;
import com.gmail.collinsmith70.toycompiler.parser2.ParserTablesGenerator;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public abstract class SLRParserTablesGenerator implements ParserTablesGenerator<SLRParserTables> {
	private static final Logger LOGGER = Logger.getLogger("ToyCompilerLogger");

	public SLRParserTablesGenerator() {
		//...
	}

	@Override
	public SLRParserTables generateParserTables(Grammar g) {
		Preconditions.checkNotNull(g);

		LOGGER.addHandler(new ConsoleHandler());
		LOGGER.setLevel(Level.ALL);

		try {
			FileHandler fileHandler = new FileHandler(Paths.get(".", "logs", g.getName() + ".txt").toString(), true);
			fileHandler.setFormatter(new SimpleFormatter());
			LOGGER.addHandler(fileHandler);
		} catch (IOException e) {
			LOGGER.severe("Log file could not be generated!");
		}

		LOGGER.info("Generating tables...");
		long dt = System.currentTimeMillis();
		/*this.TABLES = generateParserTables();
		LOGGER.info(String.format("Tables generated in %dms; %d tables",
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
}
