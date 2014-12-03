package com.gmail.collinsmith70.toycompiler.parser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class State<E extends Instanceable<E>> implements Iterable<E> {
	private final int ID;
	private final State<E> PARENT;
	private final Map<Symbol, State<E>> TRANSITIONS;
	private final ImmutableList<Symbol> VIABLE_PREFIX;
	private final ImmutableMap<E, E> KERNEL_ITEMS;
	private final ImmutableMap<E, E> CLOSURE_ITEMS;

	public State(
		int id,
		State<E> parent,
		ImmutableList<Symbol> viablePrefix,
		ImmutableMap<E, E> kernelItems,
		ImmutableMap<E, E> closureItems
	) {
		this.ID = id;
		this.PARENT = parent;
		this.TRANSITIONS = new HashMap<>();
		this.VIABLE_PREFIX = Objects.requireNonNull(viablePrefix);
		this.KERNEL_ITEMS = Objects.requireNonNull(kernelItems);
		this.CLOSURE_ITEMS = Objects.requireNonNull(closureItems);
	}

	public int getId() {
		return ID;
	}

	public State<E> getParent() {
		return PARENT;
	}

	public ImmutableList<Symbol> getViablePrefix() {
		return VIABLE_PREFIX;
	}

	public ImmutableMap<E, E> getKernelItems() {
		return KERNEL_ITEMS;
	}

	public ImmutableMap<E, E> getClosureItems() {
		return CLOSURE_ITEMS;
	}

	public boolean transitionExists(Symbol s) {
		return TRANSITIONS.containsKey(Objects.requireNonNull(s));
	}

	public State<E> getTransition(Symbol s) {
		return TRANSITIONS.get(Objects.requireNonNull(s));
	}

	public void putTransition(Symbol s, State<E> state) {
		if (transitionExists(s)) {
			// TODO: throw better exception
			throw new RuntimeException();
		}

		TRANSITIONS.put(s, Objects.requireNonNull(state));
	}

	@Override
	public Iterator<E> iterator() {
		return Iterators.concat(KERNEL_ITEMS.keySet().iterator(), CLOSURE_ITEMS.keySet().iterator());
	}

	public Metadata<E> createMetadata(ImmutableMap<E, E> kernelItems) {
		boolean asserting = false;
		assert asserting = true;
		if (asserting) {
			/**
			 * TODO: This would be a good point to assert that all
			 * ProductionInstance elements of kernelItems exist within
			 * KERNEL_ITEMS and CLOSURE_ITEMS, otherwise this can't be
			 * used to create a child.
			 */
		}

		return new Metadata<>(this, kernelItems);
	}

	public static <E extends Instanceable<E>> Metadata<E> firstMetadata(ImmutableMap<E, E> kernelItems) {
		return new Metadata<>(null, kernelItems);
	}

	public static final class Metadata<E extends Instanceable<E>> {
		private final State<E> PARENT;
		private final ImmutableMap<E, E> KERNEL_ITEMS;
		private final Symbol CURRENT;

		private Metadata(State parent, ImmutableMap<E, E> kernelItems) {
			this.PARENT = parent;
			this.KERNEL_ITEMS = kernelItems;
			this.CURRENT = kernelItems.values().stream().findFirst().get().currentSymbol();

			boolean asserting = false;
			assert asserting = true;
			if (asserting) {
				if (CURRENT == null) {
					if (kernelItems.values().stream().anyMatch(kernelItem -> kernelItem.currentSymbol() != null)) {
						throw new AssertionError("All items in the kernel set must have the same current symbol value", null);
					}
				} else {
					if (kernelItems.values().stream().anyMatch(kernelItem -> !kernelItem.currentSymbol().equals(CURRENT))) {
						throw new AssertionError("All items in the kernel set must have the same current symbol value", null);
					}
				}
			}
		}

		public State<E> getParent() {
			return PARENT;
		}

		public ImmutableMap<E, E> getKernelItems() {
			return KERNEL_ITEMS;
		}

		public Symbol getCurrent() {
			return CURRENT;
		}
	}
}
