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
public class OptTokenMatcher extends TokenMatcher {

  private final TokenMatcher matcher;

  public OptTokenMatcher(TokenMatcher matcher) {
    this.matcher = matcher;
  }

  @Override
  public boolean matchToken(TokenQueue tokenQueue, List<Token> matchedTokenList) {
    matcher.matchToken(tokenQueue, matchedTokenList);
    return true;
  }
}
