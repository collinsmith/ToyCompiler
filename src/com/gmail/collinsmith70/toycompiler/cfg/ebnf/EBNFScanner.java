package com.gmail.collinsmith70.toycompiler.cfg.ebnf;

import com.gmail.collinsmith70.toycompiler.cfg.Lexeme;
import com.gmail.collinsmith70.toycompiler.cfg.LexemeFormatException;
import com.gmail.collinsmith70.toycompiler.cfg.Scanner;
import com.gmail.collinsmith70.toycompiler.cfg.Token;
import com.gmail.collinsmith70.toycompiler.cfg.UndefinedLexemeException;
import com.gmail.collinsmith70.toycompiler.cfg.bnf.BNFScanner;
import java.io.IOException;
import java.io.Reader;

public class EBNFScanner implements Scanner<Token> {
	public EBNFScanner() {
		//...
	}

	@Override
	public boolean requiresMark() {
		return true;
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
				} else if (isIgnorableCharacter(i)) {
					continue reader;
				}

				StringBuilder sb = new StringBuilder()
					.appendCodePoint(i);

				switch (i) {
					case '=':
						assert EBNFLexeme._assignop.getPattern().matcher(sb).matches();
						return EBNFLexeme._assignop.getDefaultToken();
					case '|':
						assert EBNFLexeme._alternation.getPattern().matcher(sb).matches();
						return EBNFLexeme._alternation.getDefaultToken();
					case '.':
					case ';':
						assert EBNFLexeme._terminator.getPattern().matcher(sb).matches();
						return EBNFLexeme._terminator.getDefaultToken();
					case '(':
						r.mark(1);
						i = r.read();
						if (i == -1) {
							// is left paren only, still a valid lexeme
						} else if (i == '*') {
							sb.appendCodePoint(i);
							while (true) {
								i = r.read();
								if (i == -1) {
									throw new UndefinedLexemeException(sb.toString());
								} else if (i == '*') {
									sb.appendCodePoint(i);
									r.mark(1);
									i = r.read();
									if (i == -1) {
										throw new UndefinedLexemeException(sb.toString());
									} else if (i == ')') {
										sb.appendCodePoint(i);
										assert EBNFLexeme._comment.getPattern().matcher(sb).matches();
										return EBNFLexeme._comment.getDefaultToken();
									}

									sb.deleteCharAt(sb.length()-1);
									r.reset();
								}
							}
						}

						r.reset();
						assert EBNFLexeme._leftparen.getPattern().matcher(sb).matches();
						return EBNFLexeme._leftparen.getDefaultToken();
					case ')':
						assert EBNFLexeme._rightparen.getPattern().matcher(sb).matches();
						return EBNFLexeme._rightparen.getDefaultToken();
					case '[':
						assert EBNFLexeme._leftbracket.getPattern().matcher(sb).matches();
						return EBNFLexeme._leftbracket.getDefaultToken();
					case ']':
						assert EBNFLexeme._rightbracket.getPattern().matcher(sb).matches();
						return EBNFLexeme._rightbracket.getDefaultToken();
					case '{':
						assert EBNFLexeme._leftbrace.getPattern().matcher(sb).matches();
						return EBNFLexeme._leftbrace.getDefaultToken();
					case '}':
						assert EBNFLexeme._rightbrace.getPattern().matcher(sb).matches();
						return EBNFLexeme._rightbrace.getDefaultToken();
				}

				boolean nonterminal = !isQuoteCharacter(i);
				if (nonterminal) {
					if (i == '<') {
						int length = 0;
						while (true) {
							i = r.read();
							if (i == -1) {
								if (length == 0) {
									throw new LexemeFormatException(EBNFLexeme._nonterminalSymbol, sb.toString(), "Nonterminal symbol length = 0");
								} else {
									throw new LexemeFormatException(EBNFLexeme._nonterminalSymbol, sb.toString(), "Nonterminal symbol termination character did not match \">\"");
								}
							}

							sb.appendCodePoint(i);
							if (!isIdentifierCharacter(i)) {
								break;
							}

							length++;
						}

						if (length == 0) {
							throw new LexemeFormatException(EBNFLexeme._nonterminalSymbol, sb.toString(), "Nonterminal symbol length = 0");
						}

						while (true) {
							if (!BNFScanner.isIdentifierPrimeCharacter(i)) {
								break;
							}

							i = r.read();
							if (i == -1) {
								break;
							}

							length++;
							sb.appendCodePoint(i);
						}

						if (i == '>') {
							assert EBNFLexeme._nonterminalSymbol.getPattern().matcher(sb).matches();
							return new Token(EBNFLexeme._nonterminalSymbol, sb.substring(1, sb.length()-1));
						} else {
							throw new LexemeFormatException(EBNFLexeme._nonterminalSymbol, sb.toString(), "Nonterminal symbol termination character did not match \">\"");
						}
					} else if (isIdentifierCharacter(i)) {
						// nonterminal symbol length is already 1
						while (true) {
							r.mark(1);
							i = r.read();
							if (i == -1) {
								break;
							}

							sb.appendCodePoint(i);
							if (!isIdentifierCharacter(i)) {
								break;
							}
						}

						while (true) {
							if (!BNFScanner.isIdentifierPrimeCharacter(i)) {
								break;
							}

							r.mark(1);
							i = r.read();
							if (i == -1) {
								break;
							}

							sb.appendCodePoint(i);
						}

						if (Character.isWhitespace(i)) {
							assert EBNFLexeme._nonterminalSymbol.getPattern().matcher(sb.substring(0, sb.length()-1)).matches();
							return new Token(EBNFLexeme._nonterminalSymbol, sb.substring(0, sb.length()-1));
						} else if (i == -1) {
							assert EBNFLexeme._nonterminalSymbol.getPattern().matcher(sb).matches();
							return new Token(EBNFLexeme._nonterminalSymbol, sb.toString());
						} else if (isEnclosableCharacter(i)) {
							r.reset();
							sb.deleteCharAt(sb.length()-1);
							assert EBNFLexeme._nonterminalSymbol.getPattern().matcher(sb).matches();
							return new Token(EBNFLexeme._nonterminalSymbol, sb.toString());
						} else {
							throw new UndefinedLexemeException(sb.toString());
						}
					} else {
						throw new UndefinedLexemeException(sb.toString());
					}
				} else { // if it is a terminal symbol
					int quoteChar = i;
					int length = 0;
					while (true) {
						i = r.read();
						if (i == -1) {
							if (length == 0) {
								throw new LexemeFormatException(EBNFLexeme._terminalSymbol, sb.toString(), "Terminal symbol length = 0");
							} else {
								throw new LexemeFormatException(EBNFLexeme._terminalSymbol, sb.toString(), "Terminal symbol termination character did not match \"" + quoteChar + "\"");
							}
						}

						sb.appendCodePoint(i);
						if (isQuoteCharacter(i)) {
							break;
						}

						length++;
					}

					if (quoteChar == i) {
						assert EBNFLexeme._terminalSymbol.getPattern().matcher(sb).matches();
						return new Token(EBNFLexeme._terminalSymbol, sb.substring(1, sb.length()-1));
					} else if (isQuoteCharacter(i)) {
						throw new LexemeFormatException(EBNFLexeme._terminalSymbol, sb.toString(), "Terminal symbol termination characters do not match");
					} else {
						throw new LexemeFormatException(EBNFLexeme._terminalSymbol, sb.toString(), "Terminal symbol termination character " + (char)i + " did not match \"" + (char)quoteChar + "\"");
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
		return BNFScanner.isIdentifierCharacter(codePoint);
	}

	private static boolean isIgnorableCharacter(int codePoint) {
		return BNFScanner.isIgnorableCharacter(codePoint);
	}
}
