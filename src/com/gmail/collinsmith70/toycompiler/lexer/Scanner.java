package com.gmail.collinsmith70.toycompiler.lexer;

import java.io.LineNumberReader;

public interface Scanner {
	Token next(LineNumberReader r);
}
