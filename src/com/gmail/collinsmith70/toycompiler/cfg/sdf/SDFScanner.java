package com.gmail.collinsmith70.toycompiler.cfg.sdf;

import com.gmail.collinsmith70.toycompiler.cfg.Lexeme;
import com.gmail.collinsmith70.toycompiler.cfg.Scanner;
import com.gmail.collinsmith70.toycompiler.cfg.Token;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.Reader;

public class SDFScanner implements Scanner<Token> {
	public SDFScanner() {
		//...
	}

	@Override
	public boolean requiresMark() {
		return false;
	}

	@Override
	public Token next(Reader r) throws IOException {
		Preconditions.checkNotNull(r);
		reader: while (true) {
			int i = r.read();
			switch (i) {
				case -1:
					return Lexeme._eof;
				case ':':
					return SDFLexicon._definingSymbol;
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
								//assert SDFLexicon._metaIdentifier.getPattern().matcher(metaIdentifierBuilder).matches();
								return SDFLexicon._metaIdentifier.createChild(metaIdentifierBuilder.toString());
						}
					}
				case '_':
					StringBuilder terminalStringBuilder = new StringBuilder(32)
						.appendCodePoint(i);
					switch (i = r.read()) {
						case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'g': case 'h': case 'i': case 'j': case 'k': case 'l': case 'm': case 'n': case 'o': case 'p': case 'q': case 'r': case 's': case 't': case 'u': case 'v': case 'w': case 'x': case 'y': case 'z':
						case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': case 'G': case 'H': case 'I': case 'J': case 'K': case 'L': case 'M': case 'N': case 'O': case 'P': case 'Q': case 'R': case 'S': case 'T': case 'U': case 'V': case 'W': case 'X': case 'Y': case 'Z':
						case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
							terminalStringBuilder.appendCodePoint(i);
							while (true) {
								r.mark(1);
								switch (i = r.read()) {
									case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'g': case 'h': case 'i': case 'j': case 'k': case 'l': case 'm': case 'n': case 'o': case 'p': case 'q': case 'r': case 's': case 't': case 'u': case 'v': case 'w': case 'x': case 'y': case 'z':
									case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': case 'G': case 'H': case 'I': case 'J': case 'K': case 'L': case 'M': case 'N': case 'O': case 'P': case 'Q': case 'R': case 'S': case 'T': case 'U': case 'V': case 'W': case 'X': case 'Y': case 'Z':
									case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
										terminalStringBuilder.appendCodePoint(i);
										break;
									case -1:
									default:
										r.reset();
										//assert SDFLexicon._terminalString.getPattern().matcher(terminalStringBuilder).matches();
										return SDFLexicon._terminalString.createChild(terminalStringBuilder.toString());
								}
							}
						default:
							// TODO: lexeme exception
							throw new RuntimeException();
					}
				default:
					if (isLineSeparator(i, r)) {
						return Lexeme._eol;
					}

					if (Character.isWhitespace(i)) {
						continue;
					}

					// TODO: Lexeme exception
					throw new RuntimeException();
			}
		}
	}

	public boolean isLineSeparator(int codePoint, Reader r) throws IOException {
		switch (codePoint) {
			case '\n':		return true;
			case '\u000B':	return true;
			case '\f':		return true;
			case '\r':
				r.mark(1);
				if (r.read() != '\n') {
					r.reset();
				}

				return true;
			case '\u0085':	return true;
			case '\u2028':	return true;
			case '\u2029':	return true;
			default:
				/*if (Character.getType(codePoint) == Character.LINE_SEPARATOR) {
					return true;
				}*/

				return false;
		}
	}
}
