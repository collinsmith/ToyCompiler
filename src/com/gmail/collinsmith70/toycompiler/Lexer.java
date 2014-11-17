package com.gmail.collinsmith70.toycompiler;

/**
 * A lexer is a scanner which parses a byte stream and generates a stream of
 * tokens.
 *
 * @author Collin Smith <strong>collinsmith70@gmail.com</strong>
 */
public interface Lexer {
	boolean hasNext();
	Token peek();
	Token next();
}
