package com.gmail.collinsmith70.toycompiler.bnf;

import java.io.Reader;

public interface Scanner<E extends Token> {
	E next(Reader reader);
}
