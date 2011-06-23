package org.sonar.duplications.java;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Test;
import org.sonar.duplications.DuplicationsTestUtil;
import org.sonar.duplications.api.Block;
import org.sonar.duplications.api.DuplicationsException;
import org.sonar.duplications.block.BlockChunker;
import org.sonar.duplications.index.FileBlockGroup;
import org.sonar.duplications.statement.StatementChunker;
import org.sonar.duplications.token.TokenChunker;

public class FileBlockGroupTest {

  File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest/CPDFile1.java");
  TokenChunker lexer = JavaTokenProducer.build();
  StatementChunker statementBuilder = JavaStatementBuilder.build();
  int blockSize = 3;
  BlockChunker blockBuilder = new BlockChunker(testFile, blockSize);

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
      TokenChunker lexer = JavaTokenProducer.build();
      StatementChunker statementBuilder = JavaStatementBuilder.build();
      BlockChunker blockBuilder = new BlockChunker(file);
      for (Block block : blockBuilder.chunk(statementBuilder.chunk(lexer.chunk(file)))) {
        fci.addBlock(block);
      }
    } catch (Exception e) {
      throw new DuplicationsException("Error in initialization", e);
    }
  }
}
