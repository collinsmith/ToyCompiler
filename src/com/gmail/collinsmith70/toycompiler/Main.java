package com.gmail.collinsmith70.toycompiler;

import com.gmail.collinsmith70.toycompiler.lexer.Scanner;
import com.gmail.collinsmith70.toycompiler.lexer.TokenStream;
import com.gmail.collinsmith70.toycompiler.lexer.ToyScanner;
import com.gmail.collinsmith70.toycompiler.parser.Grammar;
import com.gmail.collinsmith70.toycompiler.parser.LAProductionRuleInstance;
import com.gmail.collinsmith70.toycompiler.parser.LRParserTables;
import com.gmail.collinsmith70.toycompiler.parser.Parser;
import com.gmail.collinsmith70.toycompiler.parser.ProductionRuleInstance;
import com.gmail.collinsmith70.toycompiler.parser.State;
import com.gmail.collinsmith70.toycompiler.parser.lalr.LALRParser;
import com.gmail.collinsmith70.toycompiler.parser.lalr.LALRParserStatesGenerator;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class Main {
	private static final Path OUTPUT_PATH = Paths.get(".", "output");
	private static final Scanner TOY_SCANNER = new ToyScanner();

	private static Parser parser;

	public static void main(String[] args) {
		try {
			//Grammar g = Grammar.generate(Paths.get(".", "res", "test1.grammar"), Charset.forName("US-ASCII"));
			//Grammar g = Grammar.generate(Paths.get(".", "res", "test2.grammar"), Charset.forName("US-ASCII"));
			Grammar g = Grammar.generate(Paths.get(".", "res", "toy.grammar"), Charset.forName("US-ASCII"));
			g.output();
			LALRParserStatesGenerator lalrParserStatesGenerator = new LALRParserStatesGenerator();
			Map<Set<ProductionRuleInstance>, State<LAProductionRuleInstance>> parserStates = lalrParserStatesGenerator.generateParserTables(g);
			LALRParserStatesGenerator.outputStates(g, parserStates);

			g.getLogger().info("Compiling parser states into tables...");
			long dt = System.currentTimeMillis();
			LRParserTables tables = LALRParserStatesGenerator.compile(g, parserStates);
			g.getLogger().info(String.format("Tables compiled in %dms",
				System.currentTimeMillis()-dt
			));

			tables.output(g);
			parser = new LALRParser(tables);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Arrays.stream(args)
			.forEachOrdered(arg -> {
				try {
					DirectoryStream<Path> files = Files.newDirectoryStream(Paths.get(arg));
					for (Path path : files) {
						if (!Files.isReadable(path)) {
							continue;
						}

						compile(path);
					}
				} catch (IOException e) {
					System.out.format("Unreadable source: \"%s\"", arg);
				}
			});
	}

	private static void compile(Path p) {
		String fileName = p.getFileName().toString();
		Path outFile = OUTPUT_PATH.resolve("parsetrees").resolve(fileName.substring(0, fileName.lastIndexOf('.')) + ".parsetree");
		Charset charset = Charset.forName("US-ASCII");
		try (BufferedWriter writer = Files.newBufferedWriter(outFile, charset, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
			try (BufferedReader br = Files.newBufferedReader(p, charset)) {
				System.out.format("Analyzing %s...%n", fileName);
				long dt = System.currentTimeMillis();
				TokenStream tokenStream = new TokenStream(TOY_SCANNER, br);
				parser.parse(tokenStream, writer);
				System.out.format("%s scanned and parsed in %dms%n", fileName, System.currentTimeMillis()-dt);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
