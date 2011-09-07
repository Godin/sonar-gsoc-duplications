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
package org.sonar.duplications.java;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.number.OrderingComparisons.greaterThan;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.sonar.duplications.DuplicationsTestUtil;
import org.sonar.duplications.token.Token;
import org.sonar.duplications.token.TokenChunker;

import com.google.common.collect.Lists;

/**
 * See <a href="http://java.sun.com/docs/books/jls/third_edition/html/lexical.html">The Java Language Specification, Third Edition: Lexical Structure</a>
 * 
 * TODO Java 7 features: Binary Integer Literals, Using Underscore Characters in Numeric Literals
 * TODO add more complex example
 */
public class JavaTokenProducerTest {

  private TokenChunker chunker = JavaTokenProducer.build();

  /**
   * <a href="http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#3.6">White Space</a>
   */
  @Test
  public void shouldIgnoreWhitespaces() {
    assertThat(chunk(" \t\f\n\r"), isTokens());
  }

  /**
   * <a href="http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#3.7">Comments</a>
   */
  @Test
  public void shouldIgnoreEndOfLineComment() {
    assertThat(chunk("// This is a comment"), isTokens());
    assertThat(chunk("// This is a comment \n and_this_is_not"), isTokens(new Token("and_this_is_not", 2, 1)));
  }

  @Test
  public void shouldIgnoreTraditionalComment() {
    assertThat(chunk("/* This is a comment \n and the second line */"), isTokens());
    assertThat(chunk("/** This is a javadoc \n and the second line */"), isTokens());
    assertThat(chunk("/* this \n comment /* \n // /** ends \n here: */"), isTokens());
  }

  /**
   * <a href="http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#3.8">Identifiers</a>
   */
  @Test
  public void shouldPreserveIdentifiers() {
    assertThat(chunk("String"), isTokens(new Token("String", 1, 0)));
    assertThat(chunk("i3"), isTokens(new Token("i3", 1, 0)));
    assertThat(chunk("MAX_VALUE"), isTokens(new Token("MAX_VALUE", 1, 0)));
    assertThat(chunk("isLetterOrDigit"), isTokens(new Token("isLetterOrDigit", 1, 0)));

    assertThat(chunk("_"), isTokens(new Token("_", 1, 0)));
    assertThat(chunk("_123_"), isTokens(new Token("_123_", 1, 0)));
    assertThat(chunk("_Field"), isTokens(new Token("_Field", 1, 0)));
    assertThat(chunk("_Field5"), isTokens(new Token("_Field5", 1, 0)));

    assertThat(chunk("$"), isTokens(new Token("$", 1, 0)));
    assertThat(chunk("$field"), isTokens(new Token("$field", 1, 0)));

    assertThat(chunk("i2j"), isTokens(new Token("i2j", 1, 0)));
    assertThat(chunk("from1to4"), isTokens(new Token("from1to4", 1, 0)));

    assertThat("identifier with unicode", chunk("αβγ"), isTokens(new Token("αβγ", 1, 0)));
  }

  /**
   * <a href="http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#3.9">Keywords</a>
   */
  @Test
  public void shouldPreserverKeywords() {
    assertThat(chunk("private static final"), isTokens(new Token("private", 1, 0), new Token("static", 1, 8), new Token("final", 1, 15)));
  }

  /**
   * <a href="http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#3.10.1">Integer Literals</a>
   */
  @Test
  public void shouldNormalizeDecimalIntegerLiteral() {
    assertThat(chunk("543"), isNumericLiteral());
    assertThat(chunk("543l"), isNumericLiteral());
    assertThat(chunk("543L"), isNumericLiteral());
  }

  @Test
  public void shouldNormalizeOctalIntegerLiteral() {
    assertThat(chunk("077"), isNumericLiteral());
    assertThat(chunk("077l"), isNumericLiteral());
    assertThat(chunk("077L"), isNumericLiteral());
  }

  @Test
  public void shouldNormalizeHexIntegerLiteral() {
    assertThat(chunk("0xFF"), isNumericLiteral());
    assertThat(chunk("0xFFl"), isNumericLiteral());
    assertThat(chunk("0xFFL"), isNumericLiteral());
  }

  /**
   * <a href="http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#3.10.2">Floating-Point Literals</a>
   */
  @Test
  public void shouldNormalizeDecimalFloatingPointLiteral() {
    // with dot at the end
    assertThat(chunk("1234."), isNumericLiteral());
    assertThat(chunk("1234.E1"), isNumericLiteral());
    assertThat(chunk("1234.e+1"), isNumericLiteral());
    assertThat(chunk("1234.E-1"), isNumericLiteral());
    assertThat(chunk("1234.f"), isNumericLiteral());

    // with dot between
    assertThat(chunk("12.34"), isNumericLiteral());
    assertThat(chunk("12.34E1"), isNumericLiteral());
    assertThat(chunk("12.34e+1"), isNumericLiteral());
    assertThat(chunk("12.34E-1"), isNumericLiteral());

    assertThat(chunk("12.34f"), isNumericLiteral());
    assertThat(chunk("12.34E1F"), isNumericLiteral());
    assertThat(chunk("12.34E+1d"), isNumericLiteral());
    assertThat(chunk("12.34e-1D"), isNumericLiteral());

    // with dot at the beginning
    assertThat(chunk(".1234"), isNumericLiteral());
    assertThat(chunk(".1234e1"), isNumericLiteral());
    assertThat(chunk(".1234E+1"), isNumericLiteral());
    assertThat(chunk(".1234E-1"), isNumericLiteral());

    assertThat(chunk(".1234f"), isNumericLiteral());
    assertThat(chunk(".1234E1F"), isNumericLiteral());
    assertThat(chunk(".1234e+1d"), isNumericLiteral());
    assertThat(chunk(".1234E-1D"), isNumericLiteral());

    // without dot
    assertThat(chunk("1234e1"), isNumericLiteral());
    assertThat(chunk("1234E+1"), isNumericLiteral());
    assertThat(chunk("1234E-1"), isNumericLiteral());

    assertThat(chunk("1234E1f"), isNumericLiteral());
    assertThat(chunk("1234e+1d"), isNumericLiteral());
    assertThat(chunk("1234E-1D"), isNumericLiteral());
  }

  @Test
  public void shouldNormalizeHexadecimalFloatingPointLiteral() {
    // with dot at the end
    assertThat(chunk("0xAF."), isNumericLiteral());
    assertThat(chunk("0XAF.P1"), isNumericLiteral());
    assertThat(chunk("0xAF.p+1"), isNumericLiteral());
    assertThat(chunk("0XAF.p-1"), isNumericLiteral());
    assertThat(chunk("0xAF.f"), isNumericLiteral());

    // with dot between
    assertThat(chunk("0XAF.BC"), isNumericLiteral());
    assertThat(chunk("0xAF.BCP1"), isNumericLiteral());
    assertThat(chunk("0XAF.BCp+1"), isNumericLiteral());
    assertThat(chunk("0xAF.BCP-1"), isNumericLiteral());

    assertThat(chunk("0xAF.BCf"), isNumericLiteral());
    assertThat(chunk("0xAF.BCp1F"), isNumericLiteral());
    assertThat(chunk("0XAF.BCP+1d"), isNumericLiteral());
    assertThat(chunk("0XAF.BCp-1D"), isNumericLiteral());

    // without dot
    assertThat(chunk("0xAFp1"), isNumericLiteral());
    assertThat(chunk("0XAFp+1"), isNumericLiteral());
    assertThat(chunk("0xAFp-1"), isNumericLiteral());

    assertThat(chunk("0XAFp1f"), isNumericLiteral());
    assertThat(chunk("0xAFp+1d"), isNumericLiteral());
    assertThat(chunk("0XAFp-1D"), isNumericLiteral());
  }

  /**
   * <a href="http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#3.10.3">Boolean Literals</a>
   */
  @Test
  public void shouldPreserveBooleanLiterals() {
    assertThat(chunk("true false"), isTokens(new Token("true", 1, 0), new Token("false", 1, 5)));
  }

  /**
   * <a href="http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#3.10.4">Character Literals</a>
   */
  @Test
  public void shouldNormalizeCharacterLiterals() {
    assertThat("single character", chunk("'a'"), isStringLiteral());
    assertThat("escaped LF", chunk("'\\n'"), isStringLiteral());
    assertThat("escaped quote", chunk("'\\''"), isStringLiteral());
    assertThat("octal escape", chunk("'\\177'"), isStringLiteral());
    assertThat("unicode escape", chunk("'\\u03a9'"), isStringLiteral());
  }

  /**
   * <a href="http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#3.10.5">String Literals</a>
   */
  @Test
  public void shouldNormalizeStringLiterals() {
    assertThat("regular string", chunk("\"string\""), isStringLiteral());
    assertThat("empty string", chunk("\"\""), isStringLiteral());
    assertThat("escaped LF", chunk("\"\\n\""), isStringLiteral());
    assertThat("escaped double quotes", chunk("\"string, which contains \\\"escaped double quotes\\\"\""), isStringLiteral());
    assertThat("octal escape", chunk("\"string \\177\""), isStringLiteral());
    assertThat("unicode escape", chunk("\"string \\u03a9\""), isStringLiteral());
  }

  /**
   * <a href="http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#3.10.7">The Null Literal</a>
   */
  @Test
  public void shouldPreserverNullLiteral() {
    assertThat(chunk("null"), isTokens(new Token("null", 1, 0)));
  }

  /**
   * <a href="http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#3.11">Separators</a>
   */
  @Test
  public void shouldPreserveSeparators() {
    assertThat(chunk("(){}[];,."), isTokens(
        new Token("(", 1, 0), new Token(")", 1, 1),
        new Token("{", 1, 2), new Token("}", 1, 3),
        new Token("[", 1, 4), new Token("]", 1, 5),
        new Token(";", 1, 6), new Token(",", 1, 7),
        new Token(".", 1, 8)));
  }

  /**
   * <a href="http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#3.12">Operators</a>
   */
  @Test
  public void shouldPreserveOperators() {
    assertThat(chunk("+="), isTokens(new Token("+", 1, 0), new Token("=", 1, 1)));
    assertThat(chunk("--"), isTokens(new Token("-", 1, 0), new Token("-", 1, 1)));
  }

  @Test
  public void realExamples() {
    File testFile = DuplicationsTestUtil.findFile("/java/MessageResources.java");
    assertThat(chunker.chunk(testFile).size(), greaterThan(0));

    testFile = DuplicationsTestUtil.findFile("/java/RequestUtils.java");
    assertThat(chunker.chunk(testFile).size(), greaterThan(0));
  }

  private static Matcher<List<Token>> isNumericLiteral() {
    return isTokens(new Token("$NUMBER", 1, 0));
  }

  private static Matcher<List<Token>> isStringLiteral() {
    return isTokens(new Token("$CHARS", 1, 0));
  }

  /**
   * @return matcher for list of tokens
   */
  private static Matcher<List<Token>> isTokens(Token... tokens) {
    return is(Arrays.asList(tokens));
  }

  private List<Token> chunk(String sourceCode) {
    return Lists.newArrayList(chunker.chunk(sourceCode));
  }

}
