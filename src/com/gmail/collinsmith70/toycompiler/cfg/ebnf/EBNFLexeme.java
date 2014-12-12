package com.gmail.collinsmith70.toycompiler.cfg.ebnf;

import com.gmail.collinsmith70.toycompiler.cfg.Lexeme;
import com.gmail.collinsmith70.toycompiler.cfg.Token;
import java.util.Objects;
import java.util.regex.Pattern;

public enum EBNFLexeme implements Lexeme {
	// Terminal Symbols
	concatenateSymbol("\\,"),
	definingSymbol("\\="),
	definitionSeparatorSymbol("(\\|)|(\\/)|(\\!)"),
	//endCommentSymbol("\\Q*)\\E"), // Ignored by scanner, never returned
	endGroupSymbol("\\)"),
	endOptionSymbol("(\\])|(\\Q/)\\E)"),
	endRepeatSymbol("(\\})|(\\Q:)\\E)"),
	exceptSymbol("\\-"),
	firstQuoteSymbol("\\'"),
	repetitionSymbol("\\*"),
	secondQuoteSymbol("\\\""),
	specialSequenceSymbol("\\?"),
	//startCommentSymbol("\\Q(*\\E"), // Ignored by scanner, never returned
	startGroupSymbol("\\("),
	startOptionSymbol("(\\[)|(\\Q(/\\E)"),
	startRepeatSymbol("(\\{)|(\\Q(:\\E)"),
	terminatorSymbol("\\;|\\."),

	// Optimized Productions
	terminalString("(\"[^\"]+\")|('[^']+')"),
	metaIdentifier("[a-zA-Z][a-zA-Z0-9]*"),
	//bracketedTextualComment("\\Q(*\\E.*\\Q*)\\E"), // Ignored by scanner, never returned
	specialSequence("\\?.*\\?"),
	integer("[0-9]+"),
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
