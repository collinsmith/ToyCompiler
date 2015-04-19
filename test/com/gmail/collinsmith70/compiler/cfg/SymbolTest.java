package com.gmail.collinsmith70.compiler.cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import junit.framework.Assert;
import org.junit.Test;

public class SymbolTest {
	final Symbol s1 = new SymbolImpl(-1);
	final Symbol s2 = new SymbolImpl(0);
	final Symbol s3 = new SymbolImpl(0);
	final Symbol s4 = new SymbolImpl(1);

	public SymbolTest() {
	}

	@Test
	public void testGetId() {
		Assert.assertTrue(s1.getId() == s1.getId());
		Assert.assertTrue(s2.getId() == s2.getId());
		Assert.assertTrue(s2.getId() == s3.getId());

		Assert.assertTrue(s1.getId() == -1);
		Assert.assertTrue(s2.getId() == 0);
		Assert.assertTrue(s3.getId() == 0);
		Assert.assertTrue(s4.getId() == 1);
	}

	@Test
	public void testEquals() {
		Assert.assertTrue(!s1.equals(null));

		Assert.assertTrue(s1.equals(s1));
		Assert.assertTrue(s2.equals(s2));
		Assert.assertTrue(s3.equals(s3));
		Assert.assertTrue(s4.equals(s4));

		Assert.assertTrue(!s1.equals(new Object()));
		Assert.assertTrue(!s1.equals(""));

		Assert.assertTrue(s2.equals(s3));
		Assert.assertTrue(!s2.equals(s1));
		Assert.assertTrue(!s2.equals(s4));
		Assert.assertTrue(!s1.equals(s4));
	}

	@Test
	public void testHashCode() {
		Assert.assertTrue(s1.hashCode() == s1.hashCode());
		Assert.assertTrue(s1.hashCode() != s2.hashCode());
		Assert.assertTrue(s2.hashCode() == s3.hashCode());
		Assert.assertTrue(s1.hashCode() != s4.hashCode());
	}

	@Test
	public void testCompareTo() {
		Random rand = new Random();

		final int SAMPLE_SIZE = 16;
		List<Symbol> symbols = new ArrayList<>(SAMPLE_SIZE);
		for (int i = 0; i < SAMPLE_SIZE; i++) {
			symbols.add(new SymbolImpl(rand.nextInt()));
		}

		Collections.sort(symbols);
		int previousValue = symbols.get(0).getId();
		for (Symbol s : symbols) {

		}
	}

	public class SymbolImpl extends Symbol {
		public SymbolImpl(int id) {
			super(id);
		}
	}

}
