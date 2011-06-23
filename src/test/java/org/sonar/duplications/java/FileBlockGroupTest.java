package org.sonar.duplications.java;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Test;
import org.sonar.duplications.DuplicationsTestUtil;
import org.sonar.duplications.api.Block;
import org.sonar.duplications.api.BlockBuilder;
import org.sonar.duplications.api.DuplicationsException;
import org.sonar.duplications.api.Lexer;
import org.sonar.duplications.api.StatementBuilder;
import org.sonar.duplications.api.index.FileBlockGroup;

public class FileBlockGroupTest {

  File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest/CPDFile1.java");
  Lexer lexer = JavaLexer.build();
  StatementBuilder statementBuilder = JavaStatementBuilder.build();
  int blockSize = 3;
  BlockBuilder blockBuilder = new BlockBuilder(testFile, blockSize);

  @Test
  public void shouldTokenizeSource() {
    FileBlockGroup fci = new FileBlockGroup(testFile);
    fci.init();
    assertThat(fci.getBlockList().size(), is(8));
  }

  @Test(expected = DuplicationsException.class)
  public void testWrongResourceId() {
    FileBlockGroup file = new FileBlockGroup("a");
    file.addBlock(new Block("b", "13dws2324d", 1, 1, 7));
  }
}
