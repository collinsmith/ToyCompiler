package com.gmail.collinsmith70.toycompiler.parser2;

import java.util.Map;
import java.util.Set;

public interface ParserStatesGenerator {
	Map<Set<ProductionRuleInstance>, State> generateParserTables(Grammar g);
}
