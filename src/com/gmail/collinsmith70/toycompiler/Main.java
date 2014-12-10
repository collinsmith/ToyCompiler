package com.gmail.collinsmith70.toycompiler;

import com.gmail.collinsmith70.toycompiler.cfg.ebnf.EBNFScanner;
import com.gmail.collinsmith70.toycompiler.lexer.Scanner;
import com.gmail.collinsmith70.toycompiler.lexer.TokenStream;
import com.gmail.collinsmith70.toycompiler.lexer.ToyScanner;
import com.gmail.collinsmith70.toycompiler.parser.Parser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Main {
	private static final Path OUTPUT_PATH = Paths.get(".", "output");
	private static final Scanner TOY_SCANNER = new ToyScanner();

	private static Parser parser;

	public static void main(String[] args) {
		try {
			for (int i = 1; i <= 2; i++) {
			Path p = Paths.get(".", "input", "ebnf", "test" + i + ".txt");
			System.out.println("opening: " + p);
			Reader r = Files.newBufferedReader(p, Charset.forName("UTF-8"));
			com.gmail.collinsmith70.toycompiler.cfg.TokenStream stream = new com.gmail.collinsmith70.toycompiler.cfg.TokenStream(new EBNFScanner(), r);
			while (stream.hasNext()) {
				System.out.println(stream.next());
			}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		/*try {
			//Grammar g = Grammar.generate(Paths.get(".", "res", "test1.grammar"), Charset.forName("UTF-8"));
			//Grammar g = Grammar.generate(Paths.get(".", "res", "test2.grammar"), Charset.forName("UTF-8"));
			Grammar g = Grammar.generate(Paths.get(".", "res", "toy.grammar"), Charset.forName("UTF-8"));
			g.output();
			LALRParserStatesGenerator lalrParserStatesGenerator = new LALRParserStatesGenerator();
			Map<Set<ProductionRuleInstance>, State<LAProductionRuleInstance>> parserStates = lalrParserStatesGenerator.generateParserTables(g);
			LALRParserStatesGenerator.outputStates(g, parserStates);

			g.getLogger().info("Compiling parser states into tables...");
			long dt = System.currentTimeMillis();
			LALRParserTables tables = LALRParserStatesGenerator.compile(g, parserStates);
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
			});*/
	}

	private static void compile(Path p) {
		String fileName = p.getFileName().toString();
		Path outFile = OUTPUT_PATH.resolve("parsetrees").resolve(fileName.substring(0, fileName.lastIndexOf('.')) + ".parsetree");
		Charset charset = Charset.forName("UTF-8");
		try (BufferedWriter writer = Files.newBufferedWriter(outFile, charset, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
			try (BufferedReader br = Files.newBufferedReader(p, charset)) {
				System.out.format("Analyzing %s...", fileName);
				long dt = System.currentTimeMillis();
				TokenStream tokenStream = new TokenStream(TOY_SCANNER, br);
				boolean accepted = parser.parse(tokenStream, writer);
				System.out.print(accepted ? "ACCEPTED" : "REJECTED");
				System.out.format("%n%s scanned and parsed in %dms%n", fileName, System.currentTimeMillis()-dt);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
