package com.gmail.collinsmith70.toycompiler.parser2;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.regex.Pattern;

public abstract class AbstractParserGenerator<T> implements ParserGenerator<T> {
	protected static final Pattern NONTERMINAL_PATTERN = Pattern.compile("[A-Z]\\w*:$");

	public AbstractParserGenerator(Charset c, Path p) throws IOException {
		Preconditions.checkNotNull(c);
		Preconditions.checkNotNull(p);
	}

	@Override
	public T generateTables() {
		return null;
	}
}
