package com.gmail.collinsmith70.toycompiler.cfg.ebnf;

import com.gmail.collinsmith70.toycompiler.cfg.Lexeme;
import com.gmail.collinsmith70.toycompiler.cfg.Token;
import java.util.Objects;
import java.util.regex.Pattern;

public enum EBNFLexeme implements Lexeme {
	//_terminalSymbol("(\"[^\"]+\"|'[^\"]+')"),
	//_nonterminalSymbol("(\\w+('|`)*)|(\\<(\\w+('|`)*)*\\>)"),

	// EBNF Terminal Symbols
	letter("[a-zA-Z]"),
	decimalDigit("[0-9]"),
	terminalCharacter("\\w"),
	otherCharacter("\\s|[+_%@&#$<>\\^`~]"),

	concatenateSymbol("\\,"),
	definingSymbol("\\="),
	definitionSeparatorSymbol("(\\|)|(\\/)|(\\!)"),
	endCommentSymbol("\\Q*)\\E"),
	endGroupSymbol("\\)"),
	endOptionSymbol("(\\])|(\\Q/)\\E)"),
	endRepeatSymbol("(\\})|(\\Q:)\\E)"),
	exceptSymbol("\\-"),
	firstQuoteSymbol("\\'"),
	repetitionSymbol("\\*"),
	secondQuoteSymbol("\\\""),
	specialSequenceSymbol("\\?"),
	startCommentSymbol("\\Q(*\\E"),
	startGroupSymbol("\\("),
	startOptionSymbol("(\\[)|(\\Q(/\\E)"),
	startRepeatSymbol("(\\{)|(\\Q(:\\E)"),
	terminatorSymbol("\\;|\\."),
	;

	private final Pattern PATTERN;
	private final Token DEFAULT_TOKEN;

	private EBNFLexeme(String regex) {
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
