package org.sonar.duplications.java;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.sonar.duplications.api.codeunit.Statement;
import org.sonar.duplications.api.codeunit.StatementProvider;
import org.sonar.duplications.api.lexer.Lexer;
import org.sonar.duplications.api.lexer.family.JavaLexer;
import org.sonar.duplications.api.lexer.family.StatementExtractor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

public class StatementProviderTest {

  StatementProvider statementProvider;
  File testFile = new File("test-resources/org/sonar/duplications/cpd/CPDTest/CPDFile1.java");

  @Ignore//this test will be updated after statement builder framework integration is done
  public void initTest() {

    Lexer lexer = JavaLexer.build();
    StatementExtractor statementExtractor = StatementExtractor.getInstance();

    statementProvider = new StatementProvider(lexer, statementExtractor);
    statementProvider.init(testFile);
  }

  @Ignore //this test will be updated after statement builder framework integration is done
  public void shouldTokenizeSource() {
    List<Statement> statementList = new ArrayList<Statement>();
    Statement statement;
    while ((statement = statementProvider.getNext()) != null) {
      statementList.add(statement);
    }

    Statement st1 = new Statement(4, 4, "publicstaticvoidmain(String[]args){", "publicstaticvoidmain(String[]args){", 1);
    Statement st2 = new Statement(8, 8, "case1:dirlist(args[0]);", "caseINTEGER:dirlist(args[INTEGER]);", 5);
    Statement st3 = new Statement(10, 10, "default:System.out.println(\"Multiple files are not allow.\");", "default:System.out.println(LITERAL);", 7);
    Statement st4 = new Statement(11, 11, "System.exit(0);", "System.exit(INTEGER);", 8);

    assertThat(statementList, hasItems(st1, st2, st3, st4));

  }

}
