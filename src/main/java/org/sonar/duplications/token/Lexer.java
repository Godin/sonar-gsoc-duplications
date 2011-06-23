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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.sonar.channel.Channel;
import org.sonar.channel.ChannelDispatcher;
import org.sonar.channel.CodeReader;
import org.sonar.channel.CodeReaderConfiguration;
import org.sonar.duplications.api.DuplicationsException;
import org.sonar.duplications.api.Token;

public final class Lexer {

  private final Charset charset;
  private final ChannelDispatcher<List<Token>> channelDispatcher;

  public static Builder builder() {
    return new Builder();
  }

  private Lexer(Builder builder) {
    this.charset = builder.charset;
    this.channelDispatcher = builder.getChannelDispatcher();
  }

  public List<Token> lex(String sourceCode) {
    return lex(new StringReader(sourceCode));
  }

  public List<Token> lex(File file) {
    InputStreamReader reader = null;
    try {
      reader = new InputStreamReader(new FileInputStream(file), charset);
      return lex(reader);
    } catch (Exception e) {
      throw new DuplicationsException("Unable to lex file : " + file.getAbsolutePath(), e);
    } finally {
      IOUtils.closeQuietly(reader);
    }
  }

  public List<Token> lex(Reader reader) {
    CodeReader code = new CodeReader(reader, new CodeReaderConfiguration());
    List<Token> tokens = new ArrayList<Token>();
    try {
      channelDispatcher.consume(code, tokens);
      return tokens;
    } catch (Exception e) {
      throw new DuplicationsException("Unable to lex source code at line : " + code.getLinePosition() + " and column : "
          + code.getColumnPosition(), e);
    }
  }

  public static final class Builder {

    private List<Channel> channels = new ArrayList<Channel>();
    private Charset charset = Charset.defaultCharset();

    private Builder() {
    }

    public Lexer build() {
      return new Lexer(this);
    }

    public Builder addBlackHoleChannel(String regularExpression) {
      channels.add(new BlackHoleLexerChannel(regularExpression));
      return this;
    }

    public Builder addChannel(String regularExpression) {
      channels.add(new LexerChannel(regularExpression));
      return this;
    }

    public Builder addChannel(String regularExpression, String normalizationValue) {
      channels.add(new LexerChannel(regularExpression, normalizationValue));
      return this;
    }

    private ChannelDispatcher<List<Token>> getChannelDispatcher() {
      return new ChannelDispatcher<List<Token>>(channels);
    }

    public Builder setCharset(Charset charset) {
      this.charset = charset;
      return this;
    }

  }

}
