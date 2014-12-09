package com.gmail.collinsmith70.toycompiler.bnf;

public interface Lexeme {
	String getName();
	String getRegex();
	int getId();
	Token getDefaultToken();
}
