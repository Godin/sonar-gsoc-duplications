package org.sonar.duplications.block;

import java.io.File;

import org.junit.Test;
import org.sonar.duplications.DuplicationsException;
import org.sonar.duplications.DuplicationsTestUtil;
import org.sonar.duplications.java.JavaTokenProducer;
import org.sonar.duplications.statement.JavaStatementBuilder;
import org.sonar.duplications.statement.StatementChunker;
import org.sonar.duplications.token.TokenChunker;

import static org.junit.Assert.assertThat;

import static org.hamcrest.Matchers.is;

public class FileBlockGroupTest {

  private File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest/CPDFile1.java");

  @Test
  public void shouldTokenizeSource() {
    FileCloneIndex fci = new FileCloneIndex("MyFile");
    init(fci, testFile);
    assertThat(fci.getBlockList().size(), is(8));
  }

  @Test(expected = DuplicationsException.class)
  public void testWrongResourceId() {
    FileCloneIndex file = new FileCloneIndex("a");
    file.addBlock(new Block("b", "13dws2324d", 1, 1, 7));
  }

  public void init(FileCloneIndex fci, File file) {
    try {
      TokenChunker lexer = JavaTokenProducer.build();
      StatementChunker statementBuilder = JavaStatementBuilder.build();
      BlockChunker blockBuilder = new BlockChunker("MyFile", 5);
      for (Block block : blockBuilder.chunk(statementBuilder.chunk(lexer.chunk(file)))) {
        fci.addBlock(block);
      }
    } catch (Exception e) {
      throw new DuplicationsException("Error in initialization", e);
    }
  }
}
