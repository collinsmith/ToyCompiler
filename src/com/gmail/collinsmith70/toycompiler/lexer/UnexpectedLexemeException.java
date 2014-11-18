package com.gmail.collinsmith70.toycompiler.lexer;

public class UnexpectedLexemeException extends RuntimeException {
	public UnexpectedLexemeException() {
		//...
	}

	public UnexpectedLexemeException(int line, String string) {
		super(String.format("Unexpected lexeme scanned on line %d, \"%s\"", line, string));
	}
}
