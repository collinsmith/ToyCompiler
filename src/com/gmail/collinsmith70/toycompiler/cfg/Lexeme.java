package com.gmail.collinsmith70.toycompiler.cfg;

import java.util.regex.Pattern;

public interface Lexeme extends Token {
	String getName();
	Pattern getPattern();
	int getId();
	Token createChild(Object value);

	Lexeme _eof = new Lexeme() {
		private final Pattern PATTERN = Pattern.compile("\\z", Pattern.UNICODE_CHARACTER_CLASS);

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
		public Lexeme getLexeme() {
			return this;
		}

		@Override
		public Object getValue() {
			return null;
		}

		@Override
		public String toString() {
			return getName();
		}

		@Override
		public Token createChild(Object value) {
			throw new UnsupportedOperationException();
		}
	};

	Lexeme _eol = new Lexeme() {
		private final Pattern PATTERN = Pattern.compile("$", Pattern.UNICODE_CHARACTER_CLASS);

		@Override
		public String getName() {
			return "_eol";
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
		public Lexeme getLexeme() {
			return this;
		}

		@Override
		public Object getValue() {
			return null;
		}

		@Override
		public String toString() {
			return getName();
		}

		@Override
		public Token createChild(Object value) {
			throw new UnsupportedOperationException();
		}
	};
}
