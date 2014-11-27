package com.gmail.collinsmith70.toycompiler.parser2;

import com.gmail.collinsmith70.toycompiler.lexer.TokenType;
import com.gmail.collinsmith70.toycompiler.lexer.ToyTokenTypes;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class Grammar {
	private static final Pattern NONTERMINAL_PATTERN = Pattern.compile("[A-Z]\\w*:$");

	private final ImmutableBiMap<String, Symbol> SYMBOLS;
	private final ImmutableSet<ProductionRule> PRODUCTIONS;
	private final ImmutableBiMap<NonterminalSymbol, ImmutableSet<ProductionRule>> NONTERMINAL;

	private final Logger LOGGER;

	private final String GRAMMAR_NAME;

	private int numTerminalSymbols;
	private int numNonterminalSymbols;
	private int numUnreachableSymbols;

	private NonterminalSymbol initialNonterminal;

	private Grammar(Path p, Charset c) throws IOException {
		assert p != null;

		this.numTerminalSymbols = Integer.MIN_VALUE;
		this.numNonterminalSymbols = Integer.MIN_VALUE;
		numUnreachableSymbols = Integer.MIN_VALUE;

		String grammarName = p.getFileName().toString();
		grammarName = grammarName.substring(0, grammarName.lastIndexOf('.'));
		GRAMMAR_NAME = grammarName;

		LOGGER = Logger.getLogger(this.getClass().getName());
		LOGGER.addHandler(new ConsoleHandler());
		LOGGER.addHandler(new FileHandler(Paths.get(".", "logs", GRAMMAR_NAME + ".xml").toString(), true));
		LOGGER.setLevel(Level.ALL);

		LOGGER.info("Generating symbols table...");
		long dt = System.currentTimeMillis();
		this.SYMBOLS = createSymbolsTable(p, c);
		LOGGER.info(String.format("Symbols table generated in %dms; %d symbols (%d terminal symbols, %d nonterminal symbols)",
			System.currentTimeMillis()-dt,
			numTerminalSymbols+numNonterminalSymbols,
			numTerminalSymbols,
			numNonterminalSymbols
		));

		LOGGER.info("Generating productions table...");
		dt = System.currentTimeMillis();
		ArrayList<ProductionRule> productions = new ArrayList<>();
		this.NONTERMINAL = createProductionsTable(p, c, productions);
		this.PRODUCTIONS = ImmutableSet.copyOf(productions);
		LOGGER.info(String.format("Productions table and list created in %dms; %d productions (%d unreachable symbols)",
			System.currentTimeMillis()-dt,
			this.PRODUCTIONS.size(),
			numUnreachableSymbols
		));
	}

	private ImmutableBiMap<String, Symbol> createSymbolsTable(Path p, Charset c) throws IOException {
		numTerminalSymbols = numNonterminalSymbols = 0;
		BiMap<String, Symbol> symbols = HashBiMap.create();
		for (TokenType.DefaultTokenTypes k : TokenType.DefaultTokenTypes.values()) {
			symbols.put(k.name(), new TerminalSymbol(k.getId()));
			if (k.isLiteral()) {
				symbols.put(k.getRegex(), new TerminalSymbol(Integer.MIN_VALUE+k.getId()));
			}

			numTerminalSymbols++;
		}

		for (ToyTokenTypes k : ToyTokenTypes.values()) {
			symbols.put(k.name(), new TerminalSymbol(k.getId()));
			if (k.isLiteral()) {
				symbols.put(k.getRegex(), new TerminalSymbol(Integer.MIN_VALUE+k.getId()));
			}

			numTerminalSymbols++;
		}

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
				numNonterminalSymbols++;
			}
		}

		return ImmutableBiMap.copyOf(symbols);
	}

	private ImmutableBiMap<NonterminalSymbol, ImmutableSet<ProductionRule>> createProductionsTable(Path p, Charset c, ArrayList<ProductionRule> productions) throws IOException {
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

		if (resolved.getId() < 0) {
			/**
			 * TODO: generating objects that should not be needed, should
			 * look into adding multiple IDs for a single symbol to
			 * support up to n alternatives.
			 */
			return new Symbol(resolved.getId() - Integer.MIN_VALUE);
		}

		return resolved;
	}

	/*public final boolean isNonterminal(Symbol s) {
		return numTerminalSymbols <= s.getId();
	}*/

	public static Grammar generate(Path p, Charset c) throws IOException {
		return new Grammar(Objects.requireNonNull(p), Objects.requireNonNull(c));
	}
}
