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
