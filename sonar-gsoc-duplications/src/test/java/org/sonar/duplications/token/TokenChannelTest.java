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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sonar.channel.CodeReader;

public class TokenChannelTest {

  @Test
  public void shouldConsume() {
    TokenChannel channel = new TokenChannel("ABC");
    TokenQueue output = mock(TokenQueue.class);
    assertThat(channel.consume(new CodeReader("ABCD"), output), is(true));

    ArgumentCaptor<Token> token = ArgumentCaptor.forClass(Token.class);
    verify(output).add(token.capture());
    assertThat(token.getValue(), is(new Token("ABC", 1, 0)));
    verifyNoMoreInteractions(output);
  }

  @Test
  public void shouldNormalize() {
    TokenChannel channel = new TokenChannel("ABC", "normalized");
    TokenQueue output = mock(TokenQueue.class);
    assertThat(channel.consume(new CodeReader("ABCD"), output), is(true));

    ArgumentCaptor<Token> token = ArgumentCaptor.forClass(Token.class);
    verify(output).add(token.capture());
    assertThat(token.getValue(), is(new Token("normalized", 1, 0)));
    verifyNoMoreInteractions(output);
  }

  @Test
  public void shouldNotConsume() {
    TokenChannel channel = new TokenChannel("ABC");
    TokenQueue output = mock(TokenQueue.class);

    assertThat(channel.consume(new CodeReader("123"), output), is(false));
    verifyZeroInteractions(output);
  }

}
