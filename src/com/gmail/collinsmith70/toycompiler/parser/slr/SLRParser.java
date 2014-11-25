package com.gmail.collinsmith70.toycompiler.parser.slr;

import com.gmail.collinsmith70.toycompiler.lexer.Token;
import com.gmail.collinsmith70.toycompiler.lexer.TokenStream;
import com.gmail.collinsmith70.toycompiler.lexer.TokenType;
import com.gmail.collinsmith70.toycompiler.parser.Parser;
import java.io.IOException;
import java.io.Writer;

public class SLRParser implements Parser {
	private final SLRTables SLR_TABLES;

	public SLRParser(SLRTables tables) {
		this.SLR_TABLES = tables;
	}

	@Override
	public void parse(TokenStream tokenStream, Writer writer) throws IOException {
		int top = 0;
		int state = 0;
		int[] stack = new int[256];

		stack[top] = state;

		int shift = Integer.MIN_VALUE;
		int production = Integer.MIN_VALUE;

		Token t;
		int symbol;
		boolean accepted = false;
		Get_Next_Token:
		while (true) {
			t = tokenStream.next();
			if (t.getTokenType() == TokenType.DefaultTokenTypes._eol) {
				continue;
			}

			symbol = SLR_TABLES.getTokenId(t);
			if (writer != null && symbol != Integer.MIN_VALUE) {
				String s;
				if (t.getValue() != null) {
					s = String.format("%s(%s)", t.getTokenType(), t.getValue());
				} else {
					s = t.getTokenType().toString();
				}

				writer.write(String.format("%-3d %-32s", symbol, s));
			}

			Shift_Handler:
			while(true) {
				shift = SLR_TABLES.shift(state, symbol);
				if (shift != Integer.MIN_VALUE) {
					if (writer != null) writer.write(String.format("[shift]%n"));
					state = shift;
					stack[++top] = state;
					continue Get_Next_Token;
				}

				production = SLR_TABLES.reduce(state);
				switch (production) {
					case Integer.MIN_VALUE:
						accepted = false;
						//System.out.format("\tReduction undefined in table A%d%n", state);
						break Get_Next_Token;
					case 0:
						// When current reduction is back to A0 because EOF is found, we're done
						if (t.getTokenType() == TokenType.DefaultTokenTypes._eof) {
							accepted = true;
							break Get_Next_Token;
						}
					default:
						if (writer != null) writer.write(String.format("[reduce %d]", production));
						top -= SLR_TABLES.getRHSSize(production);
						state = SLR_TABLES.move(stack[top], SLR_TABLES.getNonterminalId(production));
						stack[++top] = state;
				}
			}
		}

		if (writer != null) {
			if (accepted) {
				writer.write(String.format("%n[accept]"));
			} else {
				writer.write(String.format("%n[reject]"));
			}
		}
	}
}
