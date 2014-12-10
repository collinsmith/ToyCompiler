package com.gmail.collinsmith70.toycompiler.cfg;

import java.util.Objects;

public class UndefinedLexemeException extends RuntimeException {
	private final String LEXEME;

	public UndefinedLexemeException() {
		super("Unexpected lexeme scanned");
		this.LEXEME = "";
	}

	public UndefinedLexemeException(String lexeme) {
		super(String.format("Unexpected lexeme scanned \"%s\"", lexeme));
		this.LEXEME = Objects.requireNonNull(lexeme);
	}

	public String getLexeme() {
		return LEXEME;
	}
}
