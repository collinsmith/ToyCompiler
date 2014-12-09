package com.gmail.collinsmith70.toycompiler.bnf;

public class Token {
	private final Lexeme LEXEME;
	private final Object VALUE;

	public Token(Lexeme lexeme) {
		this(lexeme, null);
	}

	public Token(Lexeme lexeme, Object value) {
		this.LEXEME = lexeme;
		this.VALUE = value;
	}

	public Lexeme getLexeme() {
		return LEXEME;
	}

	public Object getValue() {
		return VALUE;
	}
}
