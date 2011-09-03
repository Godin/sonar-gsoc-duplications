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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sonar.duplications.token.Token;
import org.sonar.duplications.token.TokenQueue;

/**
 * Consumes everything up to one of the specified tokens.
 */
public class UptoTokenMatcher extends TokenMatcher {

  private final Set<String> uptoMatchTokens = new HashSet<String>();

  public UptoTokenMatcher(String[] uptoMatchTokens) {
    if (uptoMatchTokens == null) {
      throw new IllegalArgumentException();
    }
    if (uptoMatchTokens.length == 0) {
      // otherwise we will always try to consume everything, but will never succeed
      throw new IllegalArgumentException();
    }
    for (String uptoMatchToken : uptoMatchTokens) {
      this.uptoMatchTokens.add(uptoMatchToken);
    }
  }

  @Override
  public boolean matchToken(TokenQueue tokenQueue, List<Token> matchedTokenList) {
    do {
      Token token = tokenQueue.poll();
      matchedTokenList.add(token);
      if (uptoMatchTokens.contains(token.getValue())) {
        return true;
      }
    } while (tokenQueue.peek() != null);
    return false;
  }

}
