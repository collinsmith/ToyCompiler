package com.gmail.collinsmith70.toycompiler.parser2;

import java.io.PrintStream;

public abstract class Conflict {
	@Override
	public abstract String toString();

	private final State STATE;

	public Conflict(State s) {
		this.STATE = s;
	}

	public State getState() {
		return STATE;
	}

	public void dump(PrintStream stream) {
		stream.format("State %d has a %s conflict:%n", toString());
		// TODO: implement this to print tables
		//STATE.dump(stream);
	}
}
