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

import java.util.ArrayList;
import java.util.List;

import org.sonar.duplications.DuplicationsException;
import org.sonar.duplications.token.Token;
import org.sonar.duplications.token.TokenQueue;

/**
 * channel that consumes tokens if a statement can be build using those tokens as per given rule the statement is added to the output
 * 
 * @author sharif
 * 
 */
public class StatementChannel {

  private final TokenMatcher[] tokenMatchers;
  private boolean blackHole = false;

  private static int indexInFile = 0;

  private StatementChannel(TokenMatcher... tokenMatchers) {
    this.tokenMatchers = tokenMatchers;
    if (tokenMatchers == null) {
      throw new DuplicationsException("This is mandatory to provide at least one TokenMatcher");
    }
  }

  public static StatementChannel createBlackHole(TokenMatcher... tokenMatchers) {
    StatementChannel channel = new StatementChannel(tokenMatchers);
    channel.blackHole = true;
    return channel;
  }

  public static StatementChannel create(TokenMatcher... tokenMatchers) {
    return new StatementChannel(tokenMatchers);
  }

  public boolean consume(TokenQueue tokenQueue, List<Statement> output) {
    List<Token> matchedTokenList = new ArrayList<Token>();
    for (TokenMatcher tokenMatcher : tokenMatchers) {
      if ( !tokenMatcher.matchToken(tokenQueue, matchedTokenList)) {
        tokenQueue.pushBack(matchedTokenList);
        return false;
      }
    }

    // all matchers were successful, so now build the statement
    if ( !blackHole) {
      output.add(new Statement(matchedTokenList, indexInFile++));
    }
    return true;
  }
}
