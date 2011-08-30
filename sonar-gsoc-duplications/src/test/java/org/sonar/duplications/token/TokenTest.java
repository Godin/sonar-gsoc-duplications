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

import static org.junit.Assert.assertThat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class TokenTest {

  @Test
  public void shouldBeEqual() {
    Token firstToken = new Token("MyValue", 1, 3);
    Token secondToken = new Token("MyValue", 1, 3);

    assertThat(firstToken, is(secondToken));
  }

  @Test
  public void shouldNotBeEqual() {
    Token firstToken = new Token("MyValue", 1, 3);
    Token secondToken = new Token("MySecondValue", 1, 3);
    Token thirdToken = new Token("MyValue", 3, 3);
    
    assertThat(firstToken, not(is(secondToken)));
    assertThat(firstToken, not(is(thirdToken)));
  }

}
