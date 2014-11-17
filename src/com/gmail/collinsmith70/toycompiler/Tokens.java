package com.gmail.collinsmith70.toycompiler;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * This enumeration defines Token objects for each keyword and operator within
 * the language.
 *
 * @author Collin Smith <strong>collinsmith70@gmail.com</strong>
 */
public enum Tokens implements Token {
	// Keywords
	_bool(),
	_break(),
	_continue(),
	_double(),
	_else(),
	_for(),
	_if(),
	_int(),
	_return(),
	_string(),
	_void(),
	_while(),

	// Operators
	_plus("+"),
	_minus("-"),
	_multiplication("*"),
	_division("/"),
	_modulus("%"),

	// Binary Operators
	_less("<"),
	_lessequal("<="),
	_greater(">"),
	_greaterequal(">="),
	_equal("=="),
	_notequal("!="),
	_and("&&"),
	_or("||"),

	// Unary Operators
	_not("!"),

	// Assignment Operators
	_assignop("="),

	// Punctuation
	_semicolon(";"),
	_comma(","),
	_period("."),

	// Grouping Pairs
	_leftparen("("),
	_rightparen(")"),

	_leftbracket("["),
	_rightbracket("]"),

	_leftbrace("{"),
	_rightbrace("}"),

	// Literals
	_characterliteral("(\'.\')", false),
	_booleanliteral("(true|false)", false),
	_integerliteral("([+-]?(([0-9]+)|(0(x|X)[a-fA-F0-9]+)))", false),
	_doubleliteral("([0-9]+\\.[0-9]*)", false),
	_stringliteral("(\".*\")", false),
	_nullliteral("null", false),

	// Identifiers
	_id("([a-zA-Z][a-zA-Z0-9_]*)", true)
	;

	/**
	 * Set containing the tokens which are keywords.
	 */
	public static final Set<Tokens> KEYWORDS = Collections.unmodifiableSet(EnumSet.range(_bool, _while));

	/**
	 * String representation of the regular expression representing
	 * this default token.
	 */
	private final String REGEX;

	/**
	 * Defines whether or not {@link #REGEX} should be interpreted
	 * literally.
	 */
	private final boolean IS_LITERAL;

	/**
	 * Constructs a token using the name of the token retrieved by invoking
	 * {@link Enum#name()}. This is usually used to represent keywords.
	 */
	Tokens() {
		this(null);
	}

	/**
	 * Constructs a token using a specified regular expression to represent
	 * it. This is usually used to represent operators (e.g., +, -, !, etc.).
	 *
	 * @param regex regular expression representing this token
	 */
	Tokens(String regex) {
		this(regex, true);
	}

	/**
	 * Constructs a token using a specified regular expression which can
	 * be interpreted literally or as a regular expression. This is usually
	 * used to represent literal definitions (e.g., boolean values can be
	 * represented as {@code (true|false)}).
	 *
	 * @param regex regular expression representing this token
	 * @param isLiteral {@code true} to interpret the regular expression
	 *	literally, otherwise {@code false}
	 */
	Tokens(String regex, boolean isLiteral) {
		if (regex != null) {
			this.REGEX = regex;
		} else {
			this.REGEX = name().substring(1);
		}

		this.IS_LITERAL = isLiteral;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getRegex() {
		return REGEX;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isLiteral() {
		return IS_LITERAL;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getId() {
		return DefaultToken.length + ordinal();
	}
}
