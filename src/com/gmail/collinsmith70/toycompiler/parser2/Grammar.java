package com.gmail.collinsmith70.toycompiler.parser2;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import java.nio.file.Path;

public class Grammar {
	// TODO: not needed, can retrieve with .values()
	private final ImmutableSet<ProductionRule> PRODUCTIONS;
	private final ImmutableBiMap<Symbol, ImmutableSet<ProductionRule>> NONTERMINAL;
	
	private int numTerminalSymbols;
	private int numNonterminalSymbols;
	
	private Grammar(Path p) {
		this.numTerminalSymbols = Integer.MIN_VALUE;
		this.numNonterminalSymbols = Integer.MIN_VALUE;
	}
	
	public static Grammar build(Path p) {
		return new Grammar(p);
	}
}
