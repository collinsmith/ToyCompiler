package com.gmail.collinsmith70.toycompiler.cfg;

import java.io.IOException;
import java.io.Reader;

public class TokenStream {
	private final Scanner SCANNER;
	private final Reader READER;

	private Token peek;

	public TokenStream(Scanner scanner, Reader reader) {
		if (scanner.requiresMark() && !reader.markSupported()) {
			throw new IllegalArgumentException("Reader must be bufferable for lexical analyzing");
		}

		this.SCANNER = scanner;
		this.READER = reader;
	}

	public boolean hasNext() {
		try {
			return peek().getLexeme() != Lexeme._eof;
		} catch (IOException e) {
			return false;
		}
	}

	public Token peek() throws IOException {
		if (peek == null) {
			peek = next();
		}

		return peek;
	}

	public Token next() throws IOException {
		if (peek != null) {
			Token next = peek;
			peek = null;
			return next;
		}

		return SCANNER.next(READER);
	}
}
