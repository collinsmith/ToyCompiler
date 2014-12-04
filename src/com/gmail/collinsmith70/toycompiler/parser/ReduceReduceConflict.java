package com.gmail.collinsmith70.toycompiler.parser;

public class ReduceReduceConflict extends Conflict {
	public ReduceReduceConflict(State<?> s) {
		super(s);
	}

	@Override
	public String toString() {
		return "reduce-reduce";
	}
}
