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
package org.sonar.duplications.api.channel;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonar.channel.Channel;
import org.sonar.channel.CodeReader;
import org.sonar.duplications.api.Token;

public class LexerChannel extends Channel<List<Token>> {

  private final StringBuilder tmpBuilder = new StringBuilder();
  private final Matcher matcher;

  public LexerChannel(String regex) {
    matcher = Pattern.compile(regex).matcher("");
  }

  @Override
  public final boolean consume(CodeReader code, List<Token> output) {
    if (code.popTo(matcher, tmpBuilder) > 0) {
      String tokenValue = tmpBuilder.toString();
      output.add(new Token(tmpBuilder.toString(), code.getLinePosition(), code.getColumnPosition() - tokenValue.length()));
      tmpBuilder.delete(0, tmpBuilder.length());
      return true;
    }
    return false;
  }
}
