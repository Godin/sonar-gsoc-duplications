package org.sonar.duplications.java;

import org.junit.Test;
import org.sonar.duplications.api.codeunit.Token;
import org.sonar.duplications.api.lexer.family.StatementExtractor;

import java.io.File;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class StatementLexerTest {

  static String newline = System.getProperty("line.separator");
  StatementExtractor statementExtractor = StatementExtractor.getInstance();

  @Test
  public void shouldIgnoreImportStatement() {
    File testFile = new File("test-resources/org/sonar/duplications/cpd/CPDTest/StatementTest/Import.java");
    assertThat(statementExtractor.extractStatement(testFile).size(), is(0));
  }

  @Test
  public void shouldIgnorePackageStatement() {
    File testFile = new File("test-resources/org/sonar/duplications/cpd/CPDTest/StatementTest/Package.java");
    assertThat(statementExtractor.extractStatement(testFile).size(), is(0));
  }

  @Test
  public void shouldLexAnnotationStatement() {
    File testFile = new File("test-resources/org/sonar/duplications/cpd/CPDTest/StatementTest/Annotation.java");
    assertThat(statementExtractor.extractStatement(testFile).get(0).getOriginalContent(), is("@Override"+newline));
  }

  @Test
  public void shouldLexIfStatement() {
    File testFile = new File("test-resources/org/sonar/duplications/cpd/CPDTest/StatementTest/If.java");

    List<Token> tokenList = statementExtractor.extractStatement(testFile);

    assertThat(tokenList.get(0).getOriginalContent(), is("if (getParent() == null)"));
    assertThat(tokenList.get(1).getOriginalContent(), is("return localName;"));
    assertThat(tokenList.get(2).getOriginalContent(), is("if (id.length() > 0) {"));
    assertThat(tokenList.get(3).getOriginalContent(), is("id += File.separator;"));
    assertThat(tokenList.get(4).getOriginalContent(), is("}"));
    assertThat(tokenList.get(5).getOriginalContent(), is("else if(id.length() == -1){"));
    assertThat(tokenList.get(6).getOriginalContent(), is("id+= \"../\";"));
    assertThat(tokenList.get(7).getOriginalContent(), is("}"));
    assertThat(tokenList.get(8).getOriginalContent(), is("if (id.length() == 0)"+newline+"{"));
    assertThat(tokenList.get(9).getOriginalContent(), is("return localname;"));
    assertThat(tokenList.get(10).getOriginalContent(), is("}"));
    assertThat(tokenList.get(11).getOriginalContent(), is("if (id.length() == 0) {"));
    assertThat(tokenList.get(12).getOriginalContent(), is("return localname;"));
    assertThat(tokenList.get(13).getOriginalContent(), is("}"));
  }

  @Test
  public void shouldLexForStatement() {
    File testFile = new File("test-resources/org/sonar/duplications/cpd/CPDTest/StatementTest/For.java");

    List<Token> tokenList = statementExtractor.extractStatement(testFile);

    assertThat(tokenList.get(0).getOriginalContent(), is("for (int i=0; i<children.length; i++) {"));
    assertThat(tokenList.get(1).getOriginalContent(), is("total += i;"));
    assertThat(tokenList.get(2).getOriginalContent(), is("}"));
    assertThat(tokenList.get(3).getOriginalContent(), is("for (int i=0; "+newline+"i<children.length; "+newline+"i++) {"));
    assertThat(tokenList.get(4).getOriginalContent(), is("total += i;"));
    assertThat(tokenList.get(5).getOriginalContent(), is("}"));
    assertThat(tokenList.get(6).getOriginalContent(), is("for (int i=0; i<children.length; i++)"));
    assertThat(tokenList.get(7).getOriginalContent(), is("total += i;"));
    assertThat(tokenList.get(8).getOriginalContent(), is("for(Token token : tokenList)"));
    assertThat(tokenList.get(9).getOriginalContent(), is("System.out.println(token.getNormalizedContent());"));
  }

  @Test
  public void shouldLexWhileStatement() {
    File testFile = new File(
        "test-resources/org/sonar/duplications/cpd/CPDTest/StatementTest/While.java");

    List<Token> tokenList = statementExtractor.extractStatement(testFile);
    assertThat(tokenList.get(0).getOriginalContent(), is("while (i < args.length) {"));
    assertThat(tokenList.get(1).getOriginalContent(), is("System.out.print(args[i]);"));
    assertThat(tokenList.get(2).getOriginalContent(), is("i = i + 1;"));
    assertThat(tokenList.get(3).getOriginalContent(), is("}"));
    assertThat(tokenList.get(4).getOriginalContent(), is("while (i < args.length)"));
    assertThat(tokenList.get(5).getOriginalContent(), is("System.out.print(args[i++]);"));
  }

  @Test
  public void shouldLexDoWhileStatement() {
    File testFile = new File(
        "test-resources/org/sonar/duplications/cpd/CPDTest/StatementTest/DoWhile.java");

    List<Token> tokenList = statementExtractor.extractStatement(testFile);
    assertThat(tokenList.get(0).getOriginalContent(), is("do {"));
    assertThat(tokenList.get(1).getOriginalContent(), is("System.out.print(args[i]);"));
    assertThat(tokenList.get(2).getOriginalContent(), is("i = i + 1;"));
    assertThat(tokenList.get(3).getOriginalContent(), is("}"));
    assertThat(tokenList.get(4).getOriginalContent(), is("while (i < args.length);"));
    assertThat(tokenList.get(5).getOriginalContent(), is("do"));
    assertThat(tokenList.get(6).getOriginalContent(), is("System.out.print(i++);"));
    assertThat(tokenList.get(7).getOriginalContent(), is("while (i <10);"));
  }

  @Test
  public void shouldLexSwitchStatement() {
    File testFile = new File(
        "test-resources/org/sonar/duplications/cpd/CPDTest/StatementTest/Switch.java");

    List<Token> tokenList = statementExtractor.extractStatement(testFile);

    assertThat(tokenList.get(0).getOriginalContent(), is("switch (month) {"));
    assertThat(tokenList.get(1).getOriginalContent(), is("case 1: monthString = \"January\";"));
    assertThat(tokenList.get(2).getOriginalContent(), is("break;"));
    assertThat(tokenList.get(3).getOriginalContent(), is("case 2: "+newline+"\tmonthString = \"February\";"));
    assertThat(tokenList.get(4).getOriginalContent(), is("break;"));
    assertThat(tokenList.get(5).getOriginalContent(), is("case 3: "+newline+"\tmonthString = \"March\";"));
    assertThat(tokenList.get(6).getOriginalContent(), is("break;"));
    assertThat(tokenList.get(7).getOriginalContent(), is("default: monthString = \"Invalid month\";"));
    assertThat(tokenList.get(8).getOriginalContent(), is("}"));
  }

}
