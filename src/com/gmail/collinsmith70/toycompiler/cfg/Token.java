package com.gmail.collinsmith70.toycompiler.cfg;

public interface Token {
	Lexeme getLexeme();
	Object getValue();
	String toString();
}
