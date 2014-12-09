package com.gmail.collinsmith70.toycompiler.bnf;

import java.io.IOException;
import java.io.Reader;

public class BNFScanner implements Scanner<Token> {
	public BNFScanner() {
		//...
	}

	@Override
	public Token next(Reader r) {
		int i = -1;
		try {
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
					return BNFLexeme._alternation.getDefaultToken();
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
				} else if (i == '<') {
					while (true) {
						i = r.read();
						sb.append(i);
						if (!isIdentifierCharacter(i)) {
							break;
						}						
					}
					
					if (i == '>') {
						return new Token(BNFLexeme._nonterminalSymbol, sb.substring(1, sb.length()));
					} else {
						throw new LexemeFormatException(BNFLexeme._nonterminalSymbol, sb.toString(), "Nonterminal symbol termination character did not match \">\"");
					}
				} else if (isIdentifierCharacter(i)) {
					while (true) {
						i = r.read();
						sb.append(i);
						if (!isIdentifierCharacter(i)) {
							break;
						}						
					}
					
					if (Character.isWhitespace(i)) {
						return new Token(BNFLexeme._terminalSymbol, sb.substring(0, sb.length()));
					} else {
						throw new UndefinedLexemeException(sb.toString(), i);
					}
				}
			}
		} catch (IOException e) {
		}
		
		if (i == -1) {
			return Lexeme._eof.getDefaultToken();
		}

		assert false : "There is a scanned case which is not accounted for!";
		throw new UndefinedLexemeException();
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
