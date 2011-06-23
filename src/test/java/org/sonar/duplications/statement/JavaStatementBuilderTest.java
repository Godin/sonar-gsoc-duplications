package org.sonar.duplications.statement;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.sonar.duplications.DuplicationsTestUtil;
import org.sonar.duplications.java.JavaTokenProducer;
import org.sonar.duplications.token.TokenChunker;
import org.sonar.duplications.token.TokenQueue;

import static org.junit.Assert.assertThat;

import static org.hamcrest.Matchers.is;

public class JavaStatementBuilderTest {

  TokenChunker lexer = JavaTokenProducer.build();
  StatementChunker stmtBldr = JavaStatementBuilder.build();

  @Test
  public void shouldIgnoreImportStatement() {
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest/StatementTest/Import.java");
    TokenQueue tokens = lexer.chunk(testFile);
    assertThat(stmtBldr.chunk(tokens).size(), is(0));
  }

  @Test
  public void shouldIgnorePackageStatement() {
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest/StatementTest/Package.java");
    TokenQueue tokens = lexer.chunk(testFile);
    assertThat(stmtBldr.chunk(tokens).size(), is(0));
  }

  @Test
  public void shouldLexAnnotationStatement() {
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest/StatementTest/Annotation.java");
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
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest/StatementTest/If.java");
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
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest/StatementTest/For.java");
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
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest/StatementTest/While.java");

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
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest/StatementTest/DoWhile.java");

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
    File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest/StatementTest/Switch.java");

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
}
