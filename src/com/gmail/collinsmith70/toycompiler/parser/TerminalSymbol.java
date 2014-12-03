package com.gmail.collinsmith70.toycompiler.parser;

public class TerminalSymbol extends Symbol {
	public static final TerminalSymbol EMPTY_STRING = new TerminalSymbol(0);

	public TerminalSymbol(int id) {
		super(id);
	}

	/*@Override
	public String toString() {
		return "_" + super.toString();
	}*/
}
