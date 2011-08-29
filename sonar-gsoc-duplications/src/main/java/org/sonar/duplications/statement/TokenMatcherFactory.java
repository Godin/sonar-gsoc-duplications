/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2011 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.duplications.statement;

import org.sonar.duplications.statement.matcher.*;


/**
 * returns instance of different token matcher
 *
 * @author sharif
 */
public final class TokenMatcherFactory {

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
   * @param lToken : left token of the pair
   * @param rToken : right token of the pair
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
