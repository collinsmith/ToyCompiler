package com.gmail.collinsmith70.toycompiler.parser;

import com.gmail.collinsmith70.toycompiler.lexer.TokenStream;
import java.io.IOException;
import java.io.Writer;

public interface Parser {
	boolean parse(TokenStream stream, Writer writer) throws IOException;
	
	default boolean parse(TokenStream stream) {
		try {
			return parse(stream, null);
		} catch (IOException e) {
		}
		
		throw new UnsupportedOperationException("Cannot parse states because an IOException was thrown!");
	}
}
