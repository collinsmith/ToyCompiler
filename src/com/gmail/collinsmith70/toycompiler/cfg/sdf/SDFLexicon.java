package com.gmail.collinsmith70.toycompiler.cfg.sdf;

import com.gmail.collinsmith70.toycompiler.cfg.Lexeme;
import com.gmail.collinsmith70.toycompiler.cfg.Token;
import java.util.Objects;
import java.util.regex.Pattern;

public enum SDFLexicon implements Lexeme, Token {
	// Terminal Symbols
	_definingSymbol("\\:"),

	// Tokens
	_metaIdentifier("[a-zA-Z][a-zA-Z0-9]*"),
	_terminalString("\\_[a-zA-Z0-9]+"),
	;

	private final Pattern PATTERN;

	private SDFLexicon(String regex) {
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
				return SDFLexicon.this;
			}

			@Override
			public Object getValue() {
				return value;
			}

			@Override
			public String toString() {
				if (value == null) {
					return SDFLexicon.this.toString();
				}

				return SDFLexicon.this.toString() + "[" + value.toString() + "]";
			}
		};
	}

	@Override
	public String toString() {
		return getName();
	}
}
