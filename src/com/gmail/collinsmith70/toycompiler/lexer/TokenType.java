package com.gmail.collinsmith70.toycompiler.lexer;

/**
 * A token type is a lexeme in a programming language. Token types should have
 * some String of characters representation which can be determined literally or
 * not, as well as some unique identifier that is to be associated with it.
 *
 * @author Collin Smith <strong>collinsmith70@gmail.com</strong>
 */
public interface TokenType {
	/**
	 * This class represents default tokens types which are inherited by every
	 * language.
	 */
	enum DefaultTokenType implements TokenType {
		_eof("$"),
		_eol("\n")
		;

		/**
		 * Represents the total number of default tokens types.
		 */
		public static final int length = DefaultTokenType.values().length;

		/**
		 * String representation of the regular expression representing
		 * this default token type.
		 */
		private final String REGEX;

		/**
		 * Defines whether or not {@link #REGEX} should be interpreted
		 * literally.
		 */
		private final boolean IS_LITERAL;

		/**
		 * Many token types do not have values, so this variable will
		 * maintain a static copy of those that can be used throughout the
		 * stack.
		 */
		private final Token STATIC_TOKEN;

		/**
		 * Constructs a default type token using the specified regular
		 * expression and interprets it literally.
		 *
		 * @param regex regular expression representation of the token type
		 */
		DefaultTokenType(String regex) {
			this(regex, true);
		}

		/**
		 * Constructs a default token type using the specified regular
		 * expression and flags that the regular expression should be
		 * interpreted as specified.
		 *
		 * @param regex regular expression representation of the token type
		 * @param isLiteral {@true} if the regular expression should be
		 *	interpreted literally, otherwise {@code false}
		 */
		DefaultTokenType(String regex, boolean isLiteral) {
			if (regex != null) {
				this.REGEX = regex;
			} else {
				this.REGEX = name().substring(1);
			}

			this.IS_LITERAL = isLiteral;
			this.STATIC_TOKEN = new Token(this, null);
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
			return ordinal();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Token getStaticToken() {
			return STATIC_TOKEN;
		}
	}

	/**
	 * Returns the regular expression representing this token type.
	 *
	 * @return the regular expression representing this token type
	 */
	String getRegex();

	/**
	 * Returns whether or not the regular expression representing this token
	 * type should be interpreted literally or not. A token type that is not
	 * to be interpreted literally should be interpreted as a regular
	 * expression.
	 *
	 * @return {@code true} if it should be, otherwise {@code false}
	 */
	boolean isLiteral();

	/**
	 * Returns the unique identifier associated with this token type.
	 *
	 * @return the unique identifier associated with this token type
	 */
	int getId();

	/**
	 * Returns a version of this token with no associated value. Literal
	 * values store their value, however lexemes such as a comma have no
	 * value.
	 *
	 * @return statically allocated token of this type with no value
	 */
	Token getStaticToken();
}
