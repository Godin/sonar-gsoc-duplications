package org.sonar.duplications.statement;

import java.util.List;

import org.sonar.duplications.token.Token;
import org.sonar.duplications.token.TokenQueue;

/**
 * All implementation of this base class will consume tokens from token queue
 * as per defined match rule and append the matched tokens to a token List provided externally
 * 
 * @author sharif
 *
 */
public abstract class TokenMatcher {
	protected boolean isOptional;

	protected TokenMatcher(boolean isOptional) {
		super();
		this.isOptional = isOptional;
	}

	public abstract boolean matchToken(TokenQueue tokenQueue,
			List<Token> matchedTokenList);
}
