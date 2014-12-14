package com.gmail.collinsmith70.toycompiler.cfg.bnf;

import com.gmail.collinsmith70.toycompiler.cfg.Lexeme;
import com.gmail.collinsmith70.toycompiler.cfg.Scanner;
import com.gmail.collinsmith70.toycompiler.cfg.Token;
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
	public Token next(Reader r) throws IOException {
		Preconditions.checkNotNull(r);
		while (true) {
			int i = r.read();
			//StringBuilder sb = new StringBuilder(1)
			//	.appendCodePoint(i);
			switch (i) {
				case -1:
					return Lexeme._eof;
				case '|':
					//assert BNFLexicon._definitionSeparatorSymbol.getPattern().matcher(sb).matches();
					return BNFLexicon._definitionSeparatorSymbol;
				case ':':
					switch (i = r.read()) {
						case ':':
							//sb.appendCodePoint(i);
							switch (i = r.read()) {
								case '=':
									//sb.appendCodePoint(i);
									//assert BNFLexicon._definingSymbol.getPattern().matcher(sb).matches();
									return BNFLexicon._definingSymbol;
								default:
									// propagate lexeme exception
							}
						default:
						case -1:
							// TODO: lexeme exception
							throw new RuntimeException();
					}
				case '<':
					StringBuilder metaIdentifierBuilder = new StringBuilder(32);
					switch (i = r.read()) {
						case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'g': case 'h': case 'i': case 'j': case 'k': case 'l': case 'm': case 'n': case 'o': case 'p': case 'q': case 'r': case 's': case 't': case 'u': case 'v': case 'w': case 'x': case 'y': case 'z':
						case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': case 'G': case 'H': case 'I': case 'J': case 'K': case 'L': case 'M': case 'N': case 'O': case 'P': case 'Q': case 'R': case 'S': case 'T': case 'U': case 'V': case 'W': case 'X': case 'Y': case 'Z':
							metaIdentifierBuilder.appendCodePoint(i);
							remainder: while (true) {
								switch (i = r.read()) {
									case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'g': case 'h': case 'i': case 'j': case 'k': case 'l': case 'm': case 'n': case 'o': case 'p': case 'q': case 'r': case 's': case 't': case 'u': case 'v': case 'w': case 'x': case 'y': case 'z':
									case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': case 'G': case 'H': case 'I': case 'J': case 'K': case 'L': case 'M': case 'N': case 'O': case 'P': case 'Q': case 'R': case 'S': case 'T': case 'U': case 'V': case 'W': case 'X': case 'Y': case 'Z':
									case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
										metaIdentifierBuilder.appendCodePoint(i);
										break;
									case '>':
										//assert BNFLexicon._metaIdentifier.getPattern().matcher('<' + metaIdentifierBuilder.toString() + '>').matches();
										return BNFLexicon._metaIdentifier.createChild(metaIdentifierBuilder.toString());
									case -1:
									default:
										break remainder;
								}
							}
						case -1:
						default:
							// TODO: syntax exception
							throw new RuntimeException();
					}
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
									//assert BNFLexicon._terminalString.getPattern().matcher((char)quote + terminalStringBuilder.toString() + (char)i).matches();
									return BNFLexicon._terminalString.createChild(terminalStringBuilder.toString());
								}
							default:
								terminalStringBuilder.appendCodePoint(i);
						}
					}
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
