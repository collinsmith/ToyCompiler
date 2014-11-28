package com.gmail.collinsmith70.toycompiler.parser2;

public interface ParserStatesGenerator<T> {
	T generateParserTables(Grammar g);
}
