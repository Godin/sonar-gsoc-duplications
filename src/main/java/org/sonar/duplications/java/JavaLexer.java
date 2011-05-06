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
package org.sonar.duplications.java;

import java.util.ArrayList;
import java.util.List;

import org.sonar.channel.Channel;
import org.sonar.duplications.api.Lexer;
import org.sonar.duplications.api.Token;
import org.sonar.duplications.api.channel.BlackHoleLexerChannel;
import org.sonar.duplications.api.channel.LexerChannel;
import org.sonar.duplications.api.channel.NormalizationLexerChannel;

public class JavaLexer extends Lexer {

  @Override
  protected List<Channel<List<Token>>> getLexerChannels() {
    List<Channel<List<Token>>> javaLexerChannels = new ArrayList<Channel<List<Token>>>();

    javaLexerChannels.add(new BlackHoleLexerChannel("\\s"));
    javaLexerChannels.add(new BlackHoleLexerChannel("//[^\\n\\r]*+"));
    javaLexerChannels.add(new BlackHoleLexerChannel("/\\*[\\s\\S]*?\\*/"));
    javaLexerChannels.add(new LexerChannel("\".*?\""));
    javaLexerChannels.add(new LexerChannel("[a-zA-Z_]++"));
    javaLexerChannels.add(new NormalizationLexerChannel("[0-9]++", "INTEGER"));
    javaLexerChannels.add(new LexerChannel("."));
    return javaLexerChannels;
  }
}
