package org.sonar.duplications.statement;

import org.sonar.duplications.statement.matcher.AnyTokenMatcher;
import org.sonar.duplications.statement.matcher.BridgeTokenMatcher;
import org.sonar.duplications.statement.matcher.ExactTokenMatcher;
import org.sonar.duplications.statement.matcher.ForgiveLastTokenMatcher;
import org.sonar.duplications.statement.matcher.OptTokenMatcher;
import org.sonar.duplications.statement.matcher.TokenMatcher;
import org.sonar.duplications.statement.matcher.UptoTokenMatcher;


/**
 * returns instance of different token matcher
 * 
 * @author sharif
 * 
 */
public class TokenMatcherFactory {

  private TokenMatcherFactory() {
  }

  /**
   * match the beginning of a statement
   * 
   * @param tokenToMatch
   * @return
   */
  public static TokenMatcher from(String tokenToMatch) {
    return new ExactTokenMatcher(tokenToMatch);
  }

  /**
   * match the end of a statement
   * 
   * @param endMatchTokens
   * @return
   */
  public static TokenMatcher to(String... endMatchTokens) {
    return new UptoTokenMatcher(endMatchTokens);
  }

  /**
   * match everything between two token pair
   * 
   * @param lToken
   *          : left token of the pair
   * @param rToken
   *          : right token of the pair
   * @return
   */
  public static TokenMatcher bridge(String lToken, String rToken) {
    return new BridgeTokenMatcher(lToken, rToken);
  }

  /**
   * match to any token, this just consumes a specified number of token
   * 
   * @param numberOfTokenToMatch
   * @return
   */
  public static TokenMatcher anyToken() {
    return new AnyTokenMatcher();
  }

  public static TokenMatcher opt(TokenMatcher optMatcher) {
    return new OptTokenMatcher(optMatcher);
  }

  public static TokenMatcher forgiveLastToken() {
    return new ForgiveLastTokenMatcher();
  }

  /**
   * match the next token from the queue
   * 
   * @param tokenToMatch
   * @return
   */
  public static TokenMatcher token(String tokenToMatch) {
    return new ExactTokenMatcher(tokenToMatch);
  }
}
