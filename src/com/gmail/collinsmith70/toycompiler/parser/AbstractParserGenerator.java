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
import java.util.Map;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public abstract class AbstractParserGenerator {
	protected static final Pattern NONTERMINAL_DEF_PATTERN = Pattern.compile("[A-Z]\\w*:$");
	protected static final Charset CHARSET = Charset.forName("US-ASCII");

	protected final ImmutableList<Production> PRODUCTIONS;
	protected final ImmutableBiMap<String, Integer> SYMBOLS;
	protected final ImmutableBiMap<Integer, ImmutableSet<Production>> NONTERMINALS;

	private final Logger LOGGER;

	private int numTerminals;
	private int numNonterminals;
	private int numUnreachableSymbols;

	private int initialNonterminal;

	public AbstractParserGenerator(Path p) throws IOException {
		Preconditions.checkNotNull(p);

		numTerminals = Integer.MIN_VALUE;
		numNonterminals = Integer.MIN_VALUE;
		numUnreachableSymbols = Integer.MIN_VALUE;

		initialNonterminal = Integer.MIN_VALUE;

		LOGGER = Logger.getLogger(this.getClass().getName());
		LOGGER.addHandler(new ConsoleHandler());
		LOGGER.addHandler(new FileHandler(Paths.get(".", "logs", "parsergenerator.txt").toString(), true));
		LOGGER.setLevel(Level.ALL);

		LOGGER.info("Creating symbols table...");
		long dt = System.currentTimeMillis();
		this.SYMBOLS = createSymbolsTable(p);
		LOGGER.info(String.format("Symbols table created in %dms; %d symbols (%d terminals, %d nonterminals)",
			System.currentTimeMillis()-dt,
			numTerminals+numNonterminals,
			numTerminals,
			numNonterminals
		));

		LOGGER.info("Creating productions table and list...");
		dt = System.currentTimeMillis();
		ArrayList<Production> productions = new ArrayList<>();
		this.NONTERMINALS = createProductionsTable(p, productions);
		this.PRODUCTIONS = ImmutableList.copyOf(productions);
		LOGGER.info(String.format("Productions table and list created in %dms; %d productions (%d unreachable symbols)",
			System.currentTimeMillis()-dt,
			this.PRODUCTIONS.size(),
			numUnreachableSymbols
		));
	}

	protected Logger getLogger() {
		return LOGGER;
	}

	private ImmutableBiMap<String, Integer> createSymbolsTable(Path p) throws IOException {
		numTerminals = numNonterminals = 0;
		BiMap<String, Integer> symbols = HashBiMap.create();
		for (TokenType.DefaultTokenType k : TokenType.DefaultTokenType.values()) {
			symbols.put(k.name(), k.getId());
			if (k.isLiteral()) {
				symbols.put(k.getRegex(), Integer.MIN_VALUE+k.getId());
			}

			numTerminals++;
		}

		for (ToyTokenTypes k : ToyTokenTypes.values()) {
			symbols.put(k.name(), k.getId());
			if (k.isLiteral()) {
				symbols.put(k.getRegex(), Integer.MIN_VALUE+k.getId());
			}

			numTerminals++;
		}

		try (BufferedReader br = Files.newBufferedReader(p, CHARSET)) {
			int id;
			String line;
			while ((line = br.readLine()) != null) {
				if (!NONTERMINAL_DEF_PATTERN.matcher(line).matches()) {
					continue;
				}

				id = numTerminals+numNonterminals;
				if (initialNonterminal == Integer.MIN_VALUE) {
					initialNonterminal = id;
				}

				line = line.substring(0, line.length()-1);
				//System.out.println(line + ", " + id);
				//System.out.println(symbols.inverse().get(id));
				symbols.put(line, id);
				numNonterminals++;
			}
		}

		return ImmutableBiMap.copyOf(symbols);
	}

	private ImmutableBiMap<Integer, ImmutableSet<Production>> createProductionsTable(Path p, ArrayList<Production> productions) throws IOException {
		BiMap<Integer, Set<Production>> nonterminals = HashBiMap.create();
		Set<Integer> usedSymbols = new HashSet<>();
		try (BufferedReader br = Files.newBufferedReader(p, CHARSET)) {
			Production production;
			Set<Production> nonterminalProductions;
			Integer currentNonterminal = null;

			int id;
			String line;
			String[] tokens;
			while ((line = br.readLine()) != null) {
				if (line.isEmpty()) {
					continue;
				}

				if (NONTERMINAL_DEF_PATTERN.matcher(line).matches()) {
					line = line.substring(0, line.length()-1);
					currentNonterminal = SYMBOLS.get(line);
					if (currentNonterminal == null) {
						throw new ProductionException("Nonterminal %s is not within the set of symbols!",
							line
						);
					} else if (currentNonterminal < numTerminals) {
						throw new ProductionException("Terminal %s cannot be declared as a production nonterminal!",
							SYMBOLS.inverse().get(currentNonterminal)
						);
					}

					usedSymbols.add(currentNonterminal);
					continue;
				}

				if (currentNonterminal == null) {
					throw new ProductionException("Production %s does not belong to any declared nonterminal!",
						line
					);
				}

				nonterminalProductions = nonterminals.get(currentNonterminal);
				if (nonterminalProductions == null) {
					nonterminalProductions = new HashSet<>();
					nonterminals.put(currentNonterminal, nonterminalProductions);
				}

				ArrayList<Integer> productionList = new ArrayList<>();

				line = line.trim();
				tokens = line.split("\\s+");
				for (String token : tokens) {
					if (token.isEmpty()) {
						continue;
					}

					int tokenId = resolveSymbol(token);
					usedSymbols.add(tokenId);
					productionList.add(tokenId);
				}

				productionList.trimToSize();
				production = new Production(currentNonterminal, ImmutableList.copyOf(productionList));
				if (!productions.contains(production)) {
					productions.add(production);
				}

				nonterminalProductions.add(production);
			}
		}

		productions.trimToSize();

		numUnreachableSymbols = 0;
		Set<Integer> unusedSymbols = new HashSet<>(SYMBOLS.values());
		unusedSymbols.removeAll(usedSymbols);
		unusedSymbols.stream()
			.filter((symbol) -> !(symbol < 0)).map((symbol) -> {
				numUnreachableSymbols++;
				return symbol;
			})
			.forEach((symbol) -> System.out.format("%s (%d) is unreachable%n", SYMBOLS.inverse().get(symbol), symbol));

		BiMap<Integer, ImmutableSet<Production>> immutableNonterminals = HashBiMap.create();
		nonterminals.entrySet().stream()
			.forEach((entry) -> immutableNonterminals.put(entry.getKey(), ImmutableSet.copyOf(entry.getValue())));

		return ImmutableBiMap.copyOf(immutableNonterminals);
	}

	protected final int resolveSymbol(String token) {
		Preconditions.checkNotNull(token);
		Integer tokenId = SYMBOLS.get(token);
		if (tokenId == null) {
			throw new ProductionException("Symbol %s is undefined!",
				token
			);
		}

		if (tokenId < 0) {
			tokenId -= Integer.MIN_VALUE;
		}

		return tokenId;
	}

	protected final boolean isNonterminal(int tokenId) {
		return numTerminals <= tokenId;
	}

	public final int getNumNonterminals() {
		return numNonterminals;
	}

	public final int getNumTerminals() {
		return numTerminals;
	}

	public final int getNumUnreachableSymbols() {
		return numUnreachableSymbols;
	}

	public final int getInitialNonterminal() {
		return initialNonterminal;
	}

	protected final void setInitialNonterminal(int nonterminal) {
		Preconditions.checkArgument(isNonterminal(nonterminal), "Invalid nonterminal ID given.");
		initialNonterminal = nonterminal;
	}

	public void outputCFG() {
		outputSymbols();
		outputProductions();
	}

	protected void outputSymbols() {
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(".", "output", "toy.cfg.symbols.txt"), CHARSET, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
			BiMap.Entry<String, Integer>[] entries = SYMBOLS.entrySet().toArray(new BiMap.Entry[0]);
			Arrays.sort(entries, (Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) -> o1.getValue().compareTo(o2.getValue()));

			writer.write(String.format("%3s %-20s %s%n",
				"ID",
				"Token Type",
				"Alternative (Optional)"
			));

			for (BiMap.Entry<String, Integer> entry : entries) {
				if (entry.getValue() < 0) {
					continue;
				} else if (entry.getValue() == numTerminals) {
					writer.write(String.format("%n----------------------------------------------------------------%n"));
					writer.write(String.format("%3s %s%n",
						"ID",
						"Nonterminal"
					));
				}

				String alternateValue = SYMBOLS.inverse().get(entry.getValue()-Integer.MIN_VALUE);
				writer.write(String.format("%3d %-20s %s%n",
					entry.getValue(),
					entry.getKey(),
					Strings.nullToEmpty(alternateValue)
				));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void outputProductions() {
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(".", "output", "toy.cfg.productions.txt"), CHARSET, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
			BiMap.Entry<Integer, Set<Production>>[] entries = NONTERMINALS.entrySet().toArray(new BiMap.Entry[0]);
			Arrays.sort(entries, (Map.Entry<Integer, Set<Production>> o1, Map.Entry<Integer, Set<Production>> o2) -> o1.getKey().compareTo(o2.getKey()));

			writer.write(String.format("%-4s %-16s%n",
				"ID",
				"Nonterminal"
			));

			for (BiMap.Entry<Integer, Set<Production>> entry : entries) {
				writer.write(String.format("[%d] %s:%n",
					entry.getKey(),
					SYMBOLS.inverse().get(entry.getKey())
				));

				Production[] productions = entry.getValue().toArray(new Production[0]);
				Arrays.sort(productions, (Production o1, Production o2) -> PRODUCTIONS.indexOf(o1) - PRODUCTIONS.indexOf(o2));

				for (Production p : productions) {
					writer.write(String.format("\t%3d\t", PRODUCTIONS.indexOf(p)));
					for (Integer i : p) {
						writer.write(String.format("%s[%d] ", SYMBOLS.inverse().get(i), i));
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
