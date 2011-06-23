package org.sonar.duplications.block;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Test;
import org.sonar.duplications.DuplicationsException;
import org.sonar.duplications.DuplicationsTestUtil;
import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.BlockChunker;
import org.sonar.duplications.block.FileCloneIndexGroup;
import org.sonar.duplications.java.JavaTokenProducer;
import org.sonar.duplications.statement.JavaStatementBuilder;
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
    FileCloneIndexGroup fci = new FileCloneIndexGroup(testFile);
    init(fci, testFile);
    assertThat(fci.getBlockList().size(), is(8));
  }

  @Test(expected = DuplicationsException.class)
  public void testWrongResourceId() {
    FileCloneIndexGroup file = new FileCloneIndexGroup("a");
    file.addBlock(new Block("b", "13dws2324d", 1, 1, 7));
  }

  public void init(FileCloneIndexGroup fci, File file) {
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
