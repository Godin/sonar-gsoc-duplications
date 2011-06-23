package org.sonar.duplications.java;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Test;
import org.sonar.duplications.DuplicationsTestUtil;
import org.sonar.duplications.api.Block;
import org.sonar.duplications.api.BlockBuilder;
import org.sonar.duplications.api.DuplicationsException;
import org.sonar.duplications.index.FileBlockGroup;
import org.sonar.duplications.statement.StatementBuilder;
import org.sonar.duplications.token.Lexer;

public class FileBlockGroupTest {

  File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest/CPDFile1.java");
  Lexer lexer = JavaLexer.build();
  StatementBuilder statementBuilder = JavaStatementBuilder.build();
  int blockSize = 3;
  BlockBuilder blockBuilder = new BlockBuilder(testFile, blockSize);

  @Test
  public void shouldTokenizeSource() {
    FileBlockGroup fci = new FileBlockGroup(testFile);
    init(fci, testFile);
    assertThat(fci.getBlockList().size(), is(8));
  }

  @Test(expected = DuplicationsException.class)
  public void testWrongResourceId() {
    FileBlockGroup file = new FileBlockGroup("a");
    file.addBlock(new Block("b", "13dws2324d", 1, 1, 7));
  }

  public void init(FileBlockGroup fci, File file) {
    try {
      Lexer lexer = JavaLexer.build();
      StatementBuilder statementBuilder = JavaStatementBuilder.build();
      BlockBuilder blockBuilder = new BlockBuilder(file);
      for (Block block : blockBuilder.build(statementBuilder.build(lexer.lex(file)))) {
        fci.addBlock(block);
      }
    } catch (Exception e) {
      throw new DuplicationsException("Error in initialization", e);
    }
  }
}
