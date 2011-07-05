package org.sonar.duplications.statement.matcher;

import java.util.List;

import org.sonar.duplications.token.Token;
import org.sonar.duplications.token.TokenQueue;

/**
 * match everything between two token pair
 * 
 * @author sharif
 * 
 */
public class BridgeTokenMatcher extends TokenMatcher {

  private final String lToken;
  private final String rToken;
  private int stack = 0;

  public BridgeTokenMatcher(String lToken, String rToken) {
    this.lToken = lToken;
    this.rToken = rToken;
  }

  @Override
  public boolean matchToken(TokenQueue tokenQueue, List<Token> matchedTokenList) {
    if ( !tokenQueue.isNextTokenValue(lToken)) {
      return false;
    }
    matchedTokenList.add(tokenQueue.poll());
    stack++;
    do {
      if (tokenQueue.isNextTokenValue(lToken)) {
        stack++;
      }
      if (tokenQueue.isNextTokenValue(rToken)) {
        stack--;
      }
      matchedTokenList.add(tokenQueue.poll());
      if (stack == 0) {
        return true;
      }
    } while (tokenQueue.peek() != null);
    return false;
  }
}
