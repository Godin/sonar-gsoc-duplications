package org.sonar.duplications.token;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TokenQueueTest {

  TokenQueue tokenQueue;

  @Before
  public void initTest() {
    List<Token> tokenList = new ArrayList<Token>();
    tokenList.add(new Token("a", 1, 0));
    tokenList.add(new Token("bc", 1, 2));
    tokenList.add(new Token("def", 1, 5));
    tokenQueue = new TokenQueue(tokenList);
  }

  @Test
  public void shouldPeekToken() {
    Token token = tokenQueue.peek();
    assertThat(token, is(new Token("a", 1, 0)));
    assertThat(tokenQueue.size(), is(3));
  }

  @Test
  public void shouldPollToken() {
    Token token = tokenQueue.poll();
    assertThat(token, is(new Token("a", 1, 0)));
    assertThat(tokenQueue.size(), is(2));
  }

  @Test
  public void shouldPushTokenAtBegining() {
    Token pushedToken = new Token("push", 1, 0);
    List<Token> pushedTokenList = new ArrayList<Token>();
    pushedTokenList.add(pushedToken);
    tokenQueue.pushForward(pushedTokenList);
    assertThat(tokenQueue.peek(), is(pushedToken));
    assertThat(tokenQueue.size(), is(4));
  }

}
