package com.gmail.collinsmith70.toycompiler.cfg.bnf;

import com.gmail.collinsmith70.toycompiler.cfg.Lexeme;
import com.gmail.collinsmith70.toycompiler.cfg.Token;
import java.util.Objects;
import java.util.regex.Pattern;

public enum BNFLexicon implements Lexeme {
	// Terminal Symbols
	definitionSeparatorSymbol("\\|"),
	definingSymbol("::="),

	// Tokens
	terminalString("(\"[^\"]+\")|('[^']+')"),
	metaIdentifier("\\<\\w+('|`)*\\>"),
	;

	private final Pattern PATTERN;

	private BNFLexicon(String regex) {
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
	public Token createChild(Object value) {
		return new Token() {
			@Override
			public Lexeme getLexeme() {
				return BNFLexicon.this;
			}

			@Override
			public Object getValue() {
				return value;
			}

			@Override
			public String toString() {
				if (value == null) {
					return BNFLexicon.this.toString();
				}

				return BNFLexicon.this.toString() + "[" + value.toString() + "]";
			}
		};
	}

	@Override
	public Lexeme getLexeme() {
		return this;
	}

	@Override
	public Object getValue() {
		return null;
	}
}
