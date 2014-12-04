package com.gmail.collinsmith70.toycompiler.parser;

import java.io.PrintStream;

public abstract class Conflict {
	@Override
	public abstract String toString();

	private final State<?> STATE;

	public Conflict(State<?> s) {
		this.STATE = s;
	}

	public State<?> getState() {
		return STATE;
	}

	public void dump(PrintStream stream) {
		stream.format("State %d has a %s conflict:%n", STATE.getId(), toString());
		// TODO: Implement the table output
		// STATE.dump(stream);
	}
}
