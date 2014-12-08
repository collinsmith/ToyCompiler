package com.gmail.collinsmith70.toycompiler.parser.lalr;

import com.gmail.collinsmith70.toycompiler.lexer.Token;
import com.gmail.collinsmith70.toycompiler.lexer.TokenStream;
import com.gmail.collinsmith70.toycompiler.lexer.TokenType;
import com.gmail.collinsmith70.toycompiler.parser.LALRParserTables;
import com.gmail.collinsmith70.toycompiler.parser.Parser;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

public class LALRParser implements Parser {
	private final LALRParserTables TABLES;

	public LALRParser(LALRParserTables tables) {
		this.TABLES = Objects.requireNonNull(tables);
	}

	@Override
	public boolean parse(TokenStream stream, Writer writer) throws IOException {
		Preconditions.checkNotNull(stream);

		int top = 0;
		int state = 0;
		int[] stack = new int[256];

		stack[top] = state;

		int shift = Integer.MIN_VALUE;
		int productionId = Integer.MIN_VALUE;

		Token t;
		int symbolId;
		boolean accepted = false;
		Get_Next_Token:
		while (true) {
			t = stream.next();
			symbolId = TABLES.tokenId(t);
			if (writer != null && symbolId != Integer.MIN_VALUE) {
				writer.write(String.format("%-3d %-32s", symbolId, t));
			}

			Shift_Handler:
			while(true) {
				shift = TABLES.shift(state, symbolId);
				if (shift != Integer.MIN_VALUE) {
					if (writer != null) writer.write(String.format("[shift]%n"));
					state = shift;
					stack[++top] = state;
					continue Get_Next_Token;
				}

				productionId = TABLES.reduce(state, TABLES.tokenId(stream.peek()));
				//System.out.println("State: " + state + "; Token: " + t + "; lookahead: " + stream.peek());
				//System.out.println("reduce: " + productionId);
				//productionId = TABLES.reduce(state);
				//System.out.println("\treduce: " + productionId);
				switch (productionId) {
					case Integer.MIN_VALUE:
						accepted = false;
						if (writer != null) writer.write(String.format("%nReduction undefined in state S%d with lookahead %s%n", state, stream.peek().getTokenType()));
						break Get_Next_Token;
					case 0:
						// When current reduction is back to A0 because EOF is found, we're done
						if (t.getTokenType() == TokenType.DefaultTokenTypes._eof) {
							accepted = true;
							break Get_Next_Token;
						}
					default:
						if (writer != null) writer.write(String.format("[reduce %d]", productionId));
						top -= TABLES.rhsSize(productionId);
						state = TABLES.transition(stack[top], TABLES.nonterminal(productionId));
						stack[++top] = state;
				}
			}
		}

		if (writer != null) {
			writer.write(String.format("%n[%s]", accepted ? "accept" : "reject"));
		}

		return accepted;
	}
}
