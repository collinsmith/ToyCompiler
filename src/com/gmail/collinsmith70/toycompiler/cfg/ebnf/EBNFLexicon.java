package com.gmail.collinsmith70.toycompiler.cfg.ebnf;

import com.gmail.collinsmith70.toycompiler.cfg.Lexeme;
import com.gmail.collinsmith70.toycompiler.cfg.Token;
import java.util.Objects;
import java.util.regex.Pattern;

public enum EBNFLexicon implements Lexeme {
	// Terminal Symbols
	_concatenateSymbol("\\,"),
	_definingSymbol("\\="),
	_definitionSeparatorSymbol("(\\|)|(\\/)|(\\!)"),
	//_endCommentSymbol("\\Q*)\\E"), // Ignored by scanner, never returned
	_endGroupSymbol("\\)"),
	_endOptionSymbol("(\\])|(\\Q/)\\E)"),
	_endRepeatSymbol("(\\})|(\\Q:)\\E)"),
	_exceptSymbol("\\-"),
	//_firstQuoteSymbol("\\'"),
	_repetitionSymbol("\\*"),
	//_secondQuoteSymbol("\\\""),
	//_specialSequenceSymbol("\\?"),
	//_startCommentSymbol("\\Q(*\\E"), // Ignored by scanner, never returned
	_startGroupSymbol("\\("),
	_startOptionSymbol("(\\[)|(\\Q(/\\E)"),
	_startRepeatSymbol("(\\{)|(\\Q(:\\E)"),
	_terminatorSymbol("\\;|\\."),

	// Tokens
	_terminalString("(\"[^\"]+\")|('[^']+')"),
	_metaIdentifier("[a-zA-Z][a-zA-Z0-9]*"),
	//_bracketedTextualComment("\\Q(*\\E.*\\Q*)\\E"), // Ignored by scanner, never returned
	_specialSequence("\\?.*\\?"),
	_integer("[0-9]+"),
	;

	private final Pattern PATTERN;

	private EBNFLexicon(String regex) {
		this.PATTERN = Pattern.compile(Objects.requireNonNull(regex), Pattern.UNICODE_CHARACTER_CLASS);
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
