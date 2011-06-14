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

package org.sonar.duplications.api.lexer.channel;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.duplications.api.codeunit.Token;

public class StatementBuilderChannelDisptacher<OUTPUT> extends Channel2<OUTPUT> {

  private static final Logger logger = LoggerFactory.getLogger(StatementBuilderChannelDisptacher.class);
  private final boolean failIfNoChannelToConsumeOneCharacter;

  @SuppressWarnings("rawtypes")
  private final Channel2[] channels;

  @SuppressWarnings("rawtypes")
  public StatementBuilderChannelDisptacher(List<Channel2> channels) {
    this(channels, false);
  }

  @SuppressWarnings("rawtypes")
  public StatementBuilderChannelDisptacher(Channel2... channels) {
    this(Arrays.asList(channels), false);
  }

  @SuppressWarnings("rawtypes")
  public StatementBuilderChannelDisptacher(List<Channel2> channels, boolean failIfNoChannelToConsumeOneCharacter) {
    this.channels = channels.toArray(new Channel2[channels.size()]);
    this.failIfNoChannelToConsumeOneCharacter = failIfNoChannelToConsumeOneCharacter;
  }

  public boolean consume(TokenReader tokenReader, OUTPUT output) {
    Token nextToken = tokenReader.peek();
    while (nextToken != Token.EMPTY_TOKEN) {
      boolean channelConsumed = false;
      for (Channel2<OUTPUT> channel : channels) {
        if (channel.consume(tokenReader, output)) {
          channelConsumed = true;
          break;
        }
      }
      if (!channelConsumed) {
        String message = "None of the channel has been able to handle token '" + tokenReader.peek();
        if (failIfNoChannelToConsumeOneCharacter) {
          throw new IllegalStateException(message);
        }
        logger.debug(message);
        tokenReader.pop();
      }
      nextToken = tokenReader.peek();
    }
    return true;
  }
}