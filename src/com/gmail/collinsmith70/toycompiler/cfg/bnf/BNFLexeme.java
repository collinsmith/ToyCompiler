package com.gmail.collinsmith70.toycompiler.cfg.bnf;

import com.gmail.collinsmith70.toycompiler.cfg.Lexeme;
import com.gmail.collinsmith70.toycompiler.cfg.Token;
import java.util.Objects;
import java.util.regex.Pattern;

public enum BNFLexeme implements Lexeme {
	_terminalSymbol		("[^<>]+"),
	_nonterminalSymbol	("\\<\\w+('|`)*\\>"),
	_assignop			("::="),
	_alternation		("\\|"),
	;

	private final Pattern PATTERN;
	private final Token DEFAULT_TOKEN;

	private BNFLexeme(String regex) {
		this.PATTERN = Pattern.compile(Objects.requireNonNull(regex), Pattern.UNICODE_CHARACTER_CLASS);
		this.DEFAULT_TOKEN = new Token(this);
	}

	@Override
	public String getName() {
		return name();
	}

	@Override
	public int getId() {
		return ordinal()+1;
	}

	@Override
	public Pattern getPattern() {
		return PATTERN;
	}

	@Override
	public Token getDefaultToken() {
		return DEFAULT_TOKEN;
	}
}
