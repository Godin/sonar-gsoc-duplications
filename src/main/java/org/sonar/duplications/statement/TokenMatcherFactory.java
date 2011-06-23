package org.sonar.duplications.statement;

/**
 * returns instance of different token matcher
 * 
 * @author sharif
 *
 */
public class TokenMatcherFactory {

	public static boolean MATCH_IS_OPTIONAL = true;
	
	/**
	 * match the beginning of a statement
	 * @param tokenToMatch
	 * @return
	 */
	public static TokenMatcher from(String tokenToMatch) {
		return new ExactTokenMatcher(tokenToMatch);
	}

	/**
	 * match the end of a statement
	 * @param endMatchTokens
	 * @return
	 */
	public static TokenMatcher to(String... endMatchTokens) {
		return new UptoTokenMatcher(endMatchTokens);
	}

	/**
	 * match everything between two token pair
	 * 
	 * @param lToken: left token of the pair
	 * @param rToken: right token of the pair
	 * @return
	 */
	public static TokenMatcher bridge(String lToken, String rToken) {
		return new BridgeTokenMatcher(new String[] { lToken, rToken });
	}

	/**
	 * 
	 *  match everything between two token pair
	 *  
	 * @param isOptional: if true the match is optional, 
	 * i.e., if no such bridge is found, tokens matched by the previous matchers will lead to a valid statement 
	 * @param lToken: left token of the pair
	 * @param rToken: right token of the pair
	 * @return
	 */
	public static TokenMatcher bridge(boolean isOptional, String lToken, String rToken) {
		return new BridgeTokenMatcher(isOptional, new String[] { lToken, rToken });
	}

	/**
	 * match to any token, this just consumes a specified number of token 
	 * 
	 * @param numberOfTokenToMatch
	 * @return
	 */
	public static TokenMatcher nextAnyToken(int numberOfTokenToMatch) {
		return new AnyTokenMatcher(numberOfTokenToMatch);
	}

	/**
	 * match the next token from the queue
	 * 
	 * @param tokenToMatch
	 * @return
	 */
	public static TokenMatcher nextThisToken(String tokenToMatch) {
		return new ExactTokenMatcher(tokenToMatch);
	}

	/**
	 * match the next token from the queue
	 * 
	 if true the match is optional, 
	 * i.e., if no such match is found, tokens matched by the previous matchers will lead to a valid statement 
	 * @param tokenToMatch
	 * @return
	 */
	public static TokenMatcher nextThisToken(boolean isOptional, String tokenToMatch) {
		return new ExactTokenMatcher(isOptional, tokenToMatch);
	}

}
