package com.gmail.collinsmith70.toycompiler.parser;

public class ShiftReduceConflict extends Conflict {
	public ShiftReduceConflict(State<?> s) {
		super(s);
	}

	@Override
	public String toString() {
		return "shift-reduce";
	}
}
