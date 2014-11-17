package com.gmail.collinsmith70.toycompiler.utils;

public interface Trie {
	/**
	 * Returns the number of keys inserted into this trie.
	 *
	 * @return the number of keys inserted into this trie
	 */
	int numKeys();

	/**
	 * Returns whether or not this trie contains any keys.
	 *
	 * @return {@code true} if it does, otherwise {@code false}
	 */
	boolean isEmpty();

	/**
	 * Returns whether or not the specified key is contained within this trie
	 * under a given sentinel. Sentinels represent the marker used for the end
	 * of key character.
	 *
	 * @param sentinel marker used to symbolize the finished key
	 * @param key string to check
	 *
	 * @return {@code true} if it is, otherwise {@code false}
	 */
	boolean containsKey(char sentinel, String key);

	/**
	 * Inserts a new key into this trie using the given sentinel to mark it.
	 *
	 * @param sentinel marker to use to symbolize the key
	 * @param key string to insert
	 */
	void put(char sentinel, String key);
}
