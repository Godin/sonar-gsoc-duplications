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

import org.junit.Test;
import org.sonar.channel.CodeReader;

import static org.junit.Assert.assertThat;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

public class TokenChannelTest {

  @Test
  public void shouldConsume() {
    TokenChannel channel = new TokenChannel("ABC");
    TokenQueue output = new TokenQueue();
    assertThat(channel.consume(new CodeReader("ABCD"), output), is(true));

    assertThat(output, hasItem(new Token("ABC", 1, 0)));
  }

  @Test
  public void shouldNotConsume() {
    TokenChannel channel = new TokenChannel("ABC");
    TokenQueue output = new TokenQueue();
    assertThat(channel.consume(new CodeReader("123"), output), is(false));

    assertThat(output.size(), is(0));
  }

}
