package com.gmail.collinsmith70.toycompiler.cfg;

import com.google.common.base.Strings;
import java.util.Objects;

public class LexemeFormatException extends RuntimeException {
	private final Lexeme LEXEME;
	private final String FOUND;
	private final String INFO;

	public LexemeFormatException(Lexeme lexeme, String found) {
		this(lexeme, found, "");
	}

	public LexemeFormatException(Lexeme lexeme, String found, int line) {
		super(String.format("Expected lexeme format for %s. Found \"%s\" on line %d.", lexeme, found, line));
		this.LEXEME = Objects.requireNonNull(lexeme);
		this.FOUND = Objects.requireNonNull(found);
		this.INFO = "";
	}

	public LexemeFormatException(Lexeme lexeme, String found, String info) {
		super(String.format("Expected lexeme format for %s. Found \"%s\". Additional info: %s", lexeme, found, info));
		this.LEXEME = Objects.requireNonNull(lexeme);
		this.FOUND = Objects.requireNonNull(found);
		this.INFO = Strings.nullToEmpty(info);
	}

	public LexemeFormatException(Lexeme lexeme, String found, String info, int line) {
		super(String.format("Expected lexeme format for %s. Found \"%s\" on line %d. Additional info: %s", lexeme, found, line, info));
		this.LEXEME = Objects.requireNonNull(lexeme);
		this.FOUND = Objects.requireNonNull(found);
		this.INFO = Strings.nullToEmpty(info);
	}

	public Lexeme getLexeme() {
		return LEXEME;
	}

	public String getFound() {
		return FOUND;
	}

	public String getInfo() {
		return INFO;
	}
}
