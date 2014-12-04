package com.gmail.collinsmith70.toycompiler.parser;

public class ReduceReduceConflict<E extends Instanceable<E>> extends Conflict<E> {
	public ReduceReduceConflict(State<E> s) {
		super(s);
	}

	@Override
	public String toString() {
		return "reduce-reduce";
	}
}
