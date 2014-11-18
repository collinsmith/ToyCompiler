package com.gmail.collinsmith70.toycompiler.lexer;

public class Token {
	private final TokenType TYPE;
	private final Object VALUE;

	public Token(TokenType type) {
		this(type, null);
	}

	public Token(TokenType type, Object value) {
		this.TYPE = type;
		this.VALUE = value;
	}

	public TokenType getTokenType() {
		return TYPE;
	}

	public Object getValue() {
		return VALUE;
	}
}
