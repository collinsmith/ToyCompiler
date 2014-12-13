package com.gmail.collinsmith70.toycompiler.cfg;

import java.util.regex.Pattern;

public interface Lexeme extends Token {
	String getName();
	Pattern getPattern();
	int getId();
	Token createChild(Object value);

	Lexeme _eof = new Lexeme() {
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
			return new Token() {
				@Override
				public Lexeme getLexeme() {
					return Lexeme._eof;
				}

				@Override
				public Object getValue() {
					return value;
				}

				@Override
				public String toString() {
					if (value == null) {
						return Lexeme._eof.toString();
					}

					return Lexeme._eof.toString() + "[" + value.toString() + "]";
				}
			};
		}
	};
}
