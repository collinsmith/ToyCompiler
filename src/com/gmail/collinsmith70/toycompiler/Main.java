package com.gmail.collinsmith70.toycompiler;

import com.gmail.collinsmith70.toycompiler.lexer.Scanner;
import com.gmail.collinsmith70.toycompiler.lexer.TokenStream;
import com.gmail.collinsmith70.toycompiler.lexer.ToyScanner;
import com.gmail.collinsmith70.toycompiler.parser.Parser;
import com.gmail.collinsmith70.toycompiler.parser2.Grammar;
import com.gmail.collinsmith70.toycompiler.parser2.slr.SLRParserStatesGenerator;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
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
		/*ProductionRuleInstance[] set1 = new ProductionRuleInstance[] {
			new ProductionRule(new NonterminalSymbol(1), ImmutableList.of(new TerminalSymbol(2), new TerminalSymbol(3), new NonterminalSymbol(1))).createInstance(),
			new ProductionRule(new NonterminalSymbol(4, new NonterminalSymbol(1)), ImmutableList.of(new TerminalSymbol(2), new TerminalSymbol(3), new NonterminalSymbol(1))).createInstance(),
		};

		ProductionRuleInstance[] set2 = new ProductionRuleInstance[] {
			new ProductionRule(new NonterminalSymbol(1), ImmutableList.of(new TerminalSymbol(2), new TerminalSymbol(3), new NonterminalSymbol(1))).createInstance(),
			new ProductionRule(new NonterminalSymbol(4, new NonterminalSymbol(1)), ImmutableList.of(new TerminalSymbol(2), new TerminalSymbol(3), new NonterminalSymbol(1))).createInstance(),
		};

		System.out.println(Objects.hashCode(set1) == Objects.hashCode(set2));

		ImmutableSet<ProductionRuleInstance> set3 = ImmutableSet.copyOf(set1);
		ImmutableSet<ProductionRuleInstance> set4 = ImmutableSet.copyOf(set2);
		System.out.println("objects hashes? " + (Objects.hashCode(set3) == Objects.hashCode(set4)));
		System.out.println("hashes equal? " + (set3.hashCode() == set4.hashCode()));
		System.out.println("objects equal? " + (set3.equals(set4)));

		Map<Set<ProductionRuleInstance>, Integer> test = new LinkedHashMap<>();
		test.put(set3, 1);
		System.out.println("get set3 " + test.get(set3));
		System.out.println("get set4 " + test.get(set4));

		ProductionRule[] set5 = new ProductionRule[] {
			new ProductionRule(new NonterminalSymbol(1), ImmutableList.of(new TerminalSymbol(2), new TerminalSymbol(3), new NonterminalSymbol(1))),
			new ProductionRule(new NonterminalSymbol(4, new NonterminalSymbol(1)), ImmutableList.of(new TerminalSymbol(2), new TerminalSymbol(3), new NonterminalSymbol(1))),
		};

		ProductionRule[] set6 = new ProductionRule[] {
			new ProductionRule(new NonterminalSymbol(1), ImmutableList.of(new TerminalSymbol(2), new TerminalSymbol(3), new NonterminalSymbol(1))),
			new ProductionRule(new NonterminalSymbol(4, new NonterminalSymbol(1)), ImmutableList.of(new TerminalSymbol(2), new TerminalSymbol(3), new NonterminalSymbol(1))),
		};

		ImmutableSet<ProductionRule> set7 = ImmutableSet.copyOf(set5);
		ImmutableSet<ProductionRule> set8 = ImmutableSet.copyOf(set6);
		Map<Set<ProductionRule>, Integer> test2 = new LinkedHashMap<>();
		test2.put(set7, 1);
		System.out.println("get set7 " + test2.get(set7));
		System.out.println("get set8 " + test2.get(set8));

		Production[] set9 = new Production[] {
			new Production(0, ImmutableList.of(0, 1, 2)),
			new Production(0, ImmutableList.of(0, 1)),
		};

		Production[] set10 = new Production[] {
			new Production(0, ImmutableList.of(0, 1, 2)),
			new Production(0, ImmutableList.of(0, 1)),
		};

		ImmutableSet<Production> set11 = ImmutableSet.copyOf(set9);
		ImmutableSet<Production> set12 = ImmutableSet.copyOf(set10);
		Map<Set<Production>, Integer> test3 = new HashMap<>();
		test3.put(set11, 1);
		System.out.println("objects hashes? " + (Objects.hashCode(set11) == Objects.hashCode(set12)));
		System.out.println("hashes equal? " + (set11.hashCode() == set12.hashCode()));
		System.out.println("objects equal? " + (set11.equals(set12)));
		System.out.println("get set11 " + test3.get(set11));
		System.out.println("get set12 " + test3.get(set12));

		ImmutableList<Symbol> symbols1 = ImmutableList.of(
			new TerminalSymbol(1),
			new NonterminalSymbol(2),
			new TerminalSymbol(3),
			new NonterminalSymbol(4)
		);

		ImmutableList<Symbol> symbols2 = ImmutableList.of(
			new TerminalSymbol(1),
			new NonterminalSymbol(2),
			new TerminalSymbol(3),
			new NonterminalSymbol(4)
		);

		System.out.println(symbols1.equals(symbols2));
		System.out.println(symbols1.hashCode() == symbols2.hashCode());

		Map<ImmutableList<Symbol>, Integer> test4 = new LinkedHashMap<>();
		test4.put(symbols1, 1);
		System.out.println("get symbols1 " + test4.get(symbols1));
		System.out.println("get symbols2 " + test4.get(symbols2));

		ProductionRule r1 = new ProductionRule(new NonterminalSymbol(5), symbols1);
		ProductionRule r2 = new ProductionRule(new NonterminalSymbol(5), symbols2);
		System.out.println("symbols? " + r1.getNonterminalSymbol().equals(r2.getNonterminalSymbol()));
		System.out.println("rhs? " + r1.getRHS().equals(r2.getRHS()));
		System.out.println("rhs size? " + (r1.getRHS().size() == r2.getRHS().size()));
		System.out.println(r1.equals(r2));
		System.out.println(r1.hashCode() == r2.hashCode());*/

		try {
			Grammar g = Grammar.generate(Paths.get(".", "res", "toy.grammar"), Charset.forName("US-ASCII"));
			SLRParserStatesGenerator s = new SLRParserStatesGenerator();
			//s.generateParserTables(g);
			SLRParserStatesGenerator.outputTables(g, s.generateParserTables(g));
		} catch (IOException e) {
			e.printStackTrace();
		}

		/*try {
			SLRParserGenerator parserGenerator = new SLRParserGenerator(Paths.get(".", "res", "toy.cfg.txt"));
			parserGenerator.outputCFG();
			parserGenerator.outputTables();
			parser = new SLRParser(parserGenerator.getGeneratedTables());
		} catch (IOException e) {
			System.out.println("Unable to open toy.cfg.txt");
		}

		Arrays.stream(args)
			.forEach(arg -> {
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
		Path outFile = OUTPUT_PATH.resolve(fileName.substring(0, fileName.lastIndexOf('.')) + ".output.txt");
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
