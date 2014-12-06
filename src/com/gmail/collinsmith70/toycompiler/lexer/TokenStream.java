package com.gmail.collinsmith70.toycompiler.lexer;

import java.io.LineNumberReader;
import java.io.Reader;

public class TokenStream {
	private final Scanner SCANNER;
	private final LineNumberReader READER;
	private final boolean IGNORE_EOL_TOKEN;

	private Token peek;

	public TokenStream(Scanner scanner, Reader reader) {
		this(scanner, reader, true);
	}
	
	public TokenStream(Scanner scanner, Reader reader, boolean ignoreEOLToken) {
		if (!reader.markSupported()) {
			throw new IllegalArgumentException("Reader must support marking for lexical analyzing");
		}

		this.SCANNER = scanner;
		this.READER = new LineNumberReader(reader);
		READER.setLineNumber(1);
		this.IGNORE_EOL_TOKEN = ignoreEOLToken;
	}

	public boolean hasNext() {
		return peek().getTokenType() != TokenType.DefaultTokenTypes._eof;
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

		if (IGNORE_EOL_TOKEN) {
			Token next;
			while ((next = SCANNER.next(READER)).getTokenType() == TokenType.DefaultTokenTypes._eol) {}
			return next;
		}
		
		return SCANNER.next(READER);
	}
}
