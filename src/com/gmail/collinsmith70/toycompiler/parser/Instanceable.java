package com.gmail.collinsmith70.toycompiler.parser;

public interface Instanceable<E extends Instanceable<E>> {
	E next();
	boolean hasNext(int n);
	Symbol currentSymbol();
	Symbol lookahead(int n);

	default boolean hasNext() {
		return hasNext(1);
	}

	default Symbol peekNextSymbol() {
		return lookahead(1);
	}
}
