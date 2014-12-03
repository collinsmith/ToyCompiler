package com.gmail.collinsmith70.toycompiler.parser;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Pattern;
import static java.util.regex.Pattern.compile;

public final class Grammar {
	private static final Pattern NONTERMINAL_PATTERN = compile("[A-Z]\\w*:$");

	private final Logger LOGGER;

	private final String GRAMMAR_NAME;
	private final NonterminalSymbol INITIAL_NONTERMINAL;
	private final ImmutableBiMap<String, Symbol> SYMBOLS;
	private final ImmutableSet<ProductionRule> PRODUCTION_RULES;
	private final ImmutableBiMap<NonterminalSymbol, ImmutableSet<ProductionRule>> PRODUCTION_RULES_MAP;

	private int numTerminalSymbols;
	private int numNonterminalSymbols;
	private int numUnreachableSymbols;

	public static Grammar generate(Path p, Charset c) throws IOException {
		return new Grammar(Objects.requireNonNull(p), Objects.requireNonNull(c));
	}

	private Grammar(Path p, Charset c) throws IOException {
		assert p != null;
		assert c != null;

		String grammarName = p.getFileName().toString();
		grammarName = grammarName.substring(0, grammarName.lastIndexOf('.'));
		this.GRAMMAR_NAME = grammarName;

		this.LOGGER = Logger.getLogger(GRAMMAR_NAME + "CompilerLogger");
		try {
			FileHandler fileHandler = new FileHandler(Paths.get(".", "logs", GRAMMAR_NAME + ".log").toString(), true);
			fileHandler.setFormatter(new SimpleFormatter());
			LOGGER.addHandler(fileHandler);
		} catch (IOException e) {
			LOGGER.addHandler(new ConsoleHandler());
			LOGGER.severe("Log file could not be generated. Switching to console.");
		}

		this.numTerminalSymbols = Integer.MIN_VALUE;
		this.numNonterminalSymbols = Integer.MIN_VALUE;
		this.numUnreachableSymbols = Integer.MIN_VALUE;

		long dt;
		LOGGER.info("Generating symbols table...");
		dt = System.currentTimeMillis();
		BiMap<String, Symbol> symbolsTable = HashBiMap.create();
		Map<TerminalSymbol, TerminalSymbol> symbolResolutions = new HashMap<>();
		this.INITIAL_NONTERMINAL = generateSymbolsTable(p, c, symbolsTable, symbolResolutions);
		this.SYMBOLS = ImmutableBiMap.copyOf(symbolsTable);
		symbolResolutions = Collections.unmodifiableMap(symbolResolutions);
		LOGGER.info(String.format("Symbols table generated in %dms; %d symbols (%d terminal symbols, %d nonterminal symbols)",
			System.currentTimeMillis()-dt,
			numTerminalSymbols+numNonterminalSymbols,
			numTerminalSymbols,
			numNonterminalSymbols
		));

		LOGGER.info("Generating productions table...");
		dt = System.currentTimeMillis();
		LinkedHashSet<ProductionRule> productions = new LinkedHashSet<>();
		BiMap<NonterminalSymbol, Set<ProductionRule>> productionsMap = HashBiMap.create();
		generateProductions(p, c, symbolResolutions, productions, productionsMap);
		this.PRODUCTION_RULES = ImmutableSet.copyOf(productions);
		BiMap<NonterminalSymbol, ImmutableSet<ProductionRule>> immutableProductionsMap = HashBiMap.create();
		productionsMap.entrySet().parallelStream()
			.forEachOrdered(entry -> immutableProductionsMap.put(entry.getKey(), ImmutableSet.copyOf(entry.getValue())));
		this.PRODUCTION_RULES_MAP = ImmutableBiMap.copyOf(immutableProductionsMap);
		LOGGER.info(String.format("Productions table and list created in %dms; %d productions (%d unreachable symbols)",
			System.currentTimeMillis()-dt,
			PRODUCTION_RULES.size(),
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
		return INITIAL_NONTERMINAL;
	}

	public ImmutableBiMap<Symbol, String> getSymbolsTable() {
		return SYMBOLS.inverse();
	}

	public ImmutableSet<ProductionRule> getProductionRules() {
		return PRODUCTION_RULES;
	}

	public ImmutableBiMap<NonterminalSymbol, ImmutableSet<ProductionRule>> getProductionRulesMap() {
		return PRODUCTION_RULES_MAP;
	}

	public int getNumTerminalSymbols() {
		return numTerminalSymbols;
	}

	public int getNumNonterminalSymbols() {
		return numNonterminalSymbols;
	}

	public int getNumUnreachableSymbols() {
		return numUnreachableSymbols;
	}

	private Symbol resolveSymbol(String token, Map<TerminalSymbol, TerminalSymbol> symbolResolutions) {
		Preconditions.checkNotNull(token);
		Symbol s = SYMBOLS.get(token);
		if (s == null) {
			throw new GrammarGenerationException("Symbol '%s' is not within the set of symbols!", token);
		}

		if (s instanceof TerminalSymbol) {
			return symbolResolutions.get(s);
		}

		return s;
	}

	private NonterminalSymbol generateSymbolsTable(Path p, Charset c, BiMap<String, Symbol> symbolsTable, Map<TerminalSymbol, TerminalSymbol> symbolResolutions) throws IOException {
		numTerminalSymbols = 0;
		numNonterminalSymbols = 0;

		TerminalSymbol symbol, alternateSymbol;

		Enum e;
		TokenType t;
		Iterator<Enum> defaultTokens = Iterators.forArray(TokenType.DefaultTokenTypes.values());
		Iterator<Enum> toyTokenTypes = Iterators.forArray(ToyTokenTypes.values());
		for (Iterator<Enum> it = Iterators.concat(defaultTokens, toyTokenTypes); it.hasNext(); ) {
			e = it.next();
			assert e instanceof TokenType : "Enumeration should implement " + TokenType.class.toString();
			t = (TokenType)e;
			if (t == TokenType.DefaultTokenTypes._eof) {
				assert t.getId() == 0;
				symbol = TerminalSymbol.EMPTY_STRING;
			} else {
				assert t.getId() != 0;
				symbol = new TerminalSymbol(t.getId());
			}

			symbolsTable.put(t.name(), symbol);
			symbolResolutions.put(symbol, symbol);
			if (t.isLiteral()) {
				alternateSymbol = new TerminalSymbol(Integer.MIN_VALUE+t.getId());
				symbolsTable.put(t.getRegex(), alternateSymbol);
				symbolResolutions.put(alternateSymbol, symbol);
			}

			numTerminalSymbols++;
		}

		NonterminalSymbol initialNonterminal = null;
		try (BufferedReader br = Files.newBufferedReader(p, c)) {
			String line;
			NonterminalSymbol nonterminalSymbol;
			while ((line = br.readLine()) != null) {
				if (!NONTERMINAL_PATTERN.matcher(line).matches()) {
					continue;
				}

				nonterminalSymbol = new NonterminalSymbol(getNumTerminalSymbols()+getNumNonterminalSymbols());
				if (initialNonterminal == null) {
					initialNonterminal = nonterminalSymbol;
				}

				line = line.substring(0, line.length()-1);
				symbolsTable.put(line, nonterminalSymbol);
				numNonterminalSymbols++;
			}
		}

		return initialNonterminal;
	}

	private void generateProductions(Path p, Charset c, Map<TerminalSymbol, TerminalSymbol> symbolResolutions, Set<ProductionRule> productions, BiMap<NonterminalSymbol, Set<ProductionRule>> productionsMap) throws IOException {
		Set<Symbol> usedSymbols = new HashSet<>();
		try (BufferedReader br = Files.newBufferedReader(p, c)) {
			ArrayList<Symbol> rhs;
			ProductionRule productionRule;
			Set<ProductionRule> existingProductionRules;
			NonterminalSymbol currentNonterminalSymbol = null;

			int id;
			Symbol temp;
			String line;
			String[] tokens;
			while ((line = br.readLine()) != null) {
				if (line.isEmpty()) {
					continue;
				}

				if (NONTERMINAL_PATTERN.matcher(line).matches()) {
					line = line.substring(0, line.length()-1);
					temp = resolveSymbol(line, symbolResolutions);
					if (temp instanceof NonterminalSymbol) {
						currentNonterminalSymbol = (NonterminalSymbol)temp;
					} else {
						assert temp instanceof TerminalSymbol : "Symbols should only be TerminalSymbols or NonterminalSymbols";
						throw new GrammarGenerationException("Terminal symbol '%s' cannot be declared as a production rule nonterminal!",
							SYMBOLS.inverse().get(temp)
						);
					}

					usedSymbols.add(currentNonterminalSymbol);
					continue;
				}

				if (currentNonterminalSymbol == null) {
					throw new GrammarGenerationException("Production rule '%s' does not belong to any declared nonterminal!", line);
				}

				existingProductionRules = productionsMap.get(currentNonterminalSymbol);
				if (existingProductionRules == null) {
					existingProductionRules = new HashSet<>();
					productionsMap.put(currentNonterminalSymbol, existingProductionRules);
				}

				rhs = new ArrayList<>();
				line = line.trim();
				tokens = line.split("\\s+");
				for (String token : tokens) {
					if (token.isEmpty()) {
						continue;
					}

					Symbol resolvedToken = resolveSymbol(token, symbolResolutions);
					assert resolvedToken != null : "Could not resolve token.";
					usedSymbols.add(resolvedToken);
					rhs.add(resolvedToken);
				}

				rhs.trimToSize();
				productionRule = new ProductionRule(currentNonterminalSymbol, ImmutableList.copyOf(rhs), productions.size());
				if (!productions.contains(productionRule)) {
					productions.add(productionRule);
				}

				existingProductionRules.add(productionRule);
			}
		}

		numUnreachableSymbols = 0;
		Set<Symbol> unusedSymbols = new HashSet<>(SYMBOLS.values());
		unusedSymbols.removeAll(usedSymbols);
		unusedSymbols.stream()
			.filter(symbol -> !(symbol.getId() < 0))
			.forEachOrdered(symbol -> {
				numUnreachableSymbols++;
				LOGGER.warning(String.format("%s (%s) is unreachable", SYMBOLS.inverse().get(symbol), symbol));
			});
	}

	public void output() {
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
				if (entry.getValue().getId() < 0) {
					continue;
				} else if (!printingNonterminalSymbols && entry.getValue() instanceof NonterminalSymbol) {
					printingNonterminalSymbols = true;
					writer.write(String.format("%n----------------------------------------------------------------%n"));
					writer.write(String.format("%4s %s%n",
						"ID",
						"Nonterminal"
					));
				}

				String alternateValue = SYMBOLS.inverse().get(new TerminalSymbol(entry.getValue().getId()-Integer.MIN_VALUE));
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
			BiMap.Entry<NonterminalSymbol, Set<ProductionRule>>[] entries = PRODUCTION_RULES_MAP.entrySet().toArray(new BiMap.Entry[0]);
			Arrays.sort(entries, (Map.Entry<NonterminalSymbol, Set<ProductionRule>> o1, Map.Entry<NonterminalSymbol, Set<ProductionRule>> o2) -> o1.getKey().compareTo(o2.getKey()));

			writer.write(String.format("[%4s] %s%n",
				"ID",
				"Nonterminal: Production Rules"
			));

			for (BiMap.Entry<NonterminalSymbol, Set<ProductionRule>> entry : entries) {
				writer.write(String.format("[%4s] %s:%n",
					entry.getKey(),
					SYMBOLS.inverse().get(entry.getKey())
				));

				ProductionRule[] productionRules = entry.getValue().toArray(new ProductionRule[0]);
				Arrays.sort(productionRules);

				for (ProductionRule p : productionRules) {
					writer.write(String.format("\t%3d\t", p.getId()));
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
