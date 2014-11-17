package com.gmail.collinsmith70.toycompiler.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Collin Smith <strong>collinsmith70@gmail.com</strong>
 */
public class ArrayTrieTest {
	@Test
	public void testNumKeys() {
		ArrayTrie trie = new ArrayTrie();
		trie.put('!', "Collin");
		trie.put('!', "Cars");
		trie.put('@', "cars");
		trie.put('!', "carson");
		Assert.assertTrue(trie.numKeys() == 4);

		trie = new ArrayTrie();
		trie.put('^', "Collin");
		trie.put('!', "Cars");
		trie.put('@', "cars");
		trie.put('@', "carson");
		trie.put('!', "carson");
		trie.put('!', "carson");
		trie.put('!', "carson");
		Assert.assertTrue(trie.numKeys() == 5);
	}

	@Test
	public void testIsEmpty() {
		ArrayTrie trie = new ArrayTrie();
		Assert.assertTrue(trie.isEmpty());

		trie = new ArrayTrie();
		trie.put('^', "Collin");
		trie.put('!', "Cars");
		trie.put('@', "cars");
		trie.put('!', "carson");
		trie.put('!', "carson");
		trie.put('!', "carson");
		Assert.assertTrue(!trie.isEmpty());
	}

	@Test
	public void testContainsKey() {
		ArrayTrie trie = new ArrayTrie();
		trie.put('^', "Collin");
		trie.put('!', "Cars");
		trie.put('@', "Cars");
		trie.put('@', "cars");
		trie.put('!', "carson");
		trie.put('!', "carson");
		Assert.assertFalse(trie.containsKey('!', "Collin"));
		Assert.assertTrue(trie.containsKey('^', "Collin"));
		Assert.assertFalse(trie.containsKey('^', ""));
		Assert.assertTrue(trie.containsKey('!', "Cars"));
		Assert.assertTrue(trie.containsKey('@', "Cars"));
		Assert.assertFalse(trie.containsKey('!', "carso"));
	}

	@Test
	public void testPut() {
		ArrayTrie trie = new ArrayTrie();
		trie.put('^', "Collin");
		Assert.assertTrue(trie.containsKey('^', "Collin"));
		trie.put('!', "Cars");
		Assert.assertTrue(trie.containsKey('!', "Cars"));
		trie.put('@', "Cars");
		Assert.assertTrue(trie.containsKey('@', "Cars"));
		trie.put('@', "cars");
		Assert.assertTrue(trie.containsKey('@', "cars"));
		trie.put('!', "carson");
		Assert.assertTrue(trie.containsKey('!', "carson"));
		trie.put('!', "carson");
		Assert.assertTrue(trie.containsKey('!', "carson"));
	}
}
