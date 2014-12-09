package com.gmail.collinsmith70.toycompiler.bnf;

public interface Lexeme {
	String getName();
	String getRegex();
	int getId();
	Token getDefaultToken();
	
	Lexeme _eof = new Lexeme() {
		private final Token DEFAULT_TOKEN = new Token(this);
		
		@Override
		public String getName() {
			return "_eof";
		}

		@Override
		public String getRegex() {
			return "$";
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
