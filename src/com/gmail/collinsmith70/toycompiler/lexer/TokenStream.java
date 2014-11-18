package com.gmail.collinsmith70.toycompiler.lexer;

import java.io.Reader;

public class TokenStream {
	private final Reader r;
	
	private Token peek;
	
	public TokenStream(Reader r) {
		if (!r.markSupported()) {
			throw new IllegalArgumentException("Reader must support marking for lexical analyzing");
		}
		
		this.r = r;
	}
	
	public boolean hasNext() {
		return peek() != null;
	}
	
	public Token peek() {
		if (peek == null) {
			peek = next();
		}
		
		return peek;
	}
	
	public Token next() {
		//...
	}
}
