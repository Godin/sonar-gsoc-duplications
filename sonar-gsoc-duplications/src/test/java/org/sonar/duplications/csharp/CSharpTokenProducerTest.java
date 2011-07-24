package org.sonar.duplications.csharp;

import java.io.File;

import org.junit.Test;
import org.sonar.duplications.DuplicationsException;
import org.sonar.duplications.csharp.CSharpTokenProducer;
import org.sonar.duplications.token.Token;
import org.sonar.duplications.token.TokenChunker;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class CSharpTokenProducerTest {

  TokenChunker lexer = CSharpTokenProducer.build();
  String newline = System.getProperty("line.separator");

  @Test (expected = DuplicationsException.class)
  public void shouldThroughDuplicationException() {
    lexer.chunk(new File(""));
  }
  
  @Test
  public void shouldIgnoreInlineComment() {
    assertThat(lexer.chunk("// This is a comment").size(), is(0));
    assertThat(lexer.chunk("/// <summary> this is a xml documentation </summary>").size(), is(0));
  }
  
  @Test
  public void shouldIgnoreMultilinesComment() {
    assertThat(lexer.chunk("/* This is a comment \n and the second line */").size(), is(0));
    assertThat(lexer.chunk("g.trim() /* radix */").size(), is(5));
  }

  @Test
  public void shouldIgnoreMultilinesDocumentationComment() {
    assertThat(lexer.chunk("/** This is a comment "+newline+" and the second line */").size(), is(0));
  }
  
  @Test
  public void shouldLexIdentifier() {
    assertThat(lexer.chunk("my identifier"), hasItem(new Token("identifier", 1, 3)));
    assertThat(lexer.chunk("my "+newline+" identifier"), hasItem(new Token("identifier", 2, 1)));
    assertThat(lexer.chunk("int id1 = 0"), hasItem(new Token("id1", 1, 4)));
    assertThat(lexer.chunk("int id1qw = 0"), hasItem(new Token("id1qw", 1, 4)));
  }

  @Test
  public void shouldLexLiteral() {
    assertThat(lexer.chunk("string name = \"Dr. Jones\""), hasItem(new Token("LITERAL", 1, 14)));
    assertThat(lexer.chunk("char[] chars = { '\u0061', '\u0308' }"), hasItem(new Token("LITERAL", 1, 17)));
    assertThat(lexer.chunk("char chA = 'A'"), hasItem(new Token("LITERAL", 1, 11)));
  }
  
  @Test
  public void shouldLexPonctuators() {
    assertThat(lexer.chunk("./:"), hasItems(new Token(".", 1, 0), new Token("/", 1, 1), new Token(":", 1, 2)));
    assertThat(lexer.chunk("#if DEBUG"), hasItems(new Token("#", 1, 0), new Token("if", 1, 1), new Token("DEBUG", 1, 4)));
  }

  @Test
  public void shouldLexAndNormalizeInteger() {
    assertThat(lexer.chunk("452"), hasItems(new Token("INTEGER", 1, 0)));
  }
  
  @Test
  public void shouldLexAndNormalizeDecimal() {
    assertThat(lexer.chunk("45.2"), hasItems(new Token("DECIMAL", 1, 0)));
    assertThat(lexer.chunk(".1"), hasItems(new Token("DECIMAL", 1, 0)));
    assertThat(lexer.chunk("0.2"), hasItems(new Token("DECIMAL", 1, 0)));
    assertThat(lexer.chunk("45.2e-4").size(), is(1));
    assertThat(lexer.chunk("45.2e-4"), hasItems(new Token("DECIMAL", 1, 0)));
    assertThat(lexer.chunk("45.2-4").size(), is(3));
    assertThat(lexer.chunk("45.2-4"), hasItems(new Token("DECIMAL", 1, 0), new Token("-", 1, 4), new Token("INTEGER", 1, 5)));
    
  }
}
