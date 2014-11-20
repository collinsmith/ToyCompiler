package com.gmail.collinsmith70.toycompiler.lexer;

/**
 * This class represents an exception that is thrown whenever a {@link Scanner}
 * detects an unsupported lexeme (symbol or word).
 *
 * @author Collin Smith <strong>collinsmith70@gmail.com</strong>
 */
public class UnexpectedLexemeException extends RuntimeException {
	/**
	 * Constructs an {@code UnexpectedLexemeException} which will output the
	 * given line and lexeme.
	 *
	 * @param line line number within the file that this lexeme exists
	 * @param lexeme lexeme that is not supported
	 */
	public UnexpectedLexemeException(int line, String lexeme) {
		super(String.format("Unexpected lexeme scanned on line %d, \"%s\"", line, lexeme));
	}
}
