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
	public Token next(Reader r) throws IOException {
		reader: while (true) {
			int i = r.read();
			if (i == -1) {
				return Lexeme._eof.getDefaultToken();
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
									//return EBNFLexeme._comment.getDefaultToken();
									continue reader;
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
				case '\"':
				case '\'':
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

					if (length == 0) {
						throw new LexemeFormatException(EBNFLexeme._terminalSymbol, sb.toString(), "Terminal symbol length = 0");
					} else if (quoteChar == i) {
						assert EBNFLexeme._terminalSymbol.getPattern().matcher(sb).matches();
						return new Token(EBNFLexeme._terminalSymbol, sb.substring(1, sb.length()-1));
					} else if (isQuoteCharacter(i)) {
						throw new LexemeFormatException(EBNFLexeme._terminalSymbol, sb.toString(), "Terminal symbol termination characters do not match");
					}

					throw new LexemeFormatException(EBNFLexeme._terminalSymbol, sb.toString(), "Invalid terminal symbol character \"" + (char)i + "\"");
				case '<':
				default:
					boolean enclosed = i == '<';
					length = enclosed ? 0 : 1;
					while (true) {
						r.mark(1);
						i = r.read();
						if (i == -1) {
							if (length == 0) {
								throw new LexemeFormatException(EBNFLexeme._nonterminalSymbol, sb.toString(), "Nonterminal symbol length = 0");
							} else if (enclosed) {
								throw new LexemeFormatException(EBNFLexeme._nonterminalSymbol, sb.toString(), "Nonterminal symbol termination character did not match \">\"");
							} else {
								break;
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
						if (!isIdentifierPrimeCharacter(i)) {
							break;
						}

						length++;
						sb.appendCodePoint(i);

						r.mark(1);
						i = r.read();
						if (i == -1) {
							if (enclosed) {
								throw new LexemeFormatException(EBNFLexeme._nonterminalSymbol, sb.toString(), "End of input. No matching nonterminal termination character \">\"");
							} else {
								break;
							}
						}
					}

					if (i == '>') {
						assert EBNFLexeme._nonterminalSymbol.getPattern().matcher(sb).matches();
						return new Token(EBNFLexeme._nonterminalSymbol, sb.substring(1, sb.length()-1));
					} else if (enclosed) {
						throw new LexemeFormatException(EBNFLexeme._nonterminalSymbol, sb.toString(), "Nonterminal symbol termination character missing");
					} else if (isLexemeFirstSymbol(i) || Character.isWhitespace(i)) {
						r.reset();
						assert EBNFLexeme._nonterminalSymbol.getPattern().matcher(sb).matches();
						return new Token(EBNFLexeme._nonterminalSymbol, sb.toString());
					}

					throw new LexemeFormatException(EBNFLexeme._nonterminalSymbol, sb.toString(), "Invalid nonterminal symbol character \"" + (char)i + "\"");
			}
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
		return BNFScanner.isIdentifierCharacter(codePoint);
	}

	private static boolean isIgnorableCharacter(int codePoint) {
		return BNFScanner.isIgnorableCharacter(codePoint);
	}

	private static boolean isIdentifierPrimeCharacter(int codePoint) {
		return BNFScanner.isIdentifierPrimeCharacter(codePoint);
	}

	private static boolean isLexemeFirstSymbol(int codePoint) {
		switch (codePoint) {
			case '=':
			case '|':
			case '.':
			case ';':
			case '(':
			case ')':
			case '[':
			case ']':
			case '{':
			case '}':
			case '\"':
			case '\'':
			case '<':
				return true;
			default:
				return false;
		}
	}
}
