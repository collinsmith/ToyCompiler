package com.gmail.collinsmith70.toycompiler.parser2.lalr;

import com.gmail.collinsmith70.toycompiler.parser2.ProductionRuleInstance;
import com.gmail.collinsmith70.toycompiler.parser2.Symbol;
import java.util.HashSet;
import java.util.Set;

public class LAProductionRuleInstance extends ProductionRuleInstance {
	private final ProductionRuleInstance INSTANCE;
	private final LAProductionRuleInstance PARENT;
	private final Set<Symbol> FOLLOW;
	
	public LAProductionRuleInstance(ProductionRuleInstance instance) {
		this(instance, null);
	}
	
	public LAProductionRuleInstance(ProductionRuleInstance instance, LAProductionRuleInstance parent) {
		super(instance.getAncestor());
		this.INSTANCE = instance;
		this.PARENT = parent;
		this.FOLLOW = new HashSet<>();
	}
	
	public LAProductionRuleInstance getParent() {
		return PARENT;
	}
	
	public ProductionRuleInstance getInstance() {
		return INSTANCE;
	}
	
	public Set<Symbol> getFollowSet() {
		return FOLLOW;
	}
}
