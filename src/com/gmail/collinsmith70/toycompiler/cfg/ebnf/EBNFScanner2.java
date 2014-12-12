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
				case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'g': case 'h': case 'i': case 'j': case 'k': case 'l': case 'm': case 'n': case 'o': case 'p': case 'q': case 'r': case 's': case 't': case 'u': case 'v': case 'w': case 'x': case 'y': case 'z':
				case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': case 'G': case 'H': case 'I': case 'J': case 'K': case 'L': case 'M': case 'N': case 'O': case 'P': case 'Q': case 'R': case 'S': case 'T': case 'U': case 'V': case 'W': case 'X': case 'Y': case 'Z':
					//assert EBNFLexeme.letter.getPattern().matcher(sb).matches();
					return new Token(EBNFLexeme.letter, (char)i);
				case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
					//assert EBNFLexeme.decimalDigit.getPattern().matcher(sb).matches();
					return new Token(EBNFLexeme.decimalDigit, (char)i);
				case '+':
				case '_':
				case '%':
				case '@':
				case '&':
				case '#':
				case '$':
				case '<':
				case '>':
				case '\\':
				case '^':
				case '`':
				case '~':
					//assert EBNFLexeme.otherCharacter.getPattern().matcher(sb).matches();
					return new Token(EBNFLexeme.otherCharacter, (char)i);
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
					i = r.read();
					switch (i) {
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
					i = r.read();
					switch (i) {
						case ')':
							//sb.appendCodePoint(i);
							//assert EBNFLexeme.endCommentSymbol.getPattern().matcher(sb).matches();
							return EBNFLexeme.endCommentSymbol.getDefaultToken();
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
					switch (i) {
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
				case '\'':
					//assert EBNFLexeme.firstQuoteSymbol.getPattern().matcher(sb).matches();
					return EBNFLexeme.firstQuoteSymbol.getDefaultToken();
				case '\"':
					//assert EBNFLexeme.secondQuoteSymbol.getPattern().matcher(sb).matches();
					return EBNFLexeme.secondQuoteSymbol.getDefaultToken();
				case '?':
					//assert EBNFLexeme.specialSequenceSymbol.getPattern().matcher(sb).matches();
					return EBNFLexeme.specialSequenceSymbol.getDefaultToken();
				case '(':
					r.mark(1);
					i = r.read();
					switch (i) {
						case '*':
							//sb.appendCodePoint(i);
							//assert EBNFLexeme.startCommentSymbol.getPattern().matcher(sb).matches();
							return EBNFLexeme.startCommentSymbol.getDefaultToken();
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
