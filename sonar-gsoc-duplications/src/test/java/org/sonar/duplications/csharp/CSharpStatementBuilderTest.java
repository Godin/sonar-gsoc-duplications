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
package org.sonar.duplications.csharp;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.sonar.duplications.DuplicationsTestUtil;
import org.sonar.duplications.statement.Statement;
import org.sonar.duplications.statement.StatementChunker;
import org.sonar.duplications.token.TokenChunker;
import org.sonar.duplications.token.TokenQueue;

public class CSharpStatementBuilderTest {

  TokenChunker lexer = CSharpTokenProducer.build();
  StatementChunker stmtBldr = CSharpStatementBuilder.build();
  
  @Test
  public void shouldIgnoreUsingStatement() {
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/StatementTest/csharp/using.cs");
    TokenQueue tokens = lexer.chunk(testFile);
    assertThat(stmtBldr.chunk(tokens).size(), is(0));
  }

  @Test
  public void shouldIgnorePreprocessorStatement() {
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/StatementTest/csharp/preprocessor.cs");
    TokenQueue tokens = lexer.chunk(testFile);
    List<Statement> statementList = stmtBldr.chunk(tokens);
    assertThat(statementList.size(), is(0));
  }
  
  @Test
  public void shouldNotIgnoreUsingStatementInsideClass() {
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/StatementTest/csharp/using2.cs");
    TokenQueue tokens = lexer.chunk(testFile);
    Statement expected = new Statement(5, 5, "using(StreamReaderreader=newStreamReader(LITERAL))");
    List<Statement> statementList = stmtBldr.chunk(tokens);
    assertThat(statementList.size(), is(1));
    assertThat(statementList, hasItem(expected));
  }
  
  @Test
  public void shouldIgnoreNamespaceStatement() {
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/StatementTest/csharp/namespace.cs");
    TokenQueue tokens = lexer.chunk(testFile);
    assertThat(stmtBldr.chunk(tokens).size(), is(0));
  }

  @Test
  public void shouldLexMetadataStatement() {
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/StatementTest/csharp/metadata.cs");
    TokenQueue tokens = lexer.chunk(testFile);
    List<Statement> statementList = stmtBldr.chunk(tokens);

    Statement expected1 = new Statement(3, 3, "[AttributeUsage(AttributeTargets.Class)]");
    Statement expected2 = new Statement(6, 6, "inttemp=arr[i]");
    Statement unexpected1 = new Statement(6, 6, "[i]");
    Statement expected3 = new Statement(9, 9, "[Serializable]");
    Statement expected4 = new Statement(12, 12, "[NonSerialized]");
    
    assertThat(statementList, hasItems(expected1, expected2, expected3, expected4));
    assertThat(statementList, not(hasItems(unexpected1)));
  }

  @Test
  public void shouldLexIfStatement() {
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/StatementTest/csharp/if.cs");
    TokenQueue tokens = lexer.chunk(testFile);
    List<Statement> statementList = stmtBldr.chunk(tokens);

    Statement expected1 = new Statement(1, 1, "if(getParent()==null)");
    Statement expected2 = new Statement(3, 3, "else");
    Statement expected3 = new Statement(6, 6, "if(id.length()>INTEGER)");
    Statement expected4 = new Statement(9, 9, "elseif(id.length()==-INTEGER)");
    
    assertThat(statementList, hasItems(expected1, expected2, expected3, expected4));
  }

  @Test
  public void shouldLexForStatement() {
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/StatementTest/csharp/for.cs");
    TokenQueue tokens = lexer.chunk(testFile);
    List<Statement> statementList = stmtBldr.chunk(tokens);

    Statement expected1 = new Statement(1, 1, "for(inti=INTEGER;i<children.length;i++)");
    Statement expected2 = new Statement(5, 5, "foreach(stringiteminitemsToWrite)");
    
    assertThat(statementList, hasItems(expected1, expected2));
  }

  @Test
  public void shouldLexWhileStatement() {
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/StatementTest/csharp/while.cs");

    TokenQueue tokens = lexer.chunk(testFile);
    List<Statement> statementList = stmtBldr.chunk(tokens);
    
    Statement expected1 = new Statement(1, 1, "while(i<args.length)");
    Statement expected2 = new Statement(5, 5, "while(i<args.length)");
    
    assertThat(statementList, hasItems(expected1, expected2));				
  }

  @Test
  public void shouldLexDoWhileStatement() {
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/StatementTest/csharp/doWhile.cs");

    TokenQueue tokens = lexer.chunk(testFile);
    List<Statement> statementList = stmtBldr.chunk(tokens);

    Statement expected1 = new Statement(1, 1, "do");
    Statement expected2 = new Statement(4, 4, "while(i<args.length);");
    Statement expected3 = new Statement(6, 6, "do");
    Statement expected4 = new Statement(8, 8, "while(i<INTEGER);");
    
    assertThat(statementList, hasItems(expected1, expected2, expected3, expected4));
  }

  @Test
  public void shouldLexSwitchStatement() {
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/StatementTest/csharp/switch.cs");

    TokenQueue tokens = lexer.chunk(testFile);
    List<Statement> statementList = stmtBldr.chunk(tokens);
 
    Statement expected1 = new Statement(1, 1, "switch(month)");
    Statement expected2 = new Statement(2, 2, "caseINTEGER:");
    Statement expected3 = new Statement(3, 3, "caseINTEGER:");
    Statement expected4 = new Statement(4, 4, "caseINTEGER:");
    Statement expected5 = new Statement(6, 6, "default:");
    
    assertThat(statementList, hasItems(expected1, expected2, expected3, expected4, expected5));
  }

  @Test
  public void shouldLexEnumStatement() {
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/StatementTest/csharp/enum.cs");

    TokenQueue tokens = lexer.chunk(testFile);
    List<Statement> statementList = stmtBldr.chunk(tokens);

    Statement expected1 = new Statement(1, 1, "enumWeekday{Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday};");
    Statement expected2 = new Statement(3, 3, "enumAge{Infant=INTEGER,Teenager=INTEGER,Adult=INTEGER};");
    Statement expected3 = new Statement(5, 5, "enumCardSuit:byte{Hearts,Diamonds,Spades,Clubs};");
    assertThat(statementList, hasItems(expected1, expected2, expected3));
  }
  
  @Test
  public void shouldLexGetterSetterStatement() {
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/StatementTest/csharp/getterSetter.cs");

    TokenQueue tokens = lexer.chunk(testFile);
    List<Statement> statementList = stmtBldr.chunk(tokens);
    
    Statement expected1 = new Statement(3, 3, "set{_name=value;}");
    Statement expected2 = new Statement(4, 4, "get{return_name;}");
    Statement expected3 = new Statement(9, 9, "set{_age=value;}");
    Statement expected4 = new Statement(10, 10, "get{return_age;}");
    Statement expected5 = new Statement(13, 13, "get;");
    Statement expected6 = new Statement(13, 13, "set;");
    Statement expected7 = new Statement(14, 14, "get;");
    Statement expected8 = new Statement(14, 14, "set;");
    assertThat(statementList, hasItems(expected1, expected2, expected3, expected4, expected5, expected6, expected7, expected8));
  }
}
