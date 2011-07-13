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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.sonar.duplications.statement.matcher.TokenMatcher;
import org.sonar.duplications.token.Token;
import org.sonar.duplications.token.TokenQueue;

public class StatementChannelTest {

  TokenQueue tokenQueue;
    
  @Test
  public void shouldConsumeTokenFromTokenQueue(){
	  List<Statement> output = consume(TokenMatcherFactory.anyToken());
	  assertThat(tokenQueue.size(), is(0));
	  assertThat(output.size(), is(1));
  }
  
  @Test
  public void shouldNotConsumeTokenFromTokenQueue(){
	  List<Statement> output = consume(TokenMatcherFactory.from("b"));
	  assertThat(tokenQueue.size(), is(1));
	  assertThat(output.size(), is(0));
  }
  
  @Test
  public void outputShouldNotDependOnAmountOfInvocations() {
    List<Statement> output = consume(TokenMatcherFactory.anyToken());
    List<Statement> secondOutput = consume(TokenMatcherFactory.anyToken());

    assertThat(output.size(), is(1));
    assertThat(output.get(0).getIndexInFile(), is(0));
    assertEquals(output, secondOutput);
  }

  private List<Statement> consume(TokenMatcher tokenMatcher) {
    StatementChannel channel = StatementChannel.create(tokenMatcher);
    ArrayList<Statement> output = new ArrayList<Statement>();
	tokenQueue = new TokenQueue();
	tokenQueue.add(new Token("a", 1, 1));
    channel.consume(tokenQueue, output);
    return output;
  }

}
