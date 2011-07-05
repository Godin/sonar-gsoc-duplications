package org.sonar.duplications.statement.matcher;

import java.util.List;

import org.sonar.duplications.token.Token;
import org.sonar.duplications.token.TokenQueue;

/**
 * match an exact token
 * 
 * @author sharif
 * 
 */
public class ExactTokenMatcher extends TokenMatcher {

  private String tokenToMatch;

  public ExactTokenMatcher(String tokenToMatch) {
    this.tokenToMatch = tokenToMatch;
  }

  @Override
  public boolean matchToken(TokenQueue tokenQueue, List<Token> matchedTokenList) {
    Token nextToken = tokenQueue.peek();
    if (tokenToMatch != null && tokenToMatch.equals(nextToken.getValue())) {
      matchedTokenList.add(tokenQueue.poll());
      return true;
    }
    return false;
  }
}
