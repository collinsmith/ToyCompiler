package com.gmail.collinsmith70.toycompiler.ebnf;

import com.gmail.collinsmith70.toycompiler.bnf.Lexeme;
import com.gmail.collinsmith70.toycompiler.bnf.Token;

public enum EBNFLexeme implements Lexeme {
	_terminalSymbol		("(\".+\"|'.+')"),
	_nonterminalSymbol		("((\\p{L}\\p{M}*)+'?|\\<(\\p{L}\\p{M}*)+'?\\>)"),
	_terminator			("(\\.|;)"),
	_leftparen			("\\("),
	_rightparen			("\\)"),
	_leftbracket		("\\["),
	_rightbracket		("\\]"),
	_leftbrace			("\\{"),
	_rightbrace			("\\}"),
	_comment			("\\Q(*\\E.*\\Q*)\\E"),
	;

	private final String REGEX;
	private final Token DEFAULT_TOKEN;
	
	private EBNFLexeme(String regex) {
		this.REGEX = regex;
		this.DEFAULT_TOKEN = new Token(this);
	}

	@Override
	public String getName() {
		return name();
	}

	@Override
	public int getId() {
		return ordinal();
	}

	@Override
	public String getRegex() {
		return REGEX;
	}

	@Override
	public Token getDefaultToken() {
		return DEFAULT_TOKEN;
	}
}
