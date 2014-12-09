package com.gmail.collinsmith70.toycompiler.bnf;

public class UndefinedLexemeException extends RuntimeException {
	private final String LEXEME;
	private final int LINE;

	public UndefinedLexemeException() {
		super("Unexpected lexeme scanned");
		this.LEXEME = "";
		this.LINE = Integer.MIN_VALUE;
	}
	
	public UndefinedLexemeException(String lexeme) {
		super(String.format("Unexpected lexeme scanned \"%s\"", lexeme));
		this.LEXEME = lexeme;
		this.LINE = Integer.MIN_VALUE;
	}
	
	public UndefinedLexemeException(String lexeme, int line) {
		super(String.format("Unexpected lexeme scanned on line %d, \"%s\"", line, lexeme));
		this.LEXEME = lexeme;
		this.LINE = line;
	}
	
	public String getLexeme() {
		return LEXEME;
	}
	
	public int getLineNumber() {
		return LINE;
	}
}
