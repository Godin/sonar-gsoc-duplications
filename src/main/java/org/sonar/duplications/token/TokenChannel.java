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
package org.sonar.duplications.token;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonar.channel.Channel;
import org.sonar.channel.CodeReader;

class TokenChannel extends Channel<TokenQueue> {

  private final StringBuilder tmpBuilder = new StringBuilder();
  private final Matcher matcher;
  private String normalizationValue;

  public TokenChannel(String regex) {
    matcher = Pattern.compile(regex).matcher("");
  }

  public TokenChannel(String regex, String normalizationValue) {
    this(regex);
    this.normalizationValue = normalizationValue;
  }

  @Override
  public boolean consume(CodeReader code, TokenQueue output) {
    if (code.popTo(matcher, tmpBuilder) > 0) {
      String tokenValue = tmpBuilder.toString();
      int column = code.getColumnPosition() - tokenValue.length();
      if (normalizationValue != null)
        output.add(new Token(normalizationValue, tokenValue, code.getLinePosition(), column));
      else
        output.add(new Token(tokenValue, tokenValue, code.getLinePosition(), column));
      tmpBuilder.delete(0, tmpBuilder.length());
      return true;
    }
    return false;
  }
}
