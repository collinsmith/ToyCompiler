package com.gmail.collinsmith70.toycompiler;

/**
 * A token is a specially defined word/character in a programming language.
 * Tokens should have some String of characters representation which can be
 * determined literally or not, as well as some unique identifier that is to be
 * associated with it.
 *
 * @author Collin Smith <strong>collinsmith70@gmail.com</strong>
 */
public interface Token {
	/**
	 * This class represents default tokens which are inherited by every
	 * language.
	 */
	enum DefaultToken implements Token {
		_eof("$")
		;

		/**
		 * Represents the total number of default tokens.
		 */
		public static final int length = DefaultToken.values().length;

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
		 * Constructs a default token using the specified regular expression
		 * and interprets it literally.
		 *
		 * @param regex regular expression representation of the token
		 */
		DefaultToken(String regex) {
			this(regex, true);
		}

		/**
		 * Constructs a default token using the specified regular expression
		 * and flags that the regular expression should be interpreted as
		 * specified.
		 *
		 * @param regex regular expression representation of the token
		 * @param isLiteral {@true} if the regular expression should be
		 *	interpreted literally, otherwise {@code false}
		 */
		DefaultToken(String regex, boolean isLiteral) {
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
			return ordinal();
		}
	}

	/**
	 * Returns the regular expression representing this token.
	 *
	 * @return the regular expression representing this token
	 */
	String getRegex();

	/**
	 * Returns whether or not the regular expression representing this token
	 * should be interpreted literally or not. A token that is not to be
	 * interpreted literally should be interpreted as a regular expression.
	 *
	 * @return {@code true} if it should be, otherwise {@code false}
	 */
	boolean isLiteral();

	/**
	 * Returns the unique identifier associated with this token.
	 *
	 * @return the unique identifier associated with this token
	 */
	int getId();
}
