package com.gmail.collinsmith70.toycompiler.utils;

import java.util.Set;

public interface MappedSet<E> extends Set<E> {
	E get(E e);
	E put(E e);
}