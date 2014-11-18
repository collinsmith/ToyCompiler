package com.gmail.collinsmith70.toycompiler.lexer;

public class Evaluator {
	private Evaluator() {
		//...
	}

	public static final Object evaluate(TokenTypes type, String lexeme) {
		switch (type) {
			case _characterliteral:	return lexeme.charAt(0);
			case _booleanliteral:	return Boolean.valueOf(lexeme);
			case _integerliteral:	return Integer.valueOf(lexeme);
			case _doubleliteral:	return Double.valueOf(lexeme);
			case _stringliteral:	return lexeme;
			case _nullliteral:		return null;
			default:			return null;
		}
	}
}
