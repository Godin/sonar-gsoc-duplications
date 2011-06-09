package org.sonar.duplications.java;

import org.junit.Before;
import org.junit.Test;
import org.sonar.duplications.api.CloneIndexException;
import org.sonar.duplications.api.codeunit.Block;
import org.sonar.duplications.api.codeunit.BlockProvider;
import org.sonar.duplications.api.codeunit.StatementProvider;
import org.sonar.duplications.api.index.FileBlockGroup;
import org.sonar.duplications.api.lexer.Lexer;
import org.sonar.duplications.api.lexer.family.JavaLexer;
import org.sonar.duplications.api.lexer.family.StatementExtractor;

import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FileBlockGroupTest {

  BlockProvider blockProvider;
  File testFile = new File("test-resources/org/sonar/duplications/cpd/CPDTest/CPDFile1.java");

  @Before
  public void initTest() {
    Lexer tokenizer = JavaLexer.build();
    StatementExtractor statementExtractor = StatementExtractor.getInstance();
    StatementProvider statementProvider = new StatementProvider(tokenizer, statementExtractor);
    blockProvider = new BlockProvider(statementProvider, BlockProvider.DEFAULT_BLOCK_SIZE);
  }

  //@Test
  public void shouldTokenizeSource() {
    FileBlockGroup fci = new FileBlockGroup(testFile, blockProvider);
    assertThat(fci.getBlockList().size(), is(8));
  }

  @Test(expected = CloneIndexException.class)
  public void testWrongResourceId() {
    FileBlockGroup file = new FileBlockGroup("a");
    file.addBlock(new Block("b", new byte[]{3}, 1, 1, 7));
  }
}
