package com.gmail.collinsmith70.toycompiler.parser2;

public class ReduceReduceConflict extends Conflict {
	public ReduceReduceConflict(State s) {
		super(s);
	}

	@Override
	public String toString() {
		return "reduce-reduce";
	}
}
