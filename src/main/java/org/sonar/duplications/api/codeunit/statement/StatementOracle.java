package org.sonar.duplications.api.codeunit.statement;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author sharif
 *
 */
public class StatementOracle implements IStatementOracle {

	private final Set<String> statementDelimiters = new HashSet<String>();

	public StatementOracle(Set<String> statementDelimiters) {
		this.statementDelimiters.addAll(statementDelimiters);
	}

	public boolean isEndOfStatementToken(String token) {
		return statementDelimiters.contains(token);
	}

	public  Set<String> getStatementDelimiters() {
		return (Set<String>) Collections.unmodifiableCollection(statementDelimiters);
	}

}