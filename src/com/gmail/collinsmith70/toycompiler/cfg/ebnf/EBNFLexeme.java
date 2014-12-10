package com.gmail.collinsmith70.toycompiler.cfg.ebnf;

import com.gmail.collinsmith70.toycompiler.cfg.Lexeme;
import com.gmail.collinsmith70.toycompiler.cfg.Token;
import java.util.Objects;
import java.util.regex.Pattern;

public enum EBNFLexeme implements Lexeme {
	_terminalSymbol("(\"[^'\"]+\"|'[^'\"]+')"),
	_nonterminalSymbol("(\\w+('|`)*)|(\\<(\\w+('|`)*)*\\>)"),
	_assignop("="),
	_alternation("\\|"),

	/*_comment("\\Q(*\\E.*\\Q*)\\E"),
	_concatenation("\\,"),
	_exception("\\-"),
	_leftbrace("\\{"),
	_leftbracket("\\["),
	_leftparen("\\("),
	_repetition("\\*"),
	_rightbrace("\\}"),
	_rightbracket("\\]"),
	_rightparen("\\)"),
	_terminator("(\\.|;)"),*/

	_comma(","),
	_equalsSign("="),
	_verticalLine("|"),
	_asterisk("*"),
	_rightCurlyBracket("}"),
	_rightParen(")"),
	_rightSquareBracket("]"),
	_hyphenMinus("-"),
	_apostrophe("'"),
	_quotationMark("\""),
	_questionMark("?"),
	_leftCurlyBracket("{"),
	_leftParen("("),
	_leftSquareBracket("["),
	_semicolon(";"),

	concatenateSymbol(_comma),
	definingSymbol(_equalsSign),
	definitionSeparatorSymbol(_verticalLine),
	endCommentSymbol(_asterisk, _rightParen),
	endGroupSymbol(_rightParen),
	endOptionSymbol(_rightSquareBracket),
	endRepeatSymbol(_rightCurlyBracket),
	exceptSymbol(_hyphenMinus),
	firstQuoteSymbol(_apostrophe),
	repetitionSymbol(_asterisk),
	secondQuoteSymbol(_quotationMark),
	specialSequenceSymbol(_questionMark),
	startCommentSymbol(_leftParen, _asterisk),
	startGroupSymbol(_leftParen),
	startOptionSymbol(_leftSquareBracket),
	startRepeatSymbol(_leftCurlyBracket),
	terminatorSymbol(_semicolon),
	;

	private final Pattern PATTERN;
	private final Token DEFAULT_TOKEN;

	private EBNFLexeme(String regex) {
		this.PATTERN = Pattern.compile(Objects.requireNonNull(regex), Pattern.UNICODE_CHARACTER_CLASS);
		this.DEFAULT_TOKEN = new Token(this);
	}

	private EBNFLexeme(Lexeme l1) {
	}

	private EBNFLexeme(Lexeme l1, Lexeme l2) {
	}

	private EBNFLexeme(Lexeme l1, Lexeme l3, Lexeme... lexemes) {
	}

	@Override
	public String getName() {
		return name();
	}

	@Override
	public int getId() {
		return ordinal()+1;
	}

	@Override
	public Pattern getPattern() {
		return PATTERN;
	}

	@Override
	public Token getDefaultToken() {
		return DEFAULT_TOKEN;
	}
}
