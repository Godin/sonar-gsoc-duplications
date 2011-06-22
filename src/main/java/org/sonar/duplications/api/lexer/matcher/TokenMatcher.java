package org.sonar.duplications.api.lexer.matcher;

import java.util.List;

import org.sonar.duplications.api.codeunit.Token;
import org.sonar.duplications.api.lexer.channel.TokenQueue;

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