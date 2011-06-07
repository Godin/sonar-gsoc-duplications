package org.sonar.duplications.java;

import org.junit.Test;
import org.sonar.duplications.api.codeunit.Token;
import org.sonar.duplications.api.lexer.Lexer;
import org.sonar.duplications.api.lexer.family.JavaLexer;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class JavaLexerTest {

  Lexer lexer = JavaLexer.build();

  @Test
  public void shouldIgnoreInlineComment() {
    assertThat(lexer.lex("// This is a comment").size(), is(0));
  }

  @Test
  public void shouldIgnoreMultilinesComment() {
    assertThat(lexer.lex("/* This is a comment \n and the second line */").size(), is(0));
  }

  @Test
  public void shouldLexIdentifier() {
    assertThat(lexer.lex("my identifier"), hasItem(new Token("identifier", 1, 3)));
  }

  @Test
  public void shouldLexPonctuators() {
    assertThat(lexer.lex("./:"), hasItems(new Token(".", 1, 0), new Token("/", 1, 1), new Token(":", 1, 2)));
  }

  @Test
  public void shouldLexAndNormalizeInteger() {
    assertThat(lexer.lex("452"), hasItems(new Token("INTEGER", 1, 0)));
  }
}
