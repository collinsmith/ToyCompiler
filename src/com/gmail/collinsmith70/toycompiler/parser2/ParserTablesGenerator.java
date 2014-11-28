package com.gmail.collinsmith70.toycompiler.parser2;

public interface ParserTablesGenerator<T> {
	T generateParserTables(Grammar g);
}
