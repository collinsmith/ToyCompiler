package com.gmail.collinsmith70.toycompiler.cfg;

import java.util.Objects;

public class Token {
	private final Lexeme LEXEME;
	private final Object VALUE;

	public Token(Lexeme lexeme) {
		this(lexeme, null);
	}

	public Token(Lexeme lexeme, Object value) {
		this.LEXEME = Objects.requireNonNull(lexeme);
		this.VALUE = value;
	}

	public Lexeme getLexeme() {
		return LEXEME;
	}

	public Object getValue() {
		return VALUE;
	}

	@Override
	public String toString() {
		if (VALUE == null) {
			return LEXEME.toString();
		}

		return LEXEME.toString() + "[" + VALUE.toString() + "]";
	}
}
