package org.sonar.duplications.csharp;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
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

public class CSharpStatementBuilderTest {

  TokenChunker lexer = CSharpTokenProducer.build();
  StatementChunker stmtBldr = CSharpStatementBuilder.build();

  @Test (expected = DuplicationsException.class)
  public void shouldThroughException() {
	  stmtBldr.chunk(null);
  }
  
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
    Statement expected = new Statement(5,5,"using(StreamReaderreader=newStreamReader(LITERAL))", 0);
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

    Statement expected1 = new Statement(3, 3, "[AttributeUsage(AttributeTargets.Class)]", 0);
    Statement expected2 = new Statement(6, 6, "inttemp=arr[i]", 2);
    Statement unexpected1 = new Statement(6, 6, "[i]", 2);
    Statement expected3 = new Statement(9, 9, "[Serializable]", 3);
    Statement expected4 = new Statement(12, 12, "[NonSerialized]", 6);
    
    assertThat(statementList, hasItems(expected1, expected2, expected3, expected4));
    assertThat(statementList, not(hasItems(unexpected1)));
  }

  @Test
  public void shouldLexIfStatement() {
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/StatementTest/csharp/if.cs");
    TokenQueue tokens = lexer.chunk(testFile);
    List<Statement> statementList = stmtBldr.chunk(tokens);

    Statement expected1 = new Statement(1, 1, "if(getParent()==null)", 0);
    Statement expected2 = new Statement(3, 3, "else", 2);
    Statement expected3 = new Statement(6, 6, "if(id.length()>INTEGER)", 4);
    Statement expected4 = new Statement(9, 9, "elseif(id.length()==-INTEGER)", 6);
    
    assertThat(statementList, hasItems(expected1, expected2, expected3, expected4));
  }

  @Test
  public void shouldLexForStatement() {
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/StatementTest/csharp/for.cs");
    TokenQueue tokens = lexer.chunk(testFile);
    List<Statement> statementList = stmtBldr.chunk(tokens);

    Statement expected1 = new Statement(1, 1, "for(inti=INTEGER;i<children.length;i++)", 0);
    Statement expected2 = new Statement(5, 5, "foreach(stringiteminitemsToWrite)", 2);
    
    assertThat(statementList, hasItems(expected1, expected2));
  }

  @Test
  public void shouldLexWhileStatement() {
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/StatementTest/csharp/while.cs");

    TokenQueue tokens = lexer.chunk(testFile);
    List<Statement> statementList = stmtBldr.chunk(tokens);
    
    Statement expected1 = new Statement(1, 1, "while(i<args.length)", 0);
    Statement expected2 = new Statement(5, 5, "while(i<args.length)", 3);
    
    assertThat(statementList, hasItems(expected1, expected2));				
  }

  @Test
  public void shouldLexDoWhileStatement() {
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/StatementTest/csharp/doWhile.cs");

    TokenQueue tokens = lexer.chunk(testFile);
    List<Statement> statementList = stmtBldr.chunk(tokens);

    Statement expected1 = new Statement(1, 1, "do", 0);
    Statement expected2 = new Statement(4, 4, "while(i<args.length);", 3);
    Statement expected3 = new Statement(6, 6, "do", 4);
    Statement expected4 = new Statement(8, 8, "while(i<INTEGER);", 6);
    
    assertThat(statementList, hasItems(expected1, expected2, expected3, expected4));
  }

  @Test
  public void shouldLexSwitchStatement() {
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/StatementTest/csharp/switch.cs");

    TokenQueue tokens = lexer.chunk(testFile);
    List<Statement> statementList = stmtBldr.chunk(tokens);
 
    Statement expected1 = new Statement(1, 1, "switch(month)", 0);
    Statement expected2 = new Statement(2, 2, "caseINTEGER:", 1);
    Statement expected3 = new Statement(3, 3, "caseINTEGER:", 4);
    Statement expected4 = new Statement(4, 4, "caseINTEGER:", 5);
    Statement expected5 = new Statement(6, 6, "default:", 8);
    
    assertThat(statementList, hasItems(expected1, expected2, expected3, expected4, expected5));
  }

  @Test
  public void shouldLexEnumStatement() {
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/StatementTest/csharp/enum.cs");

    TokenQueue tokens = lexer.chunk(testFile);
    List<Statement> statementList = stmtBldr.chunk(tokens);

    Statement expected1 = new Statement(1, 1, "enumWeekday{Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday};", 0);
    Statement expected2 = new Statement(3, 3, "enumAge{Infant=INTEGER,Teenager=INTEGER,Adult=INTEGER};", 1);
    Statement expected3 = new Statement(5, 5, "enumCardSuit:byte{Hearts,Diamonds,Spades,Clubs};", 2);
    assertThat(statementList, hasItems(expected1, expected2, expected3));
  }
  
  @Test
  public void shouldLexGetterSetterStatement() {
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/StatementTest/csharp/getterSetter.cs");

    TokenQueue tokens = lexer.chunk(testFile);
    List<Statement> statementList = stmtBldr.chunk(tokens);
    
    Statement expected1 = new Statement(2, 2, "set{_name=value;}", 1);
    Statement expected2 = new Statement(4, 4, "get{return_name;}", 2);
    Statement expected3 = new Statement(9, 9, "set{_age=value;}", 4);
    Statement expected4 = new Statement(10, 10, "get{return_age;}", 5);
    Statement expected5 = new Statement(13, 13, "get;", 7);
    Statement expected6 = new Statement(13, 13, "set;", 8);
    Statement expected7 = new Statement(14, 14, "get;", 10);
    Statement expected8 = new Statement(14, 14, "set;", 11);
    assertThat(statementList, hasItems(expected1, expected2, expected3, expected4, expected5, expected6, expected7, expected8));
  }
}
