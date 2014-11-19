package com.gmail.collinsmith70.toycompiler.parser;

public class ProductionException extends RuntimeException {
	public ProductionException() {
		//...
	}

	public ProductionException(String msg) {
		super(msg);
	}

	public ProductionException(String format, Object... args) {
		super(String.format(format, args));
	}
}
