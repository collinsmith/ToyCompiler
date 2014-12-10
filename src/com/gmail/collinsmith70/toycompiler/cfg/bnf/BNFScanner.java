package com.gmail.collinsmith70.toycompiler.cfg.bnf;

import com.gmail.collinsmith70.toycompiler.cfg.Lexeme;
import com.gmail.collinsmith70.toycompiler.cfg.LexemeFormatException;
import com.gmail.collinsmith70.toycompiler.cfg.Scanner;
import com.gmail.collinsmith70.toycompiler.cfg.Token;
import com.gmail.collinsmith70.toycompiler.cfg.UndefinedLexemeException;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.Reader;

public class BNFScanner implements Scanner<Token> {
	public BNFScanner() {
		//...
	}

	@Override
	public boolean requiresMark() {
		return false;
	}

	@Override
	public Token next(Reader r) {
		Preconditions.checkNotNull(r);
		int i = -1;
		try {
			reader: while (true) {
				i = r.read();
				if (i == -1) {
					break reader;
				} else if (Character.isWhitespace(i)) {
					continue reader;
				} else if (isIgnorableCharacter(i)) {
					continue reader;
				}

				StringBuilder sb = new StringBuilder()
					.appendCodePoint(i);

				switch (i) {
					case '|':
						assert BNFLexeme._alternation.getPattern().matcher(sb).matches();
						return BNFLexeme._alternation.getDefaultToken();
					case ':':
						i = r.read();
						if (i == -1) {
							throw new UndefinedLexemeException(sb.toString());
						} else if (i == ':') {
							sb.appendCodePoint(i);
							i = r.read();
							if (i == -1) {
								throw new UndefinedLexemeException(sb.toString());
							} else if (i == '=') {
								sb.appendCodePoint(i);
								assert BNFLexeme._assignop.getPattern().matcher(sb).matches();
								return BNFLexeme._assignop.getDefaultToken();
							}
						}

						throw new UndefinedLexemeException(sb.toString());
					case '<':
						int length = 0;
						while (true) {
							i = r.read();
							if (i == -1) {
								if (length == 0) {
									throw new LexemeFormatException(BNFLexeme._nonterminalSymbol, sb.toString(), "Nonterminal symbol length = 0");
								} else {
									throw new LexemeFormatException(BNFLexeme._nonterminalSymbol, sb.toString(), "End of input. No matching nonterminal termination character \">\"");
								}
							}

							sb.appendCodePoint(i);
							if (!isIdentifierCharacter(i)) {
								break;
							}

							length++;
						}

						if (length == 0) {
							throw new LexemeFormatException(BNFLexeme._nonterminalSymbol, sb.toString(), "Nonterminal symbol length = 0");
						}

						while (true) {
							if (!isIdentifierPrimeCharacter(i)) {
								break;
							}

							length++;
							sb.appendCodePoint(i);
							i = r.read();
							if (i == -1) {
								throw new LexemeFormatException(BNFLexeme._nonterminalSymbol, sb.toString(), "End of input. No matching nonterminal termination character \">\"");
							}
						}

						if (i == '>') {
							assert BNFLexeme._nonterminalSymbol.getPattern().matcher(sb).matches();
							return new Token(BNFLexeme._nonterminalSymbol, sb.substring(1, sb.length()-1));
						}

						throw new LexemeFormatException(BNFLexeme._nonterminalSymbol, sb.toString(), "Invalid nonterminal symbol character \"" + (char)i + "\"");
					default:
						length = 0;
						while (true) {
							i = r.read();
							if (i == -1) {
								break;
							}

							length++;
							sb.appendCodePoint(i);
							if (!isIdentifierCharacter(i)) {
								break;
							}
						}

						if (Character.isWhitespace(i)) {
							assert BNFLexeme._terminalSymbol.getPattern().matcher(sb.substring(0, sb.length()-1)).matches();
							return new Token(BNFLexeme._terminalSymbol, sb.substring(0, sb.length()-1));
						} else if (i == -1) {
							assert BNFLexeme._terminalSymbol.getPattern().matcher(sb).matches();
							return new Token(BNFLexeme._terminalSymbol, sb.toString());
						}

						throw new LexemeFormatException(BNFLexeme._terminalSymbol, sb.toString(), "Invalid terminal symbol character \"" + (char)i + "\"");
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

	public static boolean isIdentifierCharacter(int codePoint) {
		switch (Character.getType(codePoint)) {
			case Character.UPPERCASE_LETTER:		// Lu
			case Character.LOWERCASE_LETTER:		// Ll
			case Character.TITLECASE_LETTER:		// Lt
			case Character.MODIFIER_LETTER:		// Lm
			case Character.OTHER_LETTER:			// Lo
			case Character.DECIMAL_DIGIT_NUMBER:	// Nd
			case Character.CONNECTOR_PUNCTUATION:	// Pc
				return true;
			default:
				return false;
		}
	}

	public static boolean isIdentifierPrimeCharacter(int codePoint) {
		return codePoint == '\'' || codePoint == '`';
	}

	public static boolean isIgnorableCharacter(int codePoint) {
		return !Character.isDefined(codePoint) || Character.isIdentifierIgnorable(codePoint);
	}
}
