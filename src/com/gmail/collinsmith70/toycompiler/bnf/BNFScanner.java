package com.gmail.collinsmith70.toycompiler.bnf;

import java.io.IOException;
import java.io.Reader;

public class BNFScanner implements Scanner<Token> {
	public BNFScanner() {
		//...
	}

	@Override
	public Token next(Reader r) {
		try {
			int i = -1;
			reader: while (true) {
				i = r.read();
				if (i == -1) {
					break reader;
				} else if (Character.isWhitespace(i)) {
					continue reader;
				}

				StringBuilder sb = new StringBuilder()
					.append(i);

				if (i == '|') {
					return BNFLexeme._or.getDefaultToken();
				} else if (i == ':') {
					i = r.read();
					sb.append(i);
					if (i == ':') {
						i = r.read();
						sb.append(i);
						if (i == '=') {
							return BNFLexeme._assignop.getDefaultToken();
						}
					}

					return new Token(null, sb.toString());
				}

				boolean enclosed = isEnclosableCharacter(i);
				int match = i;

				while (true) {
					i = r.read();
					if (!isIdentifierCharacter(i)) {
						break;
					}

					sb.append(i);
				}

				if (enclosed) {
					if (isEnclosableTerminatorCharacter(i)) {
						if (match == i) {
							return new Token(BNFLexeme._terminalSymbol, sb.toString());
						} else {
							if (match == '<' && i == '>') {
								return new Token(BNFLexeme._nonterminalSymbol, sb.toString());
							} else {
								// enclosable mismatch exception
								return new Token(null, sb.toString());
							}
						}
					} else {
						// missing enclosable terminator
						return new Token(null, sb.toString());
					}
				} else {
					if (!isEnclosableTerminatorCharacter(i)) {
						return new Token(BNFLexeme._nonterminalSymbol, sb.toString());
					} else {
						// missing enclosable initializer
						return new Token(null, sb.toString());
					}
				}
			}
		} catch (IOException e) {
		}
	}

	private static boolean isEnclosableCharacter(int codePoint) {
		return isQuoteCharacter(codePoint) || codePoint == '<';
	}

	private static boolean isEnclosableTerminatorCharacter(int codePoint) {
		return isQuoteCharacter(codePoint) || codePoint == '>';
	}

	private static boolean isQuoteCharacter(int codePoint) {
		return codePoint == '\"' || codePoint == '\'';
	}

	private static boolean isIdentifierCharacter(int codePoint) {
		switch (Character.getType(codePoint)) {
			case Character.UPPERCASE_LETTER:
			case Character.LOWERCASE_LETTER:
			case Character.TITLECASE_LETTER:
			case Character.MODIFIER_LETTER:
			case Character.OTHER_LETTER:
			case Character.NON_SPACING_MARK:
			case Character.ENCLOSING_MARK:
			case Character.COMBINING_SPACING_MARK:
			case Character.DECIMAL_DIGIT_NUMBER:
			case Character.LETTER_NUMBER:
			case Character.OTHER_NUMBER:
			case Character.CONNECTOR_PUNCTUATION:
				return true;
			default:
				return false;
		}
	}
}
