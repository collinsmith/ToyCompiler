package com.gmail.collinsmith70.toycompiler.parser;

public class ReduceReduceConflict<E extends Instanceable<E>> extends Conflict<E> {
	private final Symbol LOOKAHEAD;
	public ReduceReduceConflict(State<E> s, Symbol lookahead) {
		super(s);
		this.LOOKAHEAD = lookahead;
	}

	public Symbol getLookahead() {
		return LOOKAHEAD;
	}

	@Override
	public String toString() {
		return "reduce-reduce";
	}
}
