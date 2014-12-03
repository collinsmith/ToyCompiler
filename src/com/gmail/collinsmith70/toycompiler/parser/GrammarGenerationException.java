package com.gmail.collinsmith70.toycompiler.parser;

public class GrammarGenerationException extends RuntimeException {
	public GrammarGenerationException(String msg) {
		super(msg);
	}

	public GrammarGenerationException(String format, Object... args) {
		super(String.format(format, args));
	}
}
