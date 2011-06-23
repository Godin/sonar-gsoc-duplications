package org.sonar.duplications.statement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonar.duplications.api.Token;

/**
 * match everything between two token pair
 * 
 * @author sharif
 *
 */
public class BridgeTokenMatcher extends TokenMatcher {
	
	private String tokenBridgePair[];
	private Map<String, Integer> tokenPairMap = new HashMap<String, Integer>();

	public BridgeTokenMatcher(String[] tokenBridgePair) {
		super(false);
		this.tokenBridgePair = tokenBridgePair;
	}

	public BridgeTokenMatcher(boolean isOptional, String[] tokenBridgePair) {
		super(isOptional);
		this.tokenBridgePair = tokenBridgePair;
	}
	
	@Override
	public boolean matchToken(TokenQueue tokenQueue, List<Token> matchedTokenList){
		int nextTokenIndex = 1;
		Token nextToken = tokenQueue.lookAhead(nextTokenIndex);
		
		//starting of the bridge not match
		if(!nextToken.getNormalizedContent().equals(tokenBridgePair[0])){
			return false || isOptional;
		}
		
		while(nextToken != Token.EMPTY_TOKEN){
			if(tokenBridgePair != null && 
					(tokenBridgePair[0].equals(nextToken.getNormalizedContent())
					|| tokenBridgePair[1].equals(nextToken.getNormalizedContent()))){
				int count = tokenPairMap.get(nextToken.getNormalizedContent()) == null ?
						0: tokenPairMap.get(nextToken.getNormalizedContent());
				tokenPairMap.put(nextToken.getNormalizedContent(), ++count);
			
				if(isBridgeComplete()){
					//consumes the matched tokens
					for (int i = 1; i <= nextTokenIndex; i++) {
						Token token = tokenQueue.pop();
				        matchedTokenList.add(token);
				    }
					return true;
				}
			}
			nextToken = tokenQueue.lookAhead(++nextTokenIndex);
		}
		
		return false || isOptional;
	}

	private boolean isBridgeComplete(){
		boolean bridgeComplete = true;
		if(!tokenPairMap.isEmpty()){
    		if(tokenPairMap.containsKey(tokenBridgePair[0]) && tokenPairMap.containsKey(tokenBridgePair[1])){
    			//pair is in map
    			if(tokenPairMap.get(tokenBridgePair[0]).intValue() != tokenPairMap.get(tokenBridgePair[1]).intValue()){
    				bridgeComplete = false;
    			}
    		} else {
    			//only a single of the pair is in map
    			bridgeComplete = false;
    		}
		} else
			bridgeComplete = false;
		return bridgeComplete;
	}
	
}
