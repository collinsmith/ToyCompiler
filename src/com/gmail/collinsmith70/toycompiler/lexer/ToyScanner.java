package com.gmail.collinsmith70.toycompiler.lexer;

import com.gmail.collinsmith70.toycompiler.utils.ArrayTrie;
import com.gmail.collinsmith70.toycompiler.utils.Trie;
import java.io.IOException;
import java.io.LineNumberReader;

public class ToyScanner implements Scanner {
	private static final char KEYWORD_VALUE	= '\uFFFF';
	private static final char ID_VALUE		= '\uFFFD';

	private final Trie<Token> TRIE;

	public ToyScanner() {
		TRIE = new ArrayTrie<>();
		ToyTokenTypes.KEYWORDS.stream()
			.forEach((t) -> TRIE.put(KEYWORD_VALUE, t.getRegex(), t.getStaticToken()));
	}

	@Override
	public Token next(LineNumberReader r) {
		int i = -1;
		try {
			Reader:
			while (true) {
				i = r.read();
				if (i == -1) {
					break Reader;
				} else if (Character.isWhitespace(i)) {
					if (i == '\n') {
						return TokenType.DefaultTokenTypes._eol.getStaticToken();
					}

					continue Reader;
				}

				if (Character.isLetter(i) || i == '_') {
					StringBuilder idBuilder = new StringBuilder();
					idBuilder.appendCodePoint(i);
					while (true) {
						r.mark(1);
						i = r.read();
						if (Character.isLetterOrDigit(i) || i == '_') {
							idBuilder.appendCodePoint(i);
							continue;
						}

						r.reset();
						break;
					}

					String id = idBuilder.toString();
					Token token = TRIE.get(KEYWORD_VALUE, id);
					if (token != null) {
						//return ToyTokenTypes.valueOf('_' + id).getStaticToken();
						return token;
					} else if (id.matches(ToyTokenTypes._booleanliteral.getRegex())) {
						return new Token(
							ToyTokenTypes._booleanliteral,
							ToyEvaluator.evaluate(
								ToyTokenTypes._booleanliteral,
								id
							)
						);
					} else if (id.matches(ToyTokenTypes._nullliteral.getRegex())) {
						return ToyTokenTypes._nullliteral.getStaticToken();
					} else {
						assert id.matches(ToyTokenTypes._id.getRegex());
						TRIE.put(ID_VALUE, id, null);
						return new Token(
							ToyTokenTypes._id,
							ToyEvaluator.evaluate(
								ToyTokenTypes._id,
								id
							)
						);
					}
				} else if (Character.isDigit(i)) {
					int intVal;
					int origVal = i;
					if (i == '0') {
						r.mark(2);
						i = r.read();
						if (i == 'x' || i == 'X') {
							//StringBuilder hexBuilder = new StringBuilder(intBuilder);
							//hexBuilder.appendCodePoint(i);

							i = r.read();
							if (isHexDigit(i)) {
								intVal = getHexVal(i);
								//hexBuilder.appendCodePoint(i);
								while (true) {
									r.mark(1);
									i = r.read();
									if (isHexDigit(i)) {
										intVal <<= 4;
										intVal += getHexVal(i);
										//hexBuilder.appendCodePoint(i);
										continue;
									}

									r.reset();
									break;
								}

								//assert hexBuilder.toString().matches(ToyTokenTypes._integerliteral.getRegex());
								return new Token(
									ToyTokenTypes._integerliteral,
									ToyEvaluator.evaluate(
										ToyTokenTypes._integerliteral,
										intVal
									)
								);
							}

							r.reset();

							intVal = 0;
							//assert intBuilder.toString().matches(ToyTokenTypes._integerliteral.getRegex());
							return new Token(
								ToyTokenTypes._integerliteral,
								ToyEvaluator.evaluate(
									ToyTokenTypes._integerliteral,
									intVal
								)
							);
						}

						r.reset();
					}

					StringBuilder doubleBuilder = new StringBuilder();
					doubleBuilder.appendCodePoint(i);

					intVal = getIntVal(origVal);
					Integer_Loop:
					while (true) {
						r.mark(1);
						i = r.read();
						if (Character.isDigit(i)) {
							intVal = (intVal*10) + getIntVal(i);
							doubleBuilder.appendCodePoint(i);
							continue Integer_Loop;
						}

						Double_Switch:
						switch (i) {
							case '.':
								Double_Loop:
								while (true) {
									r.mark(1);
									i = r.read();
									if (Character.isDigit(i)) {
										doubleBuilder.appendCodePoint(i);
										continue Double_Loop;
									}

									r.reset();
									break Double_Loop;
								}

								String doubleString = doubleBuilder.toString();
								//assert doubleString.matches(ToyTokenTypes._doubleliteral.getRegex());
								return new Token(
									ToyTokenTypes._doubleliteral,
									ToyEvaluator.evaluate(
										ToyTokenTypes._doubleliteral,
										doubleString
									)
								);
							default:
								r.reset();
								break Integer_Loop;
						}
					}

					//assert intBuilder.toString().matches(ToyTokenTypes._integerliteral.getRegex());
					return new Token(
						ToyTokenTypes._integerliteral,
						ToyEvaluator.evaluate(
							ToyTokenTypes._integerliteral,
							intVal
						)
					);
				} else {
					switch (i) {
						case '\"':
							StringBuilder stringBuilder = new StringBuilder();

							String_Loop:
							while (true) {
								i = r.read();
								if (isEOLChar(i)) {
									throw new UnexpectedLexemeException(r.getLineNumber(), stringBuilder.toString());
								} else if (i == '\"') {
									break String_Loop;
								} else {
									stringBuilder.appendCodePoint(i);
								}
							}

							String stringValue = stringBuilder.toString();
							//assert stringValue.matches(ToyTokenTypes._stringliteral.getRegex());
							return new Token(
								ToyTokenTypes._stringliteral,
								stringValue
							);
						case '\'':
							i = r.read();
							char charVal = (char)i;
							i = r.read();
							switch (i) {
								case '\'':
									return new Token(
										ToyTokenTypes._characterliteral,
										ToyEvaluator.evaluate(
											ToyTokenTypes._characterliteral,
											Character.toString(charVal)
										)
									);
								default: throw new UnexpectedLexemeException(r.getLineNumber(), String.valueOf(charVal));
							}
						case '/':
							r.mark(1);
							i = r.read();
							switch (i) {
								case '/':
									while (!isEOLChar(i) && i != -1) {
										i = r.read();
									}

									return next(r);
								case '*':
									Block_Comment_Loop:
									while (i != -1) {
										i = r.read();
										while (i == '*') {
											i = r.read();
											if (i == '/') {
												break Block_Comment_Loop;
											}
										}
									}

									return next(r);
								default:
									r.reset();
									return ToyTokenTypes._division.getStaticToken();
							}
						case '<':
							r.mark(1);
							i = r.read();
							switch (i) {
								case '=': return ToyTokenTypes._lessequal.getStaticToken();
								default:
									r.reset();
									return ToyTokenTypes._less.getStaticToken();
							}
						case '>':
							r.mark(1);
							i = r.read();
							switch (i) {
								case '=': return ToyTokenTypes._greaterequal.getStaticToken();
								default:
									r.reset();
									return ToyTokenTypes._greater.getStaticToken();
							}
						case '=':
							r.mark(1);
							i = r.read();
							switch (i) {
								case '=': return ToyTokenTypes._equal.getStaticToken();
								default:
									r.reset();
									return ToyTokenTypes._assignop.getStaticToken();
							}
						case '!':
							r.mark(1);
							i = r.read();
							switch (i) {
								case '=': return ToyTokenTypes._notequal.getStaticToken();
								default:
									r.reset();
									return ToyTokenTypes._not.getStaticToken();
							}
						case '&':
							r.mark(1);
							i = r.read();
							switch (i) {
								case '&': return ToyTokenTypes._and.getStaticToken();
								default:
									r.reset();
									throw new UnexpectedLexemeException(r.getLineNumber(), String.valueOf((char)i));
							}
						case '|':
							r.mark(1);
							i = r.read();
							switch (i) {
								case '|': return ToyTokenTypes._or.getStaticToken();
								default:
									r.reset();
									throw new UnexpectedLexemeException(r.getLineNumber(), String.valueOf((char)i));
							}
						case '+': return ToyTokenTypes._plus.getStaticToken();
						case '-': return ToyTokenTypes._minus.getStaticToken();
						case '*': return ToyTokenTypes._multiplication.getStaticToken();
						case '%': return ToyTokenTypes._modulus.getStaticToken();
						case ';': return ToyTokenTypes._semicolon.getStaticToken();
						case ',': return ToyTokenTypes._comma.getStaticToken();
						case '.': return ToyTokenTypes._period.getStaticToken();
						case '(': return ToyTokenTypes._leftparen.getStaticToken();
						case ')': return ToyTokenTypes._rightparen.getStaticToken();
						case '[': return ToyTokenTypes._leftbracket.getStaticToken();
						case ']': return ToyTokenTypes._rightbracket.getStaticToken();
						case '{': return ToyTokenTypes._leftbrace.getStaticToken();
						case '}': return ToyTokenTypes._rightbrace.getStaticToken();
					}
				}
			}
		} catch (IOException e) {
		} catch (UnexpectedLexemeException e) {
			System.out.println(e.getMessage());
		} finally {
			if (i == -1) {
				return TokenType.DefaultTokenTypes._eof.getStaticToken();
			}
		}

		return null;
	}

	private boolean isHexDigit(int codePoint) {
		return Character.isDigit(codePoint) || ('A' <= codePoint && codePoint <= 'F') || ('a' <= codePoint && codePoint <= 'f');
	}

	private int getHexVal(int codePoint) {
		switch (codePoint) {
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9': return getIntVal(codePoint);
			case 'a': return 10;
			case 'A': return 10;
			case 'b': return 11;
			case 'B': return 11;
			case 'c': return 12;
			case 'C': return 12;
			case 'd': return 13;
			case 'D': return 13;
			case 'e': return 14;
			case 'E': return 14;
			case 'f': return 15;
			case 'F': return 15;
			default: assert false : "Not a hex character";
		}

		return -1;
	}

	private int getIntVal(int codePoint) {
		switch (codePoint) {
			case '0': return 0;
			case '1': return 1;
			case '2': return 2;
			case '3': return 3;
			case '4': return 4;
			case '5': return 5;
			case '6': return 6;
			case '7': return 7;
			case '8': return 8;
			case '9': return 9;
			default: assert false : "Not an integer character";
		}

		return -1;
	}

	private boolean isEOFChar(int codePoint) {
		switch (codePoint) {
			case '\u0000': // null
			case '\u001A': // EOF (scripts)
				return true;
			default:
				return false;
		}
	}

	private boolean isEOLChar(int codePoint) {
		switch (codePoint) {
			case 0x0A: // \n line feed
			case 0x0D: // \r carraige return
				return true;
			default:
				return false;
		}
	}

	private boolean isWhitespace(int codePoint) {
		switch (codePoint) {
			case '\u0020':
			case '\u0009':
			case '\u000B':
			case '\u000C':
				return true;
			default:
				return false;
		}
	}
}
