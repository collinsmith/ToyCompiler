package com.gmail.collinsmith70.toycompiler.parser;

import com.gmail.collinsmith70.toycompiler.lexer.TokenStream;
import java.io.IOException;
import java.io.Writer;

public interface Parser {
	void parse(TokenStream tokenStream, Writer writer) throws IOException;
}
