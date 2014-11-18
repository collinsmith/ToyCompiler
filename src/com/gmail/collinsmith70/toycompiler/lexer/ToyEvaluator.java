package com.gmail.collinsmith70.toycompiler.lexer;

public class ToyEvaluator {
	private ToyEvaluator() {
		//...
	}

	public static final Object evaluate(TokenTypes type, String lexeme) {
		switch (type) {
			case _characterliteral:	return lexeme.charAt(0);
			case _booleanliteral:	return Boolean.valueOf(lexeme);
			case _integerliteral:	return Integer.valueOf(lexeme);
			case _doubleliteral:	return Double.valueOf(lexeme);
			case _stringliteral:	return lexeme;
			case _nullliteral:	return null;
			case _id:			return lexeme;
			default:			return null;
		}
	}

	public static final Object evaluate(TokenTypes type, int val) {
		if (type == TokenTypes._integerliteral) {
			return val;
		}

		throw new IllegalArgumentException("Expected _integerliteral token type");
	}
}
