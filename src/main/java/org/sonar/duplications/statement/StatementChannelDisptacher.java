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

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.duplications.api.Statement;
import org.sonar.duplications.api.Token;
import org.sonar.duplications.token.TokenQueue;

public class StatementChannelDisptacher {

  private static final Logger logger = LoggerFactory.getLogger(StatementChannelDisptacher.class);
  private final boolean failIfNoChannelToConsumeOneCharacter;

  private final StatementChannel[] channels;

  public StatementChannelDisptacher(List<StatementChannel> channels) {
    this(channels, false);
  }

  public StatementChannelDisptacher(StatementChannel... channels) {
    this(Arrays.asList(channels), false);
  }

  public StatementChannelDisptacher(List<StatementChannel> channels, boolean failIfNoChannelToConsumeOneCharacter) {
    this.channels = channels.toArray(new StatementChannel[channels.size()]);
    this.failIfNoChannelToConsumeOneCharacter = failIfNoChannelToConsumeOneCharacter;
  }

  public boolean consume(TokenQueue tokenQueue, List<Statement> statements) {
    Token nextToken = tokenQueue.peek();
    while (nextToken != Token.EMPTY_TOKEN) {
      boolean channelConsumed = false;
      for (StatementChannel channel : channels) {
        if (channel.consume(tokenQueue, statements)) {
          channelConsumed = true;
          break;
        }
      }
      if ( !channelConsumed) {
        String message = "None of the channel has been able to handle token '" + tokenQueue.peek();
        if (failIfNoChannelToConsumeOneCharacter) {
          throw new IllegalStateException(message);
        }
        logger.debug(message);
        tokenQueue.pop();
      }
      nextToken = tokenQueue.peek();
    }
    return true;
  }
}