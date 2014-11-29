package com.gmail.collinsmith70.toycompiler.parser2;

import com.gmail.collinsmith70.toycompiler.lexer.TokenType;
import com.gmail.collinsmith70.toycompiler.lexer.ToyTokenTypes;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Pattern;

public class Grammar {
	private static final Pattern NONTERMINAL_PATTERN = Pattern.compile("[A-Z]\\w*:$");

	private final ImmutableBiMap<String, Symbol> SYMBOLS;
	//private final ImmutableMap<Symbol, Symbol> RESOLUTION;

	private final ImmutableList<ProductionRule> PRODUCTION_RULES;
	private final ImmutableBiMap<NonterminalSymbol, ImmutableSet<ProductionRule>> PRODUCTION_RULES_MAP;

	private static final Logger LOGGER = Logger.getLogger("ToyCompilerLogger");

	private final String GRAMMAR_NAME;

	private int numTerminalSymbols;
	private int numNonterminalSymbols;
	private int numUnreachableSymbols;

	private NonterminalSymbol initialNonterminal;

	public static Grammar generate(Path p, Charset c) throws IOException {
		return new Grammar(Objects.requireNonNull(p), Objects.requireNonNull(c));
	}

	private Grammar(Path p, Charset c) throws IOException {
		assert p != null;

		this.numTerminalSymbols = Integer.MIN_VALUE;
		this.numNonterminalSymbols = Integer.MIN_VALUE;
		numUnreachableSymbols = Integer.MIN_VALUE;

		String grammarName = p.getFileName().toString();
		grammarName = grammarName.substring(0, grammarName.lastIndexOf('.'));
		GRAMMAR_NAME = grammarName;

		LOGGER.setLevel(Level.ALL);

		try {
			FileHandler fileHandler = new FileHandler(Paths.get(".", "logs", GRAMMAR_NAME + ".log").toString(), true);
			fileHandler.setFormatter(new SimpleFormatter());
			LOGGER.addHandler(fileHandler);
		} catch (IOException e) {
			LOGGER.addHandler(new ConsoleHandler());
			LOGGER.severe("Log file could not be generated. Switching to console.");
		}

		LOGGER.info("Generating symbols table...");
		long dt = System.currentTimeMillis();
		//Map<Symbol, Symbol> resolution = new HashMap<>();
		this.SYMBOLS = generateSymbolsTable(p, c/*, resolution*/);
		//this.RESOLUTION = ImmutableMap.copyOf(resolution);
		LOGGER.info(String.format("Symbols table generated in %dms; %d symbols (%d terminal symbols, %d nonterminal symbols)",
			System.currentTimeMillis()-dt,
			numTerminalSymbols+numNonterminalSymbols,
			numTerminalSymbols,
			numNonterminalSymbols
		));

		LOGGER.info("Generating productions table...");
		dt = System.currentTimeMillis();
		ArrayList<ProductionRule> productions = new ArrayList<>();
		this.PRODUCTION_RULES_MAP = generateProductions(p, c, productions);
		this.PRODUCTION_RULES = ImmutableList.copyOf(productions);
		LOGGER.info(String.format("Productions table and list created in %dms; %d productions (%d unreachable symbols)",
			System.currentTimeMillis()-dt,
			this.PRODUCTION_RULES.size(),
			numUnreachableSymbols
		));
	}

	public Logger getLogger() {
		return LOGGER;
	}

	public String getName() {
		return GRAMMAR_NAME;
	}

	public NonterminalSymbol getInitialNonterminalSymbol() {
		return initialNonterminal;
	}

	public ImmutableBiMap<String, Symbol> getSymbolsTable() {
		return SYMBOLS;
	}

	public ImmutableList<ProductionRule> getProductionRules() {
		return PRODUCTION_RULES;
	}

	public ImmutableBiMap<NonterminalSymbol, ImmutableSet<ProductionRule>> getProductionRulesMap() {
		return PRODUCTION_RULES_MAP;
	}

	private ImmutableBiMap<String, Symbol> generateSymbolsTable(Path p, Charset c/*, Map<Symbol, Symbol> resolution*/) throws IOException {
		numTerminalSymbols = numNonterminalSymbols = 0;
		BiMap<String, Symbol> symbols = HashBiMap.create();

		TerminalSymbol symbol, alternateSymbol;

		Enum e;
		TokenType t;
		Iterator<Enum> defaultTokens = Iterators.forArray(TokenType.DefaultTokenTypes.values());
		Iterator<Enum> toyTokenTypes = Iterators.forArray(ToyTokenTypes.values());
		for (Iterator<Enum> it = Iterators.concat(defaultTokens, toyTokenTypes); it.hasNext(); ) {
			e = it.next();
			assert e instanceof TokenType : "Enumeration should implement " + TokenType.class.toString();
			t = (TokenType)e;
			symbol = new TerminalSymbol(t.getId());
			symbols.put(t.name(), symbol);
			//resolution.put(symbol, symbol);
			if (t.isLiteral()) {
				alternateSymbol = new TerminalSymbol(Integer.MIN_VALUE+t.getId(), symbol);
				symbols.put(t.getRegex(), alternateSymbol);
				//resolution.put(alternateSymbol, symbol);
			}

			numTerminalSymbols++;
		}

		// Above part should concatonate both of the below loops
		/*for (TokenType.DefaultTokenTypes k : TokenType.DefaultTokenTypes.values()) {
			symbol = new TerminalSymbol(k.getId());
			symbols.put(k.name(), symbol);
			resolution.put(symbol, symbol);
			System.out.format("Resolve %s -> %s%n", symbol, symbol);
			if (k.isLiteral()) {
				alternateSymbol = new TerminalSymbol(Integer.MIN_VALUE+k.getId());
				symbols.put(k.getRegex(), alternateSymbol);
				resolution.put(alternateSymbol, symbol);
				System.out.format("Resolve %s -> %s%n", alternateSymbol, symbol);
			}

			numTerminalSymbols++;
		}

		for (ToyTokenTypes k : ToyTokenTypes.values()) {
			symbol = new TerminalSymbol(k.getId());
			symbols.put(k.name(), symbol);
			resolution.put(symbol, symbol);
			System.out.format("Resolve %s -> %s%n", symbol, symbol);
			if (k.isLiteral()) {
				alternateSymbol = new TerminalSymbol(Integer.MIN_VALUE+k.getId());
				symbols.put(k.getRegex(), alternateSymbol);
				resolution.put(alternateSymbol, symbol);
				System.out.format("Resolve %s -> %s%n", alternateSymbol, symbol);
			}

			numTerminalSymbols++;
		}*/

		try (BufferedReader br = Files.newBufferedReader(p, c)) {
			NonterminalSymbol nonterminalSymbol;
			String line;
			while ((line = br.readLine()) != null) {
				if (!NONTERMINAL_PATTERN.matcher(line).matches()) {
					continue;
				}

				nonterminalSymbol = new NonterminalSymbol(numTerminalSymbols+numNonterminalSymbols);
				if (initialNonterminal == null) {
					initialNonterminal = nonterminalSymbol;
				}

				line = line.substring(0, line.length()-1);
				//System.out.println(line + ", " + id);
				//System.out.println(symbols.inverse().get(id));
				symbols.put(line, nonterminalSymbol);
				//resolution.put(nonterminalSymbol, nonterminalSymbol);
				numNonterminalSymbols++;
			}
		}

		return ImmutableBiMap.copyOf(symbols);
	}

	private ImmutableBiMap<NonterminalSymbol, ImmutableSet<ProductionRule>> generateProductions(Path p, Charset c, ArrayList<ProductionRule> productions) throws IOException {
		BiMap<NonterminalSymbol, Set<ProductionRule>> nonterminals = HashBiMap.create();
		Set<Symbol> usedSymbols = new HashSet<>();
		try (BufferedReader br = Files.newBufferedReader(p, c)) {
			ProductionRule productionRule;
			Set<ProductionRule> nonterminalProductions;
			NonterminalSymbol currentNonterminalSymbol = null;

			int id;
			String line;
			String[] tokens;
			while ((line = br.readLine()) != null) {
				if (line.isEmpty()) {
					continue;
				}

				if (NONTERMINAL_PATTERN.matcher(line).matches()) {
					line = line.substring(0, line.length()-1);
					assert SYMBOLS.get(line) instanceof NonterminalSymbol;
					currentNonterminalSymbol = (NonterminalSymbol)SYMBOLS.get(line);
					if (currentNonterminalSymbol == null) {
						throw new GrammarGenerationException("Nonterminal symbol %s is not within the set of symbols!",
							line
						);
					} else if (currentNonterminalSymbol.getId() < numTerminalSymbols) {
						throw new GrammarGenerationException("Terminal symbol %s cannot be declared as a production nonterminal!",
							SYMBOLS.inverse().get(currentNonterminalSymbol)
						);
					}

					usedSymbols.add(currentNonterminalSymbol);
					continue;
				}

				if (currentNonterminalSymbol == null) {
					throw new GrammarGenerationException("Production rule %s does not belong to any declared nonterminal!",
						line
					);
				}

				nonterminalProductions = nonterminals.get(currentNonterminalSymbol);
				if (nonterminalProductions == null) {
					nonterminalProductions = new HashSet<>();
					nonterminals.put(currentNonterminalSymbol, nonterminalProductions);
				}

				ArrayList<Symbol> productionList = new ArrayList<>();

				line = line.trim();
				tokens = line.split("\\s+");
				for (String token : tokens) {
					if (token.isEmpty()) {
						continue;
					}

					Symbol resolvedToken = resolveSymbol(token);
					assert resolvedToken != null : "Could not resolve token";
					usedSymbols.add(resolvedToken);
					productionList.add(resolvedToken);
				}

				productionList.trimToSize();
				productionRule = new ProductionRule(currentNonterminalSymbol, ImmutableList.copyOf(productionList));
				if (!productions.contains(productionRule)) {
					productions.add(productionRule);
				}

				nonterminalProductions.add(productionRule);
			}
		}

		productions.trimToSize();

		numUnreachableSymbols = 0;
		Set<Symbol> unusedSymbols = new HashSet<>(SYMBOLS.values());
		unusedSymbols.removeAll(usedSymbols);
		unusedSymbols.stream()
			.filter((symbol) -> !(symbol.getId() < 0)).map((symbol) -> {
				numUnreachableSymbols++;
				return symbol;
			})
			.forEach((symbol) -> System.out.format("%s (%s) is unreachable%n", SYMBOLS.inverse().get(symbol), symbol));

		BiMap<NonterminalSymbol, ImmutableSet<ProductionRule>> immutableNonterminals = HashBiMap.create();
		nonterminals.entrySet().stream()
			.forEach((entry) -> immutableNonterminals.put(entry.getKey(), ImmutableSet.copyOf(entry.getValue())));

		return ImmutableBiMap.copyOf(immutableNonterminals);
	}

	public final Symbol resolveSymbol(String token) {
		Preconditions.checkNotNull(token);
		Symbol resolved = SYMBOLS.get(token);
		if (resolved == null) {
			throw new GrammarGenerationException("Symbol %s is undefined!",
				token
			);
		}

		if (resolved.getParent() != null) {
			return resolved.getParent();
		}

		//return RESOLUTION.get(resolved);
		return resolved;
	}

	/*public final boolean isNonterminal(Symbol s) {
		return numTerminalSymbols <= s.getId();
	}*/

	public void outputGrammar() {
		outputSymbols();
		outputProductionRules();
	}

	private void outputSymbols() {
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(".", "output", GRAMMAR_NAME + ".symbols"), Charset.forName("US-ASCII"), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
			BiMap.Entry<String, Symbol>[] entries = SYMBOLS.entrySet().toArray(new BiMap.Entry[0]);
			Arrays.sort(entries, (Map.Entry<String, Symbol> o1, Map.Entry<String, Symbol> o2) -> o1.getValue().compareTo(o2.getValue()));

			writer.write(String.format("%4s %-20s %s%n",
				"ID",
				"Token Type",
				"Alternative (Optional)"
			));

			boolean printingNonterminalSymbols = false;
			for (BiMap.Entry<String, Symbol> entry : entries) {
				if (entry.getValue().getParent() != null) {
					continue;
				} else if (!printingNonterminalSymbols && entry.getValue() instanceof NonterminalSymbol) {
					printingNonterminalSymbols = true;
					writer.write(String.format("%n----------------------------------------------------------------%n"));
					writer.write(String.format("%4s %s%n",
						"ID",
						"Nonterminal"
					));
				}

				String alternateValue = SYMBOLS.inverse().get(new Symbol<>(entry.getValue().getId()-Integer.MIN_VALUE));
				writer.write(String.format("%4s %-20s %s%n",
					entry.getValue(),
					entry.getKey(),
					Strings.nullToEmpty(alternateValue)
				));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void outputProductionRules() {
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(".", "output", GRAMMAR_NAME + ".productions"), Charset.forName("US-ASCII"), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
			BiMap.Entry<Symbol, Set<ProductionRule>>[] entries = PRODUCTION_RULES_MAP.entrySet().toArray(new BiMap.Entry[0]);
			Arrays.sort(entries, (Map.Entry<Symbol, Set<ProductionRule>> o1, Map.Entry<Symbol, Set<ProductionRule>> o2) -> o1.getKey().compareTo(o2.getKey()));

			writer.write(String.format("[%4s] %s%n",
				"ID",
				"Nonterminal: Production Rules"
			));

			for (BiMap.Entry<Symbol, Set<ProductionRule>> entry : entries) {
				writer.write(String.format("[%4s] %s:%n",
					entry.getKey(),
					SYMBOLS.inverse().get(entry.getKey())
				));

				ProductionRule[] productionRules = entry.getValue().toArray(new ProductionRule[0]);
				Arrays.sort(productionRules, (ProductionRule o1, ProductionRule o2) -> PRODUCTION_RULES.indexOf(o1) - PRODUCTION_RULES.indexOf(o2));

				for (ProductionRule p : productionRules) {
					writer.write(String.format("\t%3d\t", PRODUCTION_RULES.indexOf(p)));
					for (Symbol s : p) {
						writer.write(String.format("%s[%s] ", SYMBOLS.inverse().get(s), s));
					}

					writer.write(String.format("%n"));
				}

				writer.write(String.format("%n"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
