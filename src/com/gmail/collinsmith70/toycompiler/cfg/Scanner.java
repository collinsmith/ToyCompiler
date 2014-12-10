package com.gmail.collinsmith70.toycompiler.cfg;

import java.io.IOException;
import java.io.Reader;

public interface Scanner<E extends Token> {
	E next(Reader reader) throws IOException;
	boolean requiresMark();
}
