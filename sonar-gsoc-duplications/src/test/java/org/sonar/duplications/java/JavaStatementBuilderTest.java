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
import java.util.List;

import org.junit.Test;
import org.sonar.duplications.DuplicationsTestUtil;
import org.sonar.duplications.statement.Statement;
import org.sonar.duplications.statement.StatementChunker;
import org.sonar.duplications.token.TokenChunker;

public class JavaStatementBuilderTest {

  private TokenChunker tokenChunker = JavaTokenProducer.build();
  private StatementChunker statementChunker = JavaStatementBuilder.build();

  private List<Statement> chunk(String sourceCode) {
    return statementChunker.chunk(tokenChunker.chunk(sourceCode));
  }

  @Test
  public void shouldIgnoreImportStatement() {
    assertThat(chunk("import org.sonar.duplications.java;").size(), is(0));
  }

  @Test
  public void shouldIgnorePackageStatement() {
    assertThat(chunk("package org.sonar.duplications.java;").size(), is(0));
  }

  @Test
  public void shouldHandleAnnotation() {
    List<Statement> statements = chunk("" +
        "@Entity" +
        "@Table(name = \"properties\")" +
        "@Column(updatable = true, nullable = true)");
    assertThat(statements.size(), is(3));
    assertThat(statements.get(0).getValue(), is("@Entity"));
    assertThat(statements.get(1).getValue(), is("@Table(name=$CHARS)"));
    assertThat(statements.get(2).getValue(), is("@Column(updatable=true,nullable=true)"));
  }

  @Test
  public void shouldHandleIf() {
    List<Statement> statements = chunk("if (a > b) { something(); }");
    assertThat(statements.size(), is(2));
    assertThat(statements.get(0).getValue(), is("if(a>b)"));
    assertThat(statements.get(1).getValue(), is("something()"));

    statements = chunk("if (a > b) { something(); } else { somethingOther(); }");
    assertThat(statements.size(), is(4));
    assertThat(statements.get(0).getValue(), is("if(a>b)"));
    assertThat(statements.get(1).getValue(), is("something()"));
    assertThat(statements.get(2).getValue(), is("else"));
    assertThat(statements.get(3).getValue(), is("somethingOther()"));

    statements = chunk("if (a > 0) { something(); } else if (a == 0) { somethingOther(); }");
    assertThat(statements.size(), is(4));
    assertThat(statements.get(0).getValue(), is("if(a>$NUMBER)"));
    assertThat(statements.get(1).getValue(), is("something()"));
    assertThat(statements.get(2).getValue(), is("elseif(a==$NUMBER)"));
    assertThat(statements.get(3).getValue(), is("somethingOther()"));
  }

  @Test
  public void shouldHandleFor() {
    List<Statement> statements = chunk("for (int i = 0; i < 10; i++) { something(); }");
    assertThat(statements.size(), is(2));
    assertThat(statements.get(0).getValue(), is("for(inti=$NUMBER;i<$NUMBER;i++)"));
    assertThat(statements.get(1).getValue(), is("something()"));

    statements = chunk("for (Item item : items) { something(); }");
    assertThat(statements.size(), is(2));
    assertThat(statements.get(0).getValue(), is("for(Itemitem:items)"));
    assertThat(statements.get(1).getValue(), is("something()"));
  }

  @Test
  public void shouldHandleWhile() {
    List<Statement> statements = chunk("while (i < args.length) { something(); }");
    assertThat(statements.size(), is(2));
    assertThat(statements.get(0).getValue(), is("while(i<args.length)"));
    assertThat(statements.get(1).getValue(), is("something()"));

    statements = chunk("while (true);");
    assertThat(statements.size(), is(1));
    assertThat(statements.get(0).getValue(), is("while(true)"));
  }

  @Test
  public void shouldHandleDoWhile() {
    List<Statement> statements = chunk("do { something(); } while (true);");
    assertThat(statements.size(), is(3));
    assertThat(statements.get(0).getValue(), is("do"));
    assertThat(statements.get(1).getValue(), is("something()"));
    assertThat(statements.get(2).getValue(), is("while(true)"));
  }

  @Test
  public void shouldHandleSwitch() {
    List<Statement> statements = chunk("" +
        "switch (month) {" +
        "  case 1 : monthString=\"January\"; break;" +
        "  case 2 : monthString=\"February\"; break;" +
        "  default: monthString=\"Invalid\";");
    assertThat(statements.size(), is(9));
    assertThat(statements.get(0).getValue(), is("switch(month)"));
    assertThat(statements.get(1).getValue(), is("case$NUMBER:"));
    assertThat(statements.get(2).getValue(), is("monthString=$CHARS"));
    assertThat(statements.get(3).getValue(), is("break"));
    assertThat(statements.get(4).getValue(), is("case$NUMBER:"));
    assertThat(statements.get(5).getValue(), is("monthString=$CHARS"));
    assertThat(statements.get(6).getValue(), is("break"));
    assertThat(statements.get(7).getValue(), is("default:"));
    assertThat(statements.get(8).getValue(), is("monthString=$CHARS"));
  }

  @Test
  public void shouldHandleArray() {
    List<Statement> statements = chunk("new Integer[][] { { 1, 2 }, {3, 4} };");
    assertThat(statements.size(), is(4));
    assertThat(statements.get(0).getValue(), is("newInteger[][]"));
    assertThat(statements.get(1).getValue(), is("$NUMBER,$NUMBER"));
    assertThat(statements.get(2).getValue(), is(","));
    assertThat(statements.get(3).getValue(), is("$NUMBER,$NUMBER"));
  }

  @Test
  public void realExamples() {
    File testFile = DuplicationsTestUtil.findFile("/java/MessageResources.java");
    assertThat(statementChunker.chunk(tokenChunker.chunk(testFile)).size(), greaterThan(0));

    testFile = DuplicationsTestUtil.findFile("/java/RequestUtils.java");
    assertThat(statementChunker.chunk(tokenChunker.chunk(testFile)).size(), greaterThan(0));
  }

}
