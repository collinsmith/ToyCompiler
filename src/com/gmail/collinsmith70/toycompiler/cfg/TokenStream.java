package com.gmail.collinsmith70.toycompiler.cfg;

import java.io.Reader;

public class TokenStream {
	private final Scanner SCANNER;
	private final Reader READER;

	private Token peek;

	public TokenStream(Scanner scanner, Reader reader) {
		if (scanner.requiresMark() && !reader.markSupported()) {
			throw new IllegalArgumentException("Reader must support marking for lexical analyzing");
		}

		this.SCANNER = scanner;
		this.READER = reader;
	}

	public boolean hasNext() {
		return peek().getLexeme() != Lexeme._eof;
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
