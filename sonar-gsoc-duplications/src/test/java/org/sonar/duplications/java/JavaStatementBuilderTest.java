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
import org.sonar.duplications.DuplicationsException;
import org.sonar.duplications.DuplicationsTestUtil;

import org.sonar.duplications.statement.Statement;
import org.sonar.duplications.statement.StatementChunker;
import org.sonar.duplications.token.TokenChunker;
import org.sonar.duplications.token.TokenQueue;

public class JavaStatementBuilderTest {

  TokenChunker lexer = JavaTokenProducer.build();
  StatementChunker stmtBldr = JavaStatementBuilder.build();

  @Test(expected = DuplicationsException.class)
  public void shouldThroughException() {
    stmtBldr.chunk(null);
  }

  @Test
  public void shouldIgnoreImportStatement() {
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/StatementTest/java/Import.java");
    TokenQueue tokens = lexer.chunk(testFile);
    assertThat(stmtBldr.chunk(tokens).size(), is(0));
  }

  @Test
  public void shouldIgnorePackageStatement() {
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/StatementTest/java/Package.java");
    TokenQueue tokens = lexer.chunk(testFile);
    assertThat(stmtBldr.chunk(tokens).size(), is(0));
  }

  @Test
  public void shouldLexAnnotationStatement() {
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/StatementTest/java/Annotation.java");
    TokenQueue tokens = lexer.chunk(testFile);
    List<Statement> statementList = stmtBldr.chunk(tokens);

    assertThat(statementList.get(0).getValue(), is("@Entity"));
    assertThat(statementList.get(1).getValue(), is("@Table(name=LITERAL)"));
    assertThat(statementList.get(2).getValue(), is("publicclassPropertyextendsBaseIdentifiable"));
    assertThat(statementList.get(3).getValue(), is("@Column(name=LITERAL,updatable=true,nullable=true)"));
    assertThat(statementList.get(4).getValue(), is("privateStringkey"));
    assertThat(statementList.get(5).getValue(), is("@Column(name=LITERAL,updatable=true,nullable=true,length=INTEGER)"));
    assertThat(statementList.get(6).getValue(), is("@Lob"));
    assertThat(statementList.get(7).getValue(), is("privatechar[]value"));
    assertThat(statementList.get(8).getValue(), is("@Override"));
    assertThat(statementList.get(9).getValue(), is("publicIntegergetUserId()"));
    assertThat(statementList.get(10).getValue(), is("returnuserId"));
  }

  @Test
  public void shouldLexIfStatement() {
	File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/StatementTest/java/If.java");
    TokenQueue tokens = lexer.chunk(testFile);
    List<Statement> statementList = stmtBldr.chunk(tokens);

    assertThat(statementList.get(0).getValue(), is("if(getParent()==null)"));
    assertThat(statementList.get(1).getValue(), is("returnlocalName"));
    assertThat(statementList.get(2).getValue(), is("if(id.length()>INTEGER)"));
    assertThat(statementList.get(3).getValue(), is("id+=File.separator"));
    assertThat(statementList.get(4).getValue(), is("elseif(id.length()==-INTEGER)"));
    assertThat(statementList.get(5).getValue(), is("id+=LITERAL"));
    assertThat(statementList.get(6).getValue(), is("if(id.length()==INTEGER)"));
    assertThat(statementList.get(7).getValue(), is("returnlocalname"));
    assertThat(statementList.get(8).getValue(), is("if(id.length()==INTEGER)"));
    assertThat(statementList.get(9).getValue(), is("returnlocalname"));
  }

  @Test
  public void shouldLexForStatement() {
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/StatementTest/java/For.java");
    TokenQueue tokens = lexer.chunk(testFile);
    List<Statement> statementList = stmtBldr.chunk(tokens);

    assertThat(statementList.get(0).getValue(), is("for(inti=INTEGER;i<children.length;i++)"));
    assertThat(statementList.get(1).getValue(), is("total+=i"));
    assertThat(statementList.get(2).getValue(), is("for(inti=INTEGER;i<children.length;i++)"));
    assertThat(statementList.get(3).getValue(), is("total+=i"));
    assertThat(statementList.get(4).getValue(), is("for(inti=INTEGER;i<children.length;i++)"));
    assertThat(statementList.get(5).getValue(), is("total+=i"));
    assertThat(statementList.get(6).getValue(), is("for(Tokentoken:tokenList)"));
    assertThat(statementList.get(7).getValue(), is("System.out.println(token.getNormalizedContent())"));

    // for statement that spans multiple lines
    assertThat(statementList.get(2).getStartLine(), is(5));
    assertThat(statementList.get(2).getEndLine(), is(7));
  }

  @Test
  public void shouldLexWhileStatement() {
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/StatementTest/java/While.java");

    TokenQueue tokens = lexer.chunk(testFile);
    List<Statement> statementList = stmtBldr.chunk(tokens);

    assertThat(statementList.get(0).getValue(), is("while(i<args.length)"));
    assertThat(statementList.get(1).getValue(), is("System.out.print(args[i])"));
    assertThat(statementList.get(2).getValue(), is("i=i+INTEGER"));
    assertThat(statementList.get(3).getValue(), is("while(i<args.length)"));
    assertThat(statementList.get(4).getValue(), is("System.out.print(args[i++])"));
  }

  @Test
  public void shouldLexDoWhileStatement() {
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/StatementTest/java/DoWhile.java");

    TokenQueue tokens = lexer.chunk(testFile);
    List<Statement> statementList = stmtBldr.chunk(tokens);

    assertThat(statementList.get(0).getValue(), is("do"));
    assertThat(statementList.get(1).getValue(), is("System.out.print(args[i])"));
    assertThat(statementList.get(2).getValue(), is("i=i+INTEGER"));
    assertThat(statementList.get(3).getValue(), is("while(i<args.length);"));
    assertThat(statementList.get(4).getValue(), is("do"));
    assertThat(statementList.get(5).getValue(), is("System.out.print(i++)"));
    assertThat(statementList.get(6).getValue(), is("while(i<INTEGER);"));
  }

  @Test
  public void shouldLexSwitchStatement() {
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/StatementTest/java/Switch.java");

    TokenQueue tokens = lexer.chunk(testFile);
    List<Statement> statementList = stmtBldr.chunk(tokens);

    assertThat(statementList.get(0).getValue(), is("switch(month)"));
    assertThat(statementList.get(1).getValue(), is("caseINTEGER:"));
    assertThat(statementList.get(2).getValue(), is("monthString=LITERAL"));
    assertThat(statementList.get(3).getValue(), is("break"));
    assertThat(statementList.get(4).getValue(), is("caseINTEGER:"));
    assertThat(statementList.get(5).getValue(), is("monthString=LITERAL"));
    assertThat(statementList.get(6).getValue(), is("break"));
    assertThat(statementList.get(7).getValue(), is("caseINTEGER:"));
    assertThat(statementList.get(8).getValue(), is("monthString=LITERAL"));
    assertThat(statementList.get(9).getValue(), is("break"));
    assertThat(statementList.get(10).getValue(), is("default:"));
    assertThat(statementList.get(11).getValue(), is("monthString=LITERAL"));
  }

  @Test
  public void testArrayStatement() {
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/StatementTest/java/Array.java");

    TokenQueue tokens = lexer.chunk(testFile);
    List<Statement> statementList = stmtBldr.chunk(tokens);

    assertThat(statementList.size(), greaterThan(0));
  }

  @Test
  public void tokenQueueInsertOrderBug() {
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/special/MessageResources.java");

    TokenQueue tokens = lexer.chunk(testFile);
    List<Statement> statementList = stmtBldr.chunk(tokens);

    assertThat(statementList.size(), greaterThan(0));
  }

  @Test
  public void emptyTokenListTest2() {
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/special/RequestUtils.java");

    TokenQueue tokens = lexer.chunk(testFile);
    List<Statement> statementList = stmtBldr.chunk(tokens);

    assertThat(statementList.size(), greaterThan(0));
  }
}
