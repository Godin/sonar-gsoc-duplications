package org.sonar.duplications.statement;

import java.util.List;

import org.sonar.duplications.api.Token;

/**
 * match upto any of the specified token 
 * 
 * @author sharif
 *
 */
public class UptoTokenMatcher extends TokenMatcher {
	
	private String uptoMatchToken[];
	
	public UptoTokenMatcher(String[] uptoMatchToken) {
		super(false);
		this.uptoMatchToken = uptoMatchToken;
	}
	
	@Override
	public boolean matchToken(TokenQueue tokenQueue, List<Token> matchedTokenList){
		boolean endMatch = false;
		int nextTokenIndex = 1;
		Token nextToken = tokenQueue.lookAhead(nextTokenIndex);
		
		while(nextToken != Token.EMPTY_TOKEN){
			if (uptoMatchToken != null && isEndToken(nextToken, tokenQueue.lookAhead(nextTokenIndex+1), uptoMatchToken)){
				endMatch = true;
				break;
			}
			nextToken = tokenQueue.lookAhead(++nextTokenIndex);
		}
		
		//pop the tokens out and build the statement
		if(endMatch){
			for (int i = 1; i <= nextTokenIndex; i++) {
				Token token = tokenQueue.pop();
		        matchedTokenList.add(token);
		    }
		}
		return endMatch || isOptional;
	}
	
    private boolean isEndToken(Token currentToken, Token nextToken, String[] endsWith) {
		for (String refToken : endsWith) {
			//stop before next token
			if(refToken.startsWith("!") &&
				refToken.substring(1).equals(nextToken.getNormalizedContent())){
				return true;
			}
			if (refToken.equals(currentToken.getNormalizedContent()))
				return true;
		}
		return false;
	}
}
