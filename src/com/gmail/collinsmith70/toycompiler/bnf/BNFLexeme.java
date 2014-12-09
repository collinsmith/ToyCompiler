package com.gmail.collinsmith70.toycompiler.bnf;

import com.google.common.base.Strings;

public enum BNFLexeme implements Lexeme {
	_terminalSymbol		("\\b(\\p{L}\\p{M}*)+\\b"),
	_nonterminalSymbol		("\\<(\\p{L}\\p{M}*)+'?\\>"),
	_assignop			("::="),
	_alternation		("|"),
	;

	// ( (\\p{L]\\p{M}*+ | \\p{N} | \\p{Pc})+ )

	private final String REGEX;
	private final Token DEFAULT_TOKEN;

	private BNFLexeme(String regex) {
		this.REGEX = Strings.nullToEmpty(regex);
		this.DEFAULT_TOKEN = new Token(this);
	}

	@Override
	public String getName() {
		return name();
	}

	@Override
	public int getId() {
		return ordinal();
	}

	@Override
	public String getRegex() {
		return REGEX;
	}

	@Override
	public Token getDefaultToken() {
		return DEFAULT_TOKEN;
	}
}
