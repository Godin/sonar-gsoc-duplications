package org.sonar.duplications.api.codeunit.statement;

import java.util.Set;


/**
 * @author sharif
 *
 */
public interface IStatementOracle {

	/**
	 * Determines if the current token ends a statement.
	 * 
	 */
	public boolean isEndOfStatementToken(String token);

	/**
	 * @return Set of token that end a statement.
	 */
	public Set<String> getStatementDelimiters();
}