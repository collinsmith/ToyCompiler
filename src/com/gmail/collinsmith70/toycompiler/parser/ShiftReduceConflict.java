package com.gmail.collinsmith70.toycompiler.parser;

public class ShiftReduceConflict<E extends Instanceable<E>> extends Conflict<E> {
	public ShiftReduceConflict(State<E> s) {
		super(s);
	}

	@Override
	public String toString() {
		return "shift-reduce";
	}
}
