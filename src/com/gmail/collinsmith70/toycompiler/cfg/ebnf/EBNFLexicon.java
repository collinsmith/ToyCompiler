package com.gmail.collinsmith70.toycompiler.cfg.ebnf;

import com.gmail.collinsmith70.toycompiler.cfg.Lexeme;
import com.gmail.collinsmith70.toycompiler.cfg.Token;
import java.util.Objects;
import java.util.regex.Pattern;

public enum EBNFLexicon implements Lexeme, Token {
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

	// Tokens
	terminalString("(\"[^\"]+\")|('[^']+')"),
	metaIdentifier("[a-zA-Z][a-zA-Z0-9]*"),
	//bracketedTextualComment("\\Q(*\\E.*\\Q*)\\E"), // Ignored by scanner, never returned
	specialSequence("\\?.*\\?"),
	integer("[0-9]+"),
	;

	private final Pattern PATTERN;
	//private final Token DEFAULT_TOKEN;

	private EBNFLexicon(String regex) {
		this.PATTERN = Pattern.compile(Objects.requireNonNull(regex), Pattern.UNICODE_CHARACTER_CLASS);
		//this.DEFAULT_TOKEN = new Token(this);
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

	/*@Override
	public Token getDefaultToken() {
		return DEFAULT_TOKEN;
	}*/

	@Override
	public Lexeme getLexeme() {
		return this;
	}

	@Override
	public Object getValue() {
		return null;
	}

	@Override
	public Token createChild(Object value) {
		return new Token() {
			@Override
			public Lexeme getLexeme() {
				return EBNFLexicon.this;
			}

			@Override
			public Object getValue() {
				return value;
			}

			@Override
			public String toString() {
				if (value == null) {
					return EBNFLexicon.this.toString();
				}

				return EBNFLexicon.this.toString() + "[" + value.toString() + "]";
			}
		};
	}

	@Override
	public String toString() {
		return getName();
	}
}
