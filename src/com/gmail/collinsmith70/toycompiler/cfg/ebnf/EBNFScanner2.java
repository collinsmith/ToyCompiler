package com.gmail.collinsmith70.toycompiler.cfg.ebnf;

import com.gmail.collinsmith70.toycompiler.cfg.Lexeme;
import com.gmail.collinsmith70.toycompiler.cfg.Scanner;
import com.gmail.collinsmith70.toycompiler.cfg.Token;
import java.io.IOException;
import java.io.Reader;

public class EBNFScanner2 implements Scanner<Token> {
	public EBNFScanner2() {
		//...
	}

	@Override
	public boolean requiresMark() {
		return true;
	}

	@Override
	public Token next(Reader r) throws IOException {
		while (true) {
			int i = r.read();
			//StringBuilder sb = new StringBuilder(1)
			//	.appendCodePoint(i);
			switch (i) {
				//case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'g': case 'h': case 'i': case 'j': case 'k': case 'l': case 'm': case 'n': case 'o': case 'p': case 'q': case 'r': case 's': case 't': case 'u': case 'v': case 'w': case 'x': case 'y': case 'z':
				//case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': case 'G': case 'H': case 'I': case 'J': case 'K': case 'L': case 'M': case 'N': case 'O': case 'P': case 'Q': case 'R': case 'S': case 'T': case 'U': case 'V': case 'W': case 'X': case 'Y': case 'Z':
				//	//assert EBNFLexeme.letter.getPattern().matcher(sb).matches();
				//	return new Token(EBNFLexeme.letter, (char)i);
				case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'g': case 'h': case 'i': case 'j': case 'k': case 'l': case 'm': case 'n': case 'o': case 'p': case 'q': case 'r': case 's': case 't': case 'u': case 'v': case 'w': case 'x': case 'y': case 'z':
				case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': case 'G': case 'H': case 'I': case 'J': case 'K': case 'L': case 'M': case 'N': case 'O': case 'P': case 'Q': case 'R': case 'S': case 'T': case 'U': case 'V': case 'W': case 'X': case 'Y': case 'Z':
					StringBuilder metaIdentifierBuilder = new StringBuilder(32)
						.appendCodePoint(i);
					while (true) {
						r.mark(1);
						switch (i = r.read()) {
							case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'g': case 'h': case 'i': case 'j': case 'k': case 'l': case 'm': case 'n': case 'o': case 'p': case 'q': case 'r': case 's': case 't': case 'u': case 'v': case 'w': case 'x': case 'y': case 'z':
							case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': case 'G': case 'H': case 'I': case 'J': case 'K': case 'L': case 'M': case 'N': case 'O': case 'P': case 'Q': case 'R': case 'S': case 'T': case 'U': case 'V': case 'W': case 'X': case 'Y': case 'Z':
							case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
								metaIdentifierBuilder.appendCodePoint(i);
							case -1:
							default:
								r.reset();
								//assert EBNFLexeme.metaIdentifier.getPattern().matcher(metaIdentifierBuilder).matches();
								return new Token(EBNFLexeme.metaIdentifier, metaIdentifierBuilder.toString());
						}
					}
				//case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
				//	//assert EBNFLexeme.decimalDigit.getPattern().matcher(sb).matches();
				//	return new Token(EBNFLexeme.decimalDigit, (char)i);
				case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
					int integerBuilder = (i-'0');
					while (true) {
						r.mark(1);
						switch (i = r.read()) {
							case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
								integerBuilder <<= 1;
								integerBuilder += (i-'0');
							case -1:
							default:
								r.reset();
								return new Token(EBNFLexeme.integer, integerBuilder);
						}
					}
				//case '+':
				//case '_':
				//case '%':
				//case '@':
				//case '&':
				//case '#':
				//case '$':
				//case '<':
				//case '>':
				//case '\\':
				//case '^':
				//case '`':
				//case '~':
				//	//assert EBNFLexeme.otherCharacter.getPattern().matcher(sb).matches();
				//	return new Token(EBNFLexeme.otherCharacter, (char)i);
				case ',':
					//assert EBNFLexeme.concatenateSymbol.getPattern().matcher(sb).matches();
					return EBNFLexeme.concatenateSymbol.getDefaultToken();
				case '=':
					//assert EBNFLexeme.definingSymbol.getPattern().matcher(sb).matches();
					return EBNFLexeme.definingSymbol.getDefaultToken();
				case '|':
				case '!':
					//assert EBNFLexeme.definitionSeparatorSymbol.getPattern().matcher(sb).matches();
					return EBNFLexeme.definitionSeparatorSymbol.getDefaultToken();
				case '/':
					r.mark(i);
					switch (i = r.read()) {
						case ')':
							//sb.appendCodePoint(i);
							//assert EBNFLexeme.endOptionSymbol.getPattern().matcher(sb).matches();
							return EBNFLexeme.endOptionSymbol.getDefaultToken();
						case -1:
						default:
							//assert EBNFLexeme.definitionSeparatorSymbol.getPattern().matcher(sb).matches();
							return EBNFLexeme.definitionSeparatorSymbol.getDefaultToken();
					}
				case '*':
					r.mark(1);
					switch (i = r.read()) {
						//case ')':
						//	//sb.appendCodePoint(i);
						//	//assert EBNFLexeme.endCommentSymbol.getPattern().matcher(sb).matches();
						//	return EBNFLexeme.endCommentSymbol.getDefaultToken();
						case -1:
						default:
							r.reset();
							//assert EBNFLexeme.repetitionSymbol.getPattern().matcher(sb).matches();
							return EBNFLexeme.repetitionSymbol.getDefaultToken();
					}
				case ']':
					//assert EBNFLexeme.endOptionSymbol.getPattern().matcher(sb).matches();
					return EBNFLexeme.endOptionSymbol.getDefaultToken();
				case '}':
					//assert EBNFLexeme.endRepeatSymbol.getPattern().matcher(sb).matches();
					return EBNFLexeme.endRepeatSymbol.getDefaultToken();
				case ':':
					r.mark(1);
					switch (i = r.read()) {
						case ')':
							//sb.appendCodePoint(i);
							//assert EBNFLexeme.endRepeatSymbol.getPattern().matcher(sb).matches();
							return EBNFLexeme.endRepeatSymbol.getDefaultToken();
						case -1:
						default:
							// TODO: Lexeme exception
							throw new RuntimeException();
					}
				case '-':
					//assert EBNFLexeme.exceptSymbol.getPattern().matcher(sb).matches();
					return EBNFLexeme.exceptSymbol.getDefaultToken();
				//case '\'':
				//	//assert EBNFLexeme.firstQuoteSymbol.getPattern().matcher(sb).matches();
				//	return EBNFLexeme.firstQuoteSymbol.getDefaultToken();
				//case '\"':
				//	//assert EBNFLexeme.secondQuoteSymbol.getPattern().matcher(sb).matches();
				//	return EBNFLexeme.secondQuoteSymbol.getDefaultToken();
				case '\'':
				case '\"':
					int quote = i;
					StringBuilder terminalStringBuilder = new StringBuilder(16);
					while (true) {
						switch (i = r.read()) {
							case -1:
								// TODO: Lexeme exception
								throw new RuntimeException();
							case '\'':
							case '\"':
								if (quote == i) {
									//assert EBNFLexeme.terminalString.getPattern().matcher((char)quote + terminalStringBuilder.toString() + (char)i).matches();
									return new Token(EBNFLexeme.terminalString, terminalStringBuilder.toString());
								}
							default:
								terminalStringBuilder.appendCodePoint(i);
						}
					}
				//case '?':
				//	//assert EBNFLexeme.specialSequenceSymbol.getPattern().matcher(sb).matches();
				//	return EBNFLexeme.specialSequenceSymbol.getDefaultToken();
				case '?':
					StringBuilder specialSequenceBuilder = new StringBuilder(16);
					while (true) {
						switch (i = r.read()) {
							case -1:
								// TODO: special sequence syntax exception
								throw new RuntimeException();
							case '?':
								//assert EBNFLexeme.terminalString.getPattern().matcher('?' + specialSequenceBuilder.toString() + '?').matches();
								return new Token(EBNFLexeme.specialSequence, specialSequenceBuilder.toString());
							default:
								specialSequenceBuilder.appendCodePoint(i);
						}
					}
				case '(':
					r.mark(1);
					switch (i = r.read()) {
						case '*':
							//sb.appendCodePoint(i);
							//assert EBNFLexeme.startCommentSymbol.getPattern().matcher(sb).matches();
							//return EBNFLexeme.startCommentSymbol.getDefaultToken();
							while (true) {
								switch (i = r.read()) {
									case -1:
										return next(r);
									case '*':
										r.mark(1);
										switch (i = r.read()) {
											case ')':
												return next(r);
											case -1:
												// TODO: comment syntax error
												throw new RuntimeException();
											case '*':
											default:
												// unread and add symbol to comment
												r.reset();
										}
									default:
										// add symbol to comment
								}
							}
						case '/':
							//sb.appendCodePoint(i);
							//assert EBNFLexeme.startOptionSymbol.getPattern().matcher(sb).matches();
							return EBNFLexeme.startOptionSymbol.getDefaultToken();
						case ':':
							//sb.appendCodePoint(i);
							//assert EBNFLexeme.startRepeatSymbol.getPattern().matcher(sb).matches();
							return EBNFLexeme.startRepeatSymbol.getDefaultToken();
						case -1:
						default:
							r.reset();
							//assert EBNFLexeme.startGroupSymbol.getPattern().matcher(sb).matches();
							return EBNFLexeme.startGroupSymbol.getDefaultToken();
					}
				case '[':
					//assert EBNFLexeme.startOptionSymbol.getPattern().matcher(sb).matches();
					return EBNFLexeme.startOptionSymbol.getDefaultToken();
				case '{':
					//assert EBNFLexeme.startRepeatSymbol.getPattern().matcher(sb).matches();
					return EBNFLexeme.startRepeatSymbol.getDefaultToken();
				case ';':
				case '.':
					//assert EBNFLexeme.terminatorSymbol.getPattern().matcher(sb).matches();
					return EBNFLexeme.terminatorSymbol.getDefaultToken();
				case -1:
					return Lexeme._eof.getDefaultToken();
				default:
					if (Character.isWhitespace(i)) {
						continue;
					}

					// TODO: Lexeme exception
					throw new RuntimeException();
			}
		}
	}
}
