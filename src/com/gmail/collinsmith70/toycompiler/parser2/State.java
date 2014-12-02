package com.gmail.collinsmith70.toycompiler.parser2;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * Item sets, or states, are created each time a new Symbol is consumed within
 * the right-hand side of a ProductionRule. ProductionRules with the same
 * consumed Symbol are added to the set of kernel item, and closed over. A
 * ProductionRule is must be closed over when the next consumable Symbol is a
 * NonterminalSymbol. Each new ProductionRule that is closed over is added to
 * the set of closure items, and this along with the set of kernel items
 * represents a State.
 *
 * @author Collin Smith <strong>collinsmith70@gmail.com</strong>
 */
public class State<E extends ProductionRuleInstance> implements Iterable<E> {
	/**
	 * Unique identifier for this State.
	 */
	private final int ID;

	/**
	 * Parent of this State.
	 */
	private final State<E> PARENT;

	/**
	 * Mapping of transitions from a Symbol to the next State within this
	 * State.
	 */
	private final Map<Symbol, State<E>> TRANSITIONS; // TODO: Symbol -> TerminalSymbol?

	/**
	 * List of viable prefixes for this State. A Symbol is said to be a viable
	 * prefix if it can directly preceed this state, i.e., it the previously
	 * consumed Symbol for all of the ProductionRules in the set of kernel
	 * items.
	 */
	private final ImmutableList<Symbol> VIABLE_PREFIXES; // TODO: Symbol -> TerminalSymbol?

	/**
	 * Set of ProductionRules which are initially added into this State. Each
	 * kernel item must be closed over and the created ProductionRules added
	 * into {@link #CLOSURE_ITEMS}.
	 */
	private final ImmutableSet<E> KERNEL_ITEMS;

	/**
	 * Set of ProductionRules which are created by closing over each item
	 * contained within {@link #KERNEL_ITEMS}. These items must then be closed
	 * over, recursively, until the set is complete.
	 */
	private final ImmutableSet<E> CLOSURE_ITEMS;

	/**
	 * Constructs a State with the specified unique identifier, parent State,
	 * list of viable prefixes, and sets of kernel and closure items.
	 *
	 * @param id unique identifier for this State
	 * @param parent parent of this State
	 * @param viablePrefixes list of Symbols which can appear before this
	 *	State
	 * @param kernelItems set of ProductionRules which are initially added
	 *	into this State to be closed over
	 * @param closureItems set of ProductionRules which have been created by
	 *	closing over each item within the kernel items
	 */
	public State(
		int id,
		State parent,
		ImmutableList<Symbol> viablePrefixes,
		ImmutableSet<E> kernelItems,
		ImmutableSet<E> closureItems
	) {
		this.ID = id;
		this.PARENT = parent;
		this.TRANSITIONS = new HashMap<>();
		this.VIABLE_PREFIXES = Objects.requireNonNull(viablePrefixes);
		this.KERNEL_ITEMS = Objects.requireNonNull(kernelItems);
		this.CLOSURE_ITEMS = Objects.requireNonNull(closureItems);
	}

	/**
	 * Returns the unique identifier representing this State.
	 *
	 * @return the unique identifier representing this State
	 */
	public int getId() {
		return ID;
	}

	/**
	 * Returns the parent of this State.
	 *
	 * @return the parent of this State, or {@code null} if there is none
	 */
	public State getParent() {
		return PARENT;
	}

	/**
	 * Returns whether or not there is a transition within this table for the
	 * specified Symbol.
	 *
	 * @param s the Symbol to check
	 *
	 * @return {@code true} if there is, otherwise {@code false}
	 */
	public boolean transitionExists(Symbol s) {
		return TRANSITIONS.containsKey(Objects.requireNonNull(s));
	}
	/**
	 * Returns the transition (if it exists) for the specified Symbol within
	 * this State.
	 *
	 * @param s the Symbol to check
	 *
	 * @return the State that the Symbol should transition to, or {@code null}
	 *	if the transition does not exist.
	 */
	public State getTransition(Symbol s) {
		return TRANSITIONS.get(Objects.requireNonNull(s));
	}

	/**
	 * Sets the transition for the specified Symbol to the specified State and
	 * returns the previously specified State if it existed.
	 *
	 * @param symbol the Symbol to set the transition for
	 * @param state the State that the Symbol should transition to
	 */
	public void putTransition(Symbol symbol, State state) {
		if (getTransition(symbol) != null) {
			// TODO: create a more effective exception
			throw new RuntimeException();
		}

		TRANSITIONS.put(symbol, Objects.requireNonNull(state));
	}

	/**
	 * Returns an immutable list of viable prefixes for this State.
	 *
	 * @return an immutable list of viable prefixes for this State
	 */
	public ImmutableList<Symbol> getViablePrefixes() {
		return VIABLE_PREFIXES;
	}

	/**
	 * Returns an immutable set of the kernel items for this State.
	 *
	 * @return an immutable set of the kernel items for this State
	 */
	public ImmutableSet<E> getKernelItems() {
		return KERNEL_ITEMS;
	}

	/**
	 * Returns an immutable set of the closure items for this State.
	 *
	 * @return an immutable set of the closure items for this State
	 */
	public ImmutableSet<E> getClosureItems() {
		return CLOSURE_ITEMS;
	}

	/**
	 * Returns an Iterator which is a concatenation of the iterators of the
	 * sets of kernel and closure items. This will iterate first through the
	 * kernel items, and then through the closure items.
	 *
	 * @return an Iterator which will iterate through all ProductionRules
	 *	in this State
	 */
	@Override
	public Iterator<E> iterator() {
		return Iterators.concat(KERNEL_ITEMS.iterator(), CLOSURE_ITEMS.iterator());
	}

	/**
	 * Returns an instance of a Metadata object which contains data necessary
	 * to create a child State from this State.
	 *
	 * @param symbol the lookahead symbol for this child
	 * @param kernelItems initial items in the state
	 *
	 * @return an instance of a Metadata object containing structural metadata
	 *	for a child State of this State
	 */
	public Metadata getChildMetadata(Symbol symbol, ImmutableSet<E> kernelItems) {
		return new Metadata(this, symbol, kernelItems);
	}

	/**
	 * This class represents structural metadata which is required to form
	 * child States from a States.
	 *
	 * @author Collin Smith <strong>collinsmith70@gmail.com</strong>
	 */
	public static final class Metadata<E extends ProductionRuleInstance> {
		/**
		 * Reference to a parent State of a child State.
		 */
		private final State PARENT;

		/**
		 * The lookahead Symbol for a child State.
		 */
		private final Symbol SYMBOL;

		/**
		 * Set of ProductionRuleInstance which represent the initial items
		 * for a child State.
		 */
		private final ImmutableSet<E> KERNEL_ITEMS;

		/**
		 * Constructs a Metadata object containing the specified parameters
		 * as items in order to construct child States from a parent State.
		 *
		 * @param parent the parent of a child State
		 * @param symbol the lookahead Symbol for a child State
		 * @param kernelItems initial ProductionRuleInstance items which
		 *	represent the kernel items of a child State
		 */
		public Metadata(
			State parent,
			Symbol symbol,
			ImmutableSet<E> kernelItems
		) {
			this.PARENT = parent;
			this.SYMBOL = symbol;
			this.KERNEL_ITEMS = kernelItems;
		}

		/**
		 * Returns the parent State of the constructable child State.
		 *
		 * @return the parent State of the constructable child State
		 */
		public State getParent() {
			return PARENT;
		}

		/**
		 * Returns the lookahead symbol of the constructable child State.
		 *
		 * @return the lookahead symbol of the constructable child State
		 */
		public Symbol getSymbol() {
			return SYMBOL;
		}

		/**
		 * Returns a set of ProductionRuleInstance which represent the
		 * kernel items in the constructable child State.
		 *
		 * @return the set of kernel items for the constructable child State
		 */
		public ImmutableSet<E> getKernelItems() {
			return KERNEL_ITEMS;
		}
	}
}
