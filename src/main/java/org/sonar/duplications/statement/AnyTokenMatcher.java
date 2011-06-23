package org.sonar.duplications.statement;

import java.util.List;

import org.sonar.duplications.token.Token;
import org.sonar.duplications.token.TokenQueue;

/**
 * match any token upto specified number
 * 
 * @author sharif
 *
 */
public class AnyTokenMatcher extends TokenMatcher {
	
	private int numberOfTokenToMatch;
	
	public AnyTokenMatcher(int numberOfTokenToMatch) {
		super(false); //this is always a mandatory match
		this.numberOfTokenToMatch = numberOfTokenToMatch;
	}

	@Override
	public boolean matchToken(TokenQueue tokenQueue, List<Token> matchedTokenList){
		Token nextToken = tokenQueue.lookAhead(numberOfTokenToMatch);
		//pop the tokens out and build the statement
		if(!nextToken.equals(Token.EMPTY_TOKEN)){
			for (int i = 1; i <= numberOfTokenToMatch; i++) {
				Token token = tokenQueue.pop();
		        matchedTokenList.add(token);
		    }
			return true;
		} else
			return false;
	}
}
