package org.sonar.duplications.statement.matcher;

import java.util.List;

import org.sonar.duplications.token.Token;
import org.sonar.duplications.token.TokenQueue;

/**
 * match any token upto specified number
 * 
 * @author sharif
 * 
 */
public class ForgiveLastTokenMatcher extends TokenMatcher {

  @Override
  public boolean matchToken(TokenQueue tokenQueue, List<Token> matchedTokenList) {
    matchedTokenList.remove(matchedTokenList.size() - 1);
    return true;
  }
}
