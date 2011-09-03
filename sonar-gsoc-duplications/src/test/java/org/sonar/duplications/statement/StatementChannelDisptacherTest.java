package org.sonar.duplications.statement;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.sonar.duplications.statement.matcher.TokenMatcher;
import org.sonar.duplications.token.Token;
import org.sonar.duplications.token.TokenQueue;

public class StatementChannelDisptacherTest {

  @Test(expected = IllegalStateException.class)
  public void shouldThrowAnException() {
    TokenMatcher tokenMatcher = mock(TokenMatcher.class);
    StatementChannel channel = StatementChannel.create(tokenMatcher);
    StatementChannelDisptacher dispatcher = new StatementChannelDisptacher(Arrays.asList(channel));
    TokenQueue tokenQueue = mock(TokenQueue.class);
    when(tokenQueue.peek()).thenReturn(new Token("a", 1, 0)).thenReturn(null);
    List<Statement> statements = mock(List.class);

    dispatcher.consume(tokenQueue, statements);
  }

  @Test
  public void shouldConsume() {
    TokenMatcher tokenMatcher = mock(TokenMatcher.class);
    when(tokenMatcher.matchToken(any(TokenQueue.class), anyListOf(Token.class))).thenReturn(true);
    StatementChannel channel = StatementChannel.create(tokenMatcher);
    StatementChannelDisptacher dispatcher = new StatementChannelDisptacher(Arrays.asList(channel));
    TokenQueue tokenQueue = mock(TokenQueue.class);
    when(tokenQueue.peek()).thenReturn(new Token("a", 1, 0)).thenReturn(null);
    List<Statement> statements = mock(List.class);

    assertThat(dispatcher.consume(tokenQueue, statements), is(true));
    verify(tokenQueue, times(2)).peek();
    verifyNoMoreInteractions(tokenQueue);
    verifyNoMoreInteractions(statements);
  }

}
