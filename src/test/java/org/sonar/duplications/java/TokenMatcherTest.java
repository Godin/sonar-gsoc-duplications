package org.sonar.duplications.java;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.sonar.duplications.statement.TokenMatcherFactory;
import org.sonar.duplications.token.Token;
import org.sonar.duplications.token.TokenQueue;

import static org.junit.Assert.assertThat;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

public class TokenMatcherTest {

  @Test
  public void shouldMatchFromToken() {
    List<Token> output = new ArrayList<Token>();
    List<Token> input = new ArrayList<Token>();
    Token t1 = new Token("@", 1, 2);
    Token t2 = new Token("Entity", 1, 2);
    input.add(t1);
    input.add(t2);
    TokenQueue tokenQueue = new TokenQueue(input);

    boolean match = TokenMatcherFactory.from("@").matchToken(tokenQueue, output);
    Assert.assertTrue(match);
    assertThat(output.size(), is(1));
    assertThat(output, hasItems(t1));
  }

  @Test
  public void shouldNotMatchFromToken() {
    List<Token> output = new ArrayList<Token>();
    List<Token> input = new ArrayList<Token>();
    Token t1 = new Token("@", 1, 2);
    Token t2 = new Token("Entity", 1, 2);
    input.add(t1);
    input.add(t2);
    TokenQueue tokenQueue = new TokenQueue(input);

    boolean match = TokenMatcherFactory.from("Entity").matchToken(tokenQueue, output);
    Assert.assertFalse(match); // starts with different token
    assertThat(output.size(), is(0));
  }

  @Test
  public void shouldMatchEndToken() {
    List<Token> output = new ArrayList<Token>();
    List<Token> input = new ArrayList<Token>();
    Token t1 = new Token("int", 1, 2);
    Token t2 = new Token("count", 1, 2);
    Token t3 = new Token(";", 1, 2);
    Token t4 = new Token("public", 1, 2);
    Token t5 = new Token("void", 1, 2);
    input.add(t1);
    input.add(t2);
    input.add(t3);
    input.add(t4);
    input.add(t5);

    TokenQueue tokenQueue = new TokenQueue(input);

    boolean match = TokenMatcherFactory.to(";").matchToken(tokenQueue, output);
    Assert.assertTrue(match);
    assertThat(output.size(), is(3));
    assertThat(output, hasItems(t1, t2, t3));
  }

  @Test
  public void shouldNotMatchEndToken() {
    List<Token> output = new ArrayList<Token>();
    List<Token> input = new ArrayList<Token>();
    Token t1 = new Token("int", 1, 2);
    Token t2 = new Token("count", 1, 2);
    Token t3 = new Token("=", 1, 2);
    Token t4 = new Token("0", 1, 2);
    Token t5 = new Token("}", 1, 2);
    input.add(t1);
    input.add(t2);
    input.add(t3);
    input.add(t4);
    input.add(t5);

    TokenQueue tokenQueue = new TokenQueue(input);

    boolean match = TokenMatcherFactory.to(";").matchToken(tokenQueue, output);
    Assert.assertFalse(match); // there was not token as ";"
  }

  @Test
  public void shouldMatchBridge() {
    List<Token> output = new ArrayList<Token>();
    List<Token> input = new ArrayList<Token>();
    Token t1 = new Token("(", 1, 2);
    Token t2 = new Token("getCount", 1, 2);
    Token t3 = new Token("(", 1, 2);
    Token t4 = new Token(")", 1, 2);
    Token t5 = new Token("+", 1, 2);
    Token t6 = new Token("NUMBER", 1, 2);
    Token t7 = new Token("*", 1, 2);
    Token t8 = new Token("NUMBER", 1, 2);
    Token t9 = new Token(")", 1, 2);

    input.add(t1);
    input.add(t2);
    input.add(t3);
    input.add(t4);
    input.add(t5);
    input.add(t6);
    input.add(t7);
    input.add(t8);
    input.add(t9);

    TokenQueue tokenQueue = new TokenQueue(input);

    boolean match = TokenMatcherFactory.bridge("(", ")").matchToken(tokenQueue, output);
    Assert.assertTrue(match);
    assertThat(output.size(), is(9));
  }

  @Test
  public void shouldNotMatchBridge() {
    List<Token> output = new ArrayList<Token>();
    List<Token> input = new ArrayList<Token>();
    Token t1 = new Token("(", 1, 2);
    Token t2 = new Token("getCount", 1, 2);
    Token t3 = new Token("(", 1, 2);
    Token t4 = new Token(")", 1, 2);
    Token t5 = new Token("+", 1, 2);
    Token t6 = new Token("NUMBER", 1, 2);
    Token t7 = new Token("*", 1, 2);
    Token t8 = new Token("NUMBER", 1, 2);
    // Token t9 = new Token(")", 1, 2);

    input.add(t1);
    input.add(t2);
    input.add(t3);
    input.add(t4);
    input.add(t5);
    input.add(t6);
    input.add(t7);
    input.add(t8);
    // input.add(t9);

    TokenQueue tokenQueue = new TokenQueue(input);

    boolean match = TokenMatcherFactory.bridge("(", ")").matchToken(tokenQueue, output);
    Assert.assertFalse(match); // last closing token ")" missing
  }

  @Test
  public void shouldMatchAnyToken() {
    List<Token> output = new ArrayList<Token>();
    List<Token> input = new ArrayList<Token>();
    Token t1 = new Token("int", 1, 2);
    Token t2 = new Token("count", 1, 2);
    Token t3 = new Token("=", 1, 2);
    Token t4 = new Token("0", 1, 2);
    Token t5 = new Token(";", 1, 2);

    input.add(t1);
    input.add(t2);
    input.add(t3);
    input.add(t4);
    input.add(t5);

    TokenQueue tokenQueue = new TokenQueue(input);

    boolean match = TokenMatcherFactory.anyToken().matchToken(tokenQueue, output);
    Assert.assertTrue(match);
    assertThat(output.size(), is(1));
    assertThat(output, hasItems(t1));
  }

  @Test
  public void shouldMatchThisToken() {
    List<Token> output = new ArrayList<Token>();
    List<Token> input = new ArrayList<Token>();
    Token t1 = new Token("int", 1, 2);
    Token t2 = new Token("count", 1, 2);
    Token t3 = new Token("=", 1, 2);
    Token t4 = new Token("0", 1, 2);
    Token t5 = new Token(";", 1, 2);

    input.add(t1);
    input.add(t2);
    input.add(t3);
    input.add(t4);
    input.add(t5);

    TokenQueue tokenQueue = new TokenQueue(input);

    boolean match = TokenMatcherFactory.token("int").matchToken(tokenQueue, output);
    Assert.assertTrue(match);
    assertThat(output.size(), is(1));
    assertThat(output, hasItems(t1));
  }

  @Test
  public void shouldNotMatchThisToken() {
    List<Token> output = new ArrayList<Token>();
    List<Token> input = new ArrayList<Token>();
    Token t1 = new Token("int", 1, 2);
    Token t2 = new Token("count", 1, 2);
    Token t3 = new Token("=", 1, 2);
    Token t4 = new Token("0", 1, 2);
    Token t5 = new Token(";", 1, 2);

    input.add(t1);
    input.add(t2);
    input.add(t3);
    input.add(t4);
    input.add(t5);

    TokenQueue tokenQueue = new TokenQueue(input);

    boolean match = TokenMatcherFactory.token(";").matchToken(tokenQueue, output);
    Assert.assertFalse(match); // since match was not optional
    assertThat(output.size(), is(0));
  }

}
