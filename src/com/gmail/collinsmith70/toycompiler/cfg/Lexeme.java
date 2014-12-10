package com.gmail.collinsmith70.toycompiler.cfg;

import java.util.regex.Pattern;

public interface Lexeme {
	String getName();
	Pattern getPattern();
	int getId();
	Token getDefaultToken();

	Lexeme _eof = new Lexeme() {
		private final Token DEFAULT_TOKEN = new Token(this);
		private final Pattern PATTERN = Pattern.compile("$", 0);

		@Override
		public String getName() {
			return "_eof";
		}

		@Override
		public Pattern getPattern() {
			return PATTERN;
		}

		@Override
		public int getId() {
			return 0;
		}

		@Override
		public Token getDefaultToken() {
			return DEFAULT_TOKEN;
		}
	};
}
