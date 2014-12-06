package com.gmail.collinsmith70.toycompiler.lexer;

import java.util.Objects;

public class Token {
	private final TokenType TYPE;
	private final Object VALUE;

	public Token(TokenType type) {
		this(type, null);
	}

	public Token(TokenType type, Object value) {
		this.TYPE = Objects.requireNonNull(type);
		this.VALUE = value;
	}

	public TokenType getTokenType() {
		return TYPE;
	}

	public Object getValue() {
		return VALUE;
	}

	@Override
	public String toString() {
		return VALUE == null ? TYPE.name() : String.format("%s[%s]", TYPE.name(), VALUE);
	}
}
