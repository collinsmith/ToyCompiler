package com.gmail.collinsmith70.toycompiler.utils;

import java.util.Arrays;

/**
 *
 *
 * @author Collin Smith <strong>collinsmith70@gmail.com</strong>
 */
public class ArrayTrie implements Trie {
	/**
	 * Defines the initial size of the trie table when there is no initial
	 * size specified.
	 */
	private static final int DEFAULT_INITIAL_TABLE_SIZE = 256;

	/**
	 * Defines the number of characters that can be used as the first
	 * characters within any given key.
	 */
	private static final int NUM_FIRST_CHARACTERS = 53;

	/**
	 * Translates a given character to its corresponding index within the
	 * {@link #SWITCH} array.
	 *
	 * @param c character to translate
	 *
	 * @return index of the character in the switch array, or {@code -1} if
	 *	there is no corresponding value
	 */
	private static final int charToSwitch(char c) {
		if ('A' <= c && c <= 'Z') {
			return c - 'A';
		} else if ('a' <= c && c <= 'z') {
			return 26 + c - 'a';
		} else if (c == '_') {
			return 52;
		}

		return -1;
	}

	/**
	 * Number of keys contained within this trie.
	 */
	private int numKeys;

	/**
	 * Table which uses a first character as an index and stores a pointer to
	 * the first occurrence of a key using that character in the trie.
	 */
	private final int[] SWITCH;

	/**
	 * Array which stores the keys contained within this ArrayTrie. Keys will
	 * be stored starting at index 1 (index 0 is already contained within the
	 * SWITCH table) with indexes 2..N immediately following, and finally
	 * terminated with a delimiter.
	 */
	private char[] trie;

	/**
	 * Array whose indexes correspond with those of {@link #trie} and
	 * represent pointers to the next index within the trie that contains the
	 * next character. If the next value is undefined (not used yet), then
	 * the key does not exist within the trie yet.
	 */
	private int[] next;

	/**
	 * Points to the first blank position within the trie.
	 */
	private int tail;

	/**
	 * Constructs an ArrayTrie with the default initial table size.
	 */
	public ArrayTrie() {
		this(DEFAULT_INITIAL_TABLE_SIZE);
	}

	/**
	 * Constructs an ArrayTrie with a specified initial table size.
	 *
	 * @param initialSize initial size of the trie in characters (value > 0)
	 */
	public ArrayTrie(int initialSize) {
		if (initialSize <= 0) {
			throw new IllegalArgumentException("Initial size must be greater than 0");
		}

		this.SWITCH = new int[NUM_FIRST_CHARACTERS];
		Arrays.fill(SWITCH, 0, NUM_FIRST_CHARACTERS, -1);
		initialize(initialSize);
	}

	/**
	 * Initializes the trie tables to a new size and copies all of the data
	 * over. The trie tables can only grow in size.
	 *
	 * @param newSize new size of the trie tables
	 */
	private void initialize(int newSize) {
		assert trie == null || trie.length < newSize;

		char[] newTrie = new char[newSize];
		if (trie != null) {
			System.arraycopy(trie, 0, newTrie, 0, tail);
		}

		this.trie = newTrie;

		int[] newNext = new int[newSize];
		if (next != null) {
			System.arraycopy(next, 0, newNext, 0, tail);
		}

		Arrays.fill(newNext, tail, newSize, -1);
		this.next = newNext;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int numKeys() {
		return numKeys;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmpty() {
		return numKeys() == 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsKey(char sentinel, String key) {
		if (key == null) {
			throw new NullPointerException("Key cannot be null");
		} else if (key.isEmpty()) {
			return false;
		}

		int switchIndex = charToSwitch(key.charAt(0));
		if (switchIndex == -1) {
			return false;
		}

		if (SWITCH[switchIndex] == -1) {
			return false;
		}

		assert 0 <= SWITCH[switchIndex] : "Found a negative switch pointer";

		int i;
		int pos = SWITCH[switchIndex];
		keyIterator: for (i = 1; i < key.length(); i++) {
			nextIterator: while (true) {
				if (trie[pos] == key.charAt(i)) {
					pos++;
					continue keyIterator;
				} else if (0 <= next[pos]) {
					pos = next[pos];
					continue nextIterator;
				} else {
					return false;
				}
			}
		}

		if (trie[pos] == sentinel) {
			return true;
		}

		while (0 <= next[pos]) {
			pos = next[pos];
			if (trie[pos] == sentinel) {
				return true;
			}
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put(char sentinel, String key) {
		if (key == null) {
			throw new NullPointerException("Key cannot be null");
		} else if (key.isEmpty()) {
			throw new IllegalArgumentException("Empty keys are not allowed");
		}

		int switchIndex = charToSwitch(key.charAt(0));
		if (switchIndex == -1) {
			throw new IllegalArgumentException("Invalid key format");
		}

		if (SWITCH[switchIndex] == -1) {
			SWITCH[switchIndex] = tail;
			insert(sentinel, key, 1);
			return;
		}

		assert 0 <= SWITCH[switchIndex] : "Found a negative switch pointer";

		int i;
		int pos = SWITCH[switchIndex];
		keyIterator: for (i = 1; i < key.length(); i++) {
			nextIterator: while (true) {
				if (trie[pos] == key.charAt(i)) {
					pos++;
					continue keyIterator;
				} else if (0 <= next[pos]) {
					pos = next[pos];
					continue nextIterator;
				} else {
					next[pos] = tail;
					insert(sentinel, key, i);
					return;
				}
			}
		}

		if (trie[pos] == sentinel) {
			return;
		}

		while (0 <= next[pos]) {
			pos = next[pos];
			if (trie[pos] == sentinel) {
				return;
			}
		}

		checkAndGrow(1);
		next[pos] = tail;
		trie[tail++] = sentinel;
		numKeys++;
	}

	/**
	 * Appends a new key into this trie starting at the tail.
	 *
	 * @param sentinel marker to use to mark the end of the key
	 * @param key string to append
	 * @param pos offset within the key to start appending
	 */
	private void insert(char sentinel, String key, int pos) {
		assert key != null && !key.isEmpty();
		assert 0 <= pos;
		checkAndGrow(key.length()-pos+1);
		for (int i = pos; i < key.length(); i++) {
			trie[tail++] = key.charAt(i);
		}

		trie[tail++] = sentinel;
		numKeys++;
	}

	/**
	 * Checks the trie tables to see if they can accommodate the specified
	 * added size, and recursively calls until the tables have grown large
	 * enough.
	 *
	 * @param addedSize amount of added size the trie should accommodate
	 */
	private void checkAndGrow(int addedSize) {
		if ((tail+addedSize) < trie.length) {
			return;
		}

		initialize(trie.length << 1);
		checkAndGrow(numKeys);
	}

	/**
	 * Prints the trie tables for debugging use.
	 */
	public void printTables() {
		System.out.format("Switch:%n");
		for (int i : SWITCH) {
			System.out.format("%-3d ", i);
		}

		System.out.println();

		System.out.format("Trie:");
		for (char c : trie) {
			System.out.format("%-3c ", c);
		}

		System.out.println();

		System.out.format("Next:");
		for (int i : next) {
			if (i == Integer.MIN_VALUE) {
				i = -1;
			}

			System.out.format("%-3d ", i);
		}

		System.out.println();
	}
}