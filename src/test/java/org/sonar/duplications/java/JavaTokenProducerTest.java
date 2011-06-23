package org.sonar.duplications.java;

import org.junit.Test;
import org.sonar.duplications.api.Token;
import org.sonar.duplications.java.JavaTokenProducer;
import org.sonar.duplications.token.TokenChunker;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class JavaTokenProducerTest {

  TokenChunker lexer = JavaTokenProducer.build();

  @Test
  public void shouldIgnoreInlineComment() {
    assertThat(lexer.chunk("// This is a comment").size(), is(0));
  }

  @Test
  public void shouldIgnoreMultilinesComment() {
    assertThat(lexer.chunk("/* This is a comment \n and the second line */").size(), is(0));
  }

  @Test
  public void shouldLexIdentifier() {
    assertThat(lexer.chunk("my identifier"), hasItem(new Token("identifier", 1, 3)));
  }

  @Test
  public void shouldLexPonctuators() {
    assertThat(lexer.chunk("./:"), hasItems(new Token(".", 1, 0), new Token("/", 1, 1), new Token(":", 1, 2)));
  }

  @Test
  public void shouldLexAndNormalizeInteger() {
    assertThat(lexer.chunk("452"), hasItems(new Token("INTEGER", 1, 0)));
  }
}
