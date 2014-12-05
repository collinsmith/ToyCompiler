package com.gmail.collinsmith70.toycompiler.parser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class State<E extends Instanceable<E>> implements Iterable<E> {
	private final int ID;
	private final State<E> PARENT;
	private final Map<Symbol, State<E>> TRANSITIONS;
	private final ImmutableList<Symbol> VIABLE_PREFIX;
	private final ImmutableMap<ProductionRuleInstance, E> KERNEL_ITEMS;
	private final ImmutableMap<ProductionRuleInstance, E> CLOSURE_ITEMS;

	public State(
		int id,
		State<E> parent,
		ImmutableList<Symbol> viablePrefix,
		ImmutableMap<ProductionRuleInstance, E> kernelItems,
		ImmutableMap<ProductionRuleInstance, E> closureItems
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

	public ImmutableMap<ProductionRuleInstance, E> getKernelItems() {
		return KERNEL_ITEMS;
	}

	public ImmutableMap<ProductionRuleInstance, E> getClosureItems() {
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

	public void dump(PrintStream stream) {
		// TODO: Implement the state output
	}

	@Override
	public Iterator<E> iterator() {
		return Iterators.concat(KERNEL_ITEMS.values().iterator(), CLOSURE_ITEMS.values().iterator());
	}

	public Metadata<ProductionRuleInstance, E> createMetadata(ImmutableMap<ProductionRuleInstance, E> kernelItems) {
		Set<ProductionRuleInstance> copy = kernelItems.keySet().stream()
			.map(kernelItem -> kernelItem.getParent())
			.collect(Collectors.toCollection(HashSet::new));
		copy.removeAll(KERNEL_ITEMS.keySet());
		copy.removeAll(CLOSURE_ITEMS.keySet());
		if (!copy.isEmpty()) {
			throw new IllegalArgumentException("Some kernel items parents do not exist within this State");
		}

		return new Metadata<>(this, kernelItems);
	}

	public static <K extends Instanceable<K>, V extends Instanceable<V>> Metadata<K, V> firstMetadata(ImmutableMap<K, V> kernelItems) {
		return new Metadata<>(null, kernelItems);
	}

	public static final class Metadata<K extends Instanceable<K>, V extends Instanceable<V>> {
		private final State<V> PARENT;
		private final ImmutableMap<K, V> KERNEL_ITEMS;
		private final Symbol CURRENT;

		private Metadata(State<V> parent, ImmutableMap<K, V> kernelItems) {
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

		public State<V> getParent() {
			return PARENT;
		}

		public ImmutableMap<K, V> getKernelItems() {
			return KERNEL_ITEMS;
		}

		public Symbol getCurrent() {
			return CURRENT;
		}
	}
}
