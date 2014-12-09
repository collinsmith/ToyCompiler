package com.gmail.collinsmith70.toycompiler.ebnf;

import com.gmail.collinsmith70.toycompiler.bnf.BNFLexeme;
import com.gmail.collinsmith70.toycompiler.bnf.Lexeme;
import com.gmail.collinsmith70.toycompiler.bnf.LexemeFormatException;
import com.gmail.collinsmith70.toycompiler.bnf.Scanner;
import com.gmail.collinsmith70.toycompiler.bnf.Token;
import com.gmail.collinsmith70.toycompiler.bnf.UndefinedLexemeException;
import java.io.IOException;
import java.io.Reader;

public class EBNFScanner implements Scanner<Token> {
	public EBNFScanner() {
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
				
				switch (i) {
					case '=':
						assert "=".matches(EBNFLexeme._assignop.getRegex());
						return EBNFLexeme._assignop.getDefaultToken();
					case '|':
						assert "|".matches(EBNFLexeme._alternation.getRegex());
						return EBNFLexeme._alternation.getDefaultToken();
					case '.':
					case ';':
						assert sb.toString().matches(EBNFLexeme._terminator.getRegex());
						return EBNFLexeme._terminator.getDefaultToken();
					case '(':
						r.mark(1);
						i = r.read();
						if (i == '*') {
							while (true) {
								i = r.read();
								sb.append(i);
								if (i == '*') {
									r.mark(1);
									i = r.read();
									sb.append(i);
									if (i == ')') {
										assert sb.toString().matches(EBNFLexeme._comment.getRegex());
										return EBNFLexeme._comment.getDefaultToken();
									}
									
									sb.deleteCharAt(sb.length()-1);
									r.reset();
								}
							}
						}
						
						r.reset();
						assert sb.toString().matches(EBNFLexeme._leftparen.getRegex());
						return EBNFLexeme._leftparen.getDefaultToken();
					case ')':
						assert sb.toString().matches(EBNFLexeme._rightparen.getRegex());
						return EBNFLexeme._rightparen.getDefaultToken();
					case '[':
						assert sb.toString().matches(EBNFLexeme._leftbracket.getRegex());
						return EBNFLexeme._leftbracket.getDefaultToken();
					case ']':
						assert sb.toString().matches(EBNFLexeme._rightbracket.getRegex());
						return EBNFLexeme._rightbracket.getDefaultToken();
					case '{':
						assert sb.toString().matches(EBNFLexeme._leftbrace.getRegex());
						return EBNFLexeme._leftbrace.getDefaultToken();
					case '}':
						assert sb.toString().matches(EBNFLexeme._rightbrace.getRegex());
						return EBNFLexeme._rightbrace.getDefaultToken();
				}
				
				boolean enclosed = isEnclosableCharacter(i);
				int match = i;
				while (true) {
					r.mark(1);
					i = r.read();
					sb.append(i);
					if (!isIdentifierCharacter(i)) {
						break;
					}						
				}

				if (enclosed && isEnclosableTerminatorCharacter(i)) {
					if (isQuoteCharacter(i) && match == i) {
						assert sb.toString().matches(EBNFLexeme._terminalSymbol.getRegex());
						return new Token(BNFLexeme._terminalSymbol, sb.substring(1, sb.length()-1));
					} else {
						if (match == '<' && i == '>') {
							assert sb.toString().matches(EBNFLexeme._terminalSymbol.getRegex());
							return new Token(BNFLexeme._nonterminalSymbol, sb.substring(1, sb.length()-1));
						} else {
							switch (match) {
								case '\"': throw new LexemeFormatException(BNFLexeme._terminalSymbol, sb.toString(), "Terminal symbol termination character did not match \"\"\"");
								case '\'': throw new LexemeFormatException(BNFLexeme._terminalSymbol, sb.toString(), "Terminal symbol termination character did not match \"\'\"");
								case '<': throw new LexemeFormatException(BNFLexeme._nonterminalSymbol, sb.toString(), "Nonterminal symbol termination character did not match \">\"");
							}
							
							assert false;
						}
					}
				} else if (isEnclosableTerminatorCharacter(i)) {
					switch (i) {
						case '\"': throw new LexemeFormatException(BNFLexeme._terminalSymbol, sb.toString(), "Found terminal symbol termination character, but no initialization character \"\"\"");
						case '\'': throw new LexemeFormatException(BNFLexeme._terminalSymbol, sb.toString(), "Found terminal symbol termination character, but no initialization character \"\'\"");
						case '>': throw new LexemeFormatException(BNFLexeme._nonterminalSymbol, sb.toString(), "Found nonterminal symbol termination character, but no initialization character  \"<\"");
					}
					
					assert false;
				} else {
					r.reset();
					sb.deleteCharAt(sb.length()-1);
					assert sb.toString().matches(EBNFLexeme._nonterminalSymbol.getRegex());
					return new Token(BNFLexeme._nonterminalSymbol, sb.toString());
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
