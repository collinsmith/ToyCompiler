package com.gmail.collinsmith70.toycompiler.utils;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class HashMappedSet<E> extends AbstractSet<E> implements MappedSet<E>, Cloneable {
	private HashMap<E, E> map;
	
	public HashMappedSet() {
		map = new HashMap<>();
	}
	
	public HashMappedSet(Collection<? extends E> c) {
		map = new HashMap<>(Math.max((int) (c.size()/.75f) + 1, 16));
		addAll(c);
	}

	public HashMappedSet(int initialCapacity, float loadFactor) {
		map = new HashMap<>(initialCapacity, loadFactor);
	}
	
	public HashMappedSet(int initialCapacity) {
		map = new HashMap<>(initialCapacity);
	}
	
	@Override
	public Iterator<E> iterator() {
		return map.keySet().iterator();
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return map.containsKey(o);
	}

	@Override
	public boolean add(E e) {
		return map.put(e, e) == null;
	}

	@Override
	public boolean remove(Object o) {
		return map.remove(o) == o;
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object clone() throws CloneNotSupportedException {
		try {
			HashMappedSet<E> newSet = (HashMappedSet<E>)super.clone();
			newSet.map = (HashMap<E, E>)map.clone();
			return newSet;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e);
		}
	}
	
	@Override
	public E get(E e) {
		return map.get(e);
	}

	@Override
	public E put(E e) {
		return map.put(e, e);
	}
}
