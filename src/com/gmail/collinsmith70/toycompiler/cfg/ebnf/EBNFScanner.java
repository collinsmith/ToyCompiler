package com.gmail.collinsmith70.toycompiler.cfg.ebnf;

import com.gmail.collinsmith70.toycompiler.cfg.Lexeme;
import com.gmail.collinsmith70.toycompiler.cfg.Scanner;
import com.gmail.collinsmith70.toycompiler.cfg.Token;
import com.google.common.base.Preconditions;
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
		Preconditions.checkNotNull(r);
		while (true) {
			int i = r.read();
			//StringBuilder sb = new StringBuilder(1)
			//	.appendCodePoint(i);
			switch (i) {
				case -1:
					return Lexeme._eof;
				//case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'g': case 'h': case 'i': case 'j': case 'k': case 'l': case 'm': case 'n': case 'o': case 'p': case 'q': case 'r': case 's': case 't': case 'u': case 'v': case 'w': case 'x': case 'y': case 'z':
				//case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': case 'G': case 'H': case 'I': case 'J': case 'K': case 'L': case 'M': case 'N': case 'O': case 'P': case 'Q': case 'R': case 'S': case 'T': case 'U': case 'V': case 'W': case 'X': case 'Y': case 'Z':
				//	//assert EBNFLexicon.letter.getPattern().matcher(sb).matches();
				//	return new Token(EBNFLexicon.letter, (char)i);
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
								break;
							case -1:
							default:
								r.reset();
								//assert EBNFLexicon.metaIdentifier.getPattern().matcher(metaIdentifierBuilder).matches();
								return EBNFLexicon.metaIdentifier.createChild(metaIdentifierBuilder.toString());
						}
					}
				//case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
				//	//assert EBNFLexicon.decimalDigit.getPattern().matcher(sb).matches();
				//	return new Token(EBNFLexicon.decimalDigit, (char)i);
				case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
					int integerBuilder = (i-'0');
					while (true) {
						r.mark(1);
						switch (i = r.read()) {
							case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
								integerBuilder <<= 1;
								integerBuilder += (i-'0');
								break;
							case -1:
							default:
								r.reset();
								EBNFLexicon.integer.createChild(integerBuilder);
						}
					}
				//case ':'
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
				//	//assert EBNFLexicon.otherCharacter.getPattern().matcher(sb).matches();
				//	return new Token(EBNFLexicon.otherCharacter, (char)i);
				case ',':
					//assert EBNFLexicon.concatenateSymbol.getPattern().matcher(sb).matches();
					return EBNFLexicon.concatenateSymbol;
				case '=':
					//assert EBNFLexicon.definingSymbol.getPattern().matcher(sb).matches();
					return EBNFLexicon.definingSymbol;
				case '|':
				case '!':
					//assert EBNFLexicon.definitionSeparatorSymbol.getPattern().matcher(sb).matches();
					return EBNFLexicon.definitionSeparatorSymbol;
				case '/':
					r.mark(i);
					switch (i = r.read()) {
						case ')':
							//sb.appendCodePoint(i);
							//assert EBNFLexicon.endOptionSymbol.getPattern().matcher(sb).matches();
							return EBNFLexicon.endOptionSymbol;
						case -1:
						default:
							//assert EBNFLexicon.definitionSeparatorSymbol.getPattern().matcher(sb).matches();
							return EBNFLexicon.definitionSeparatorSymbol;
					}
				case '*':
					r.mark(1);
					switch (i = r.read()) {
						//case ')':
						//	//sb.appendCodePoint(i);
						//	//assert EBNFLexicon.endCommentSymbol.getPattern().matcher(sb).matches();
						//	return EBNFLexicon.endCommentSymbol;
						case -1:
						default:
							r.reset();
							//assert EBNFLexicon.repetitionSymbol.getPattern().matcher(sb).matches();
							return EBNFLexicon.repetitionSymbol;
					}
				case ']':
					//assert EBNFLexicon.endOptionSymbol.getPattern().matcher(sb).matches();
					return EBNFLexicon.endOptionSymbol;
				case '}':
					//assert EBNFLexicon.endRepeatSymbol.getPattern().matcher(sb).matches();
					return EBNFLexicon.endRepeatSymbol;
				case ':':
					r.mark(1);
					switch (i = r.read()) {
						case ')':
							//sb.appendCodePoint(i);
							//assert EBNFLexicon.endRepeatSymbol.getPattern().matcher(sb).matches();
							return EBNFLexicon.endRepeatSymbol;
						case -1:
						default:
							r.reset();
							//	//assert EBNFLexicon.otherCharacter.getPattern().matcher(sb).matches();
							//	return new Token(EBNFLexicon.otherCharacter, ':');
							
							// TODO: Lexeme exception
							throw new RuntimeException();
					}
				case '-':
					//assert EBNFLexicon.exceptSymbol.getPattern().matcher(sb).matches();
					return EBNFLexicon.exceptSymbol;
				//case '\'':
				//	//assert EBNFLexicon.firstQuoteSymbol.getPattern().matcher(sb).matches();
				//	return EBNFLexicon.firstQuoteSymbol;
				//case '\"':
				//	//assert EBNFLexicon.secondQuoteSymbol.getPattern().matcher(sb).matches();
				//	return EBNFLexicon.secondQuoteSymbol;
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
									//assert EBNFLexicon.terminalString.getPattern().matcher((char)quote + terminalStringBuilder.toString() + (char)i).matches();
									return EBNFLexicon.terminalString.createChild(terminalStringBuilder.toString());
								}
							default:
								terminalStringBuilder.appendCodePoint(i);
						}
					}
				//case '?':
				//	//assert EBNFLexicon.specialSequenceSymbol.getPattern().matcher(sb).matches();
				//	return EBNFLexicon.specialSequenceSymbol;
				case '?':
					StringBuilder specialSequenceBuilder = new StringBuilder(16);
					while (true) {
						switch (i = r.read()) {
							case -1:
								// TODO: special sequence syntax exception
								throw new RuntimeException();
							case '?':
								//assert EBNFLexicon.terminalString.getPattern().matcher('?' + specialSequenceBuilder.toString() + '?').matches();
								return EBNFLexicon.specialSequence.createChild(specialSequenceBuilder.toString());
							default:
								specialSequenceBuilder.appendCodePoint(i);
						}
					}
				case '(':
					r.mark(1);
					switch (i = r.read()) {
						case '*':
							//sb.appendCodePoint(i);
							//assert EBNFLexicon.startCommentSymbol.getPattern().matcher(sb).matches();
							//return EBNFLexicon.startCommentSymbol;
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
							//assert EBNFLexicon.startOptionSymbol.getPattern().matcher(sb).matches();
							return EBNFLexicon.startOptionSymbol;
						case ':':
							//sb.appendCodePoint(i);
							//assert EBNFLexicon.startRepeatSymbol.getPattern().matcher(sb).matches();
							return EBNFLexicon.startRepeatSymbol;
						case -1:
						default:
							r.reset();
							//assert EBNFLexicon.startGroupSymbol.getPattern().matcher(sb).matches();
							return EBNFLexicon.startGroupSymbol;
					}
				case '[':
					//assert EBNFLexicon.startOptionSymbol.getPattern().matcher(sb).matches();
					return EBNFLexicon.startOptionSymbol;
				case '{':
					//assert EBNFLexicon.startRepeatSymbol.getPattern().matcher(sb).matches();
					return EBNFLexicon.startRepeatSymbol;
				case ';':
				case '.':
					//assert EBNFLexicon.terminatorSymbol.getPattern().matcher(sb).matches();
					return EBNFLexicon.terminatorSymbol;
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
