package com.gmail.collinsmith70.toycompiler.utils;

public interface Trie<E> {
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
	 * The key will then strongly reference  the specified object.
	 *
	 * @param sentinel marker to use to symbolize the key
	 * @param key string to insert
	 * @param data data the key will reference
	 *
	 * @return the old data references by the sentinel-key pair, or
	 *	{@code null} if there was none or {@code null} is the current value
	 *	of the stored data
	 */
	E put(char sentinel, String key, E data);

	/**
	 * Returns the data stored with the specified sentinel-key pair.
	 *
	 * @param sentinel marker used to symbolize the key
	 * @param key string to retrieve
	 *
	 * @return the data stored with the specified sentinel-key pair
	 */
	E get(char sentinel, String key);
}
