package com.gmail.collinsmith70.toycompiler.lexer;

import java.io.LineNumberReader;
import java.io.Reader;

public class TokenStream {
	private final Scanner SCANNER;
	private final LineNumberReader READER;

	private Token peek;

	public TokenStream(Scanner scanner, Reader reader) {
		if (!reader.markSupported()) {
			throw new IllegalArgumentException("Reader must support marking for lexical analyzing");
		}

		this.SCANNER = scanner;
		this.READER = new LineNumberReader(reader);
		READER.setLineNumber(1);
	}

	public boolean hasNext() {
		return peek().getTokenType() != TokenType.DefaultTokenType._eof;
	}

	public Token peek() {
		if (peek == null) {
			peek = next();
		}

		return peek;
	}

	public Token next() {
		if (peek != null) {
			Token next = peek;
			peek = null;
			return next;
		}

		return SCANNER.next(READER);
	}
}
