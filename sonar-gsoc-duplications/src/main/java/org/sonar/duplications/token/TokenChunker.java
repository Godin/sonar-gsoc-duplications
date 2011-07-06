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
import org.sonar.duplications.DuplicationsException;

public final class TokenChunker {

  private final Charset charset;
  private final ChannelDispatcher<TokenQueue> channelDispatcher;

  public static Builder builder() {
    return new Builder();
  }

  private TokenChunker(Builder builder) {
    this.charset = builder.charset;
    this.channelDispatcher = builder.getChannelDispatcher();
  }

  public TokenQueue chunk(String sourceCode) {
    return chunk(new StringReader(sourceCode));
  }

  public TokenQueue chunk(File file) {
    InputStreamReader reader = null;
    try {
      reader = new InputStreamReader(new FileInputStream(file), charset);
      return chunk(reader);
    } catch (Exception e) {
      throw new DuplicationsException("Unable to lex file : " + file.getAbsolutePath(), e);
    } finally {
      IOUtils.closeQuietly(reader);
    }
  }

  public TokenQueue chunk(Reader reader) {
    CodeReader code = new CodeReader(reader, new CodeReaderConfiguration());
    TokenQueue queue = new TokenQueue();
    try {
      channelDispatcher.consume(code, queue);
      return queue;
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

    public TokenChunker build() {
      return new TokenChunker(this);
    }

    public Builder addBlackHoleChannel(String regularExpression) {
      channels.add(new BlackHoleTokenChannel(regularExpression));
      return this;
    }

    public Builder addChannel(String regularExpression) {
      channels.add(new TokenChannel(regularExpression));
      return this;
    }

    public Builder addChannel(String regularExpression, String normalizationValue) {
      channels.add(new TokenChannel(regularExpression, normalizationValue));
      return this;
    }

    private ChannelDispatcher<TokenQueue> getChannelDispatcher() {
      return new ChannelDispatcher<TokenQueue>(channels);
    }

    public Builder setCharset(Charset charset) {
      this.charset = charset;
      return this;
    }

  }

}