package org.sonar.duplications.java;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.sonar.duplications.api.codeunit.Statement;
import org.sonar.duplications.api.codeunit.Token;
import org.sonar.duplications.api.lexer.Lexer;
import org.sonar.duplications.api.lexer.StatementBuilder;
import org.sonar.duplications.api.lexer.family.JavaLexer;
import org.sonar.duplications.api.lexer.family.JavaStatementBuilder;

public class JavaStatementBuilderTest {

	Lexer lexer = JavaLexer.build();
	StatementBuilder stmtBldr = JavaStatementBuilder.build();

	@Test
	public void shouldIgnoreImportStatement() {
		File testFile = new File("test-resources/org/sonar/duplications/cpd/CPDTest/StatementTest/Import.java");
		List<Token> tokens = lexer.lex(testFile);
		assertThat(stmtBldr.build(tokens).size(), is(0));
	}
	 
	@Test
	public void shouldIgnorePackageStatement() {
		File testFile = new File("test-resources/org/sonar/duplications/cpd/CPDTest/StatementTest/Package.java");
		List<Token> tokens = lexer.lex(testFile);
		assertThat(stmtBldr.build(tokens).size(), is(0));
	}
	  
	@Test
	public void shouldLexAnnotationStatement() {
	    File testFile = new File("test-resources/org/sonar/duplications/cpd/CPDTest/StatementTest/Annotation.java");
	    List<Token> tokens = lexer.lex(testFile);
	    List<Statement> statementList = stmtBldr.build(tokens);
	    
	    assertThat(statementList.get(0).getNormalizedContent(), is("@Entity"));
	    assertThat(statementList.get(1).getNormalizedContent(), is("@Table(name=LITERAL)"));
	    assertThat(statementList.get(2).getNormalizedContent(), is("publicclassPropertyextendsBaseIdentifiable"));
	    assertThat(statementList.get(3).getNormalizedContent(), is("@Column(name=LITERAL,updatable=true,nullable=true)"));
	    assertThat(statementList.get(4).getNormalizedContent(), is("privateStringkey;"));
	    assertThat(statementList.get(5).getNormalizedContent(), is("@Column(name=LITERAL,updatable=true,nullable=true,length=INTEGER)"));
	    assertThat(statementList.get(6).getNormalizedContent(), is("@Lob"));
	    assertThat(statementList.get(7).getNormalizedContent(), is("privatechar[]value;"));
	    assertThat(statementList.get(8).getNormalizedContent(), is("@Override"));
	    assertThat(statementList.get(9).getNormalizedContent(), is("publicIntegergetUserId()"));
	    assertThat(statementList.get(10).getNormalizedContent(), is("returnuserId;"));
	}
  
	@Test
	public void shouldLexIfStatement() {
	    File testFile = new File("test-resources/org/sonar/duplications/cpd/CPDTest/StatementTest/If.java");
	    List<Token> tokens = lexer.lex(testFile);
	    List<Statement> statementList = stmtBldr.build(tokens);
	
	    assertThat(statementList.get(0).getNormalizedContent(), is("if(getParent()==null)"));
	    assertThat(statementList.get(1).getNormalizedContent(), is("returnlocalName;"));
	    assertThat(statementList.get(2).getNormalizedContent(), is("if(id.length()>INTEGER)"));
	    assertThat(statementList.get(3).getNormalizedContent(), is("id+=File.separator;"));
	    assertThat(statementList.get(4).getNormalizedContent(), is("elseif(id.length()==-INTEGER)"));
	    assertThat(statementList.get(5).getNormalizedContent(), is("id+=LITERAL;"));
	    assertThat(statementList.get(6).getNormalizedContent(), is("if(id.length()==INTEGER)"));
	    assertThat(statementList.get(7).getNormalizedContent(), is("returnlocalname;"));
	    assertThat(statementList.get(8).getNormalizedContent(), is("if(id.length()==INTEGER)"));
	    assertThat(statementList.get(9).getNormalizedContent(), is("returnlocalname;"));
	}
  
	@Test
	public void shouldLexForStatement() {
		File testFile = new File(
				"test-resources/org/sonar/duplications/cpd/CPDTest/StatementTest/For.java");
	    List<Token> tokens = lexer.lex(testFile);
	    List<Statement> statementList = stmtBldr.build(tokens);
		
		assertThat(statementList.get(0).getNormalizedContent(), is("for(inti=INTEGER;i<children.length;i++)"));
		assertThat(statementList.get(1).getNormalizedContent(), is("total+=i;"));
		assertThat(statementList.get(2).getNormalizedContent(), is("for(inti=INTEGER;i<children.length;i++)"));
		assertThat(statementList.get(3).getNormalizedContent(), is("total+=i;"));
		assertThat(statementList.get(4).getNormalizedContent(), is("for(inti=INTEGER;i<children.length;i++)"));
		assertThat(statementList.get(5).getNormalizedContent(), is("total+=i;"));
		assertThat(statementList.get(6).getNormalizedContent(),is("for(Tokentoken:tokenList)"));
		assertThat(statementList.get(7).getNormalizedContent(),is("System.out.println(token.getNormalizedContent());"));
		
		// for statement that spans multiple lines
		assertThat(statementList.get(2).getStartLine(), is(5));
		assertThat(statementList.get(2).getEndLine(), is(7));
	}
	
	@Test
	  public void shouldLexWhileStatement() {
	    File testFile = new File(
	        "test-resources/org/sonar/duplications/cpd/CPDTest/StatementTest/While.java");

	    List<Token> tokens = lexer.lex(testFile);
	    List<Statement> statementList = stmtBldr.build(tokens);
	    
	    assertThat(statementList.get(0).getNormalizedContent(), is("while(i<args.length)"));
	    assertThat(statementList.get(1).getNormalizedContent(), is("System.out.print(args[i]);"));
	    assertThat(statementList.get(2).getNormalizedContent(), is("i=i+INTEGER;"));
	    assertThat(statementList.get(3).getNormalizedContent(), is("while(i<args.length)"));
	    assertThat(statementList.get(4).getNormalizedContent(), is("System.out.print(args[i++]);"));
	  }

	  @Test
	  public void shouldLexDoWhileStatement() {
	    File testFile = new File(
	        "test-resources/org/sonar/duplications/cpd/CPDTest/StatementTest/DoWhile.java");

	    List<Token> tokens = lexer.lex(testFile);
	    List<Statement> statementList = stmtBldr.build(tokens);

	    assertThat(statementList.get(0).getNormalizedContent(), is("do"));
	    assertThat(statementList.get(1).getNormalizedContent(), is("System.out.print(args[i]);"));
	    assertThat(statementList.get(2).getNormalizedContent(), is("i=i+INTEGER;"));
	    assertThat(statementList.get(3).getNormalizedContent(), is("while(i<args.length);"));
	    assertThat(statementList.get(4).getNormalizedContent(), is("do"));
	    assertThat(statementList.get(5).getNormalizedContent(), is("System.out.print(i++);"));
	    assertThat(statementList.get(6).getNormalizedContent(), is("while(i<INTEGER);"));
	  }

	  @Test
	  public void shouldLexSwitchStatement() {
	    File testFile = new File(
	        "test-resources/org/sonar/duplications/cpd/CPDTest/StatementTest/Switch.java");

	    List<Token> tokens = lexer.lex(testFile);
	    List<Statement> statementList = stmtBldr.build(tokens);

	    assertThat(statementList.get(0).getNormalizedContent(), is("switch(month)"));
	    assertThat(statementList.get(1).getNormalizedContent(), is("caseINTEGER:"));
	    assertThat(statementList.get(2).getNormalizedContent(), is("monthString=LITERAL;"));
	    assertThat(statementList.get(3).getNormalizedContent(), is("break;"));
	    assertThat(statementList.get(4).getNormalizedContent(), is("caseINTEGER:"));
	    assertThat(statementList.get(5).getNormalizedContent(), is("monthString=LITERAL;"));
	    assertThat(statementList.get(6).getNormalizedContent(), is("break;"));
	    assertThat(statementList.get(7).getNormalizedContent(), is("caseINTEGER:"));
	    assertThat(statementList.get(8).getNormalizedContent(), is("monthString=LITERAL;"));
	    assertThat(statementList.get(9).getNormalizedContent(), is("break;"));
	    assertThat(statementList.get(10).getNormalizedContent(), is("default:"));
	    assertThat(statementList.get(11).getNormalizedContent(), is("monthString=LITERAL;"));
	  }
}