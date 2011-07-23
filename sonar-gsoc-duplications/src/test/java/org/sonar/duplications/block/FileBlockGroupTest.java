package org.sonar.duplications.block;

import org.junit.Before;
import org.junit.Test;
import org.sonar.duplications.DuplicationsException;
import org.sonar.duplications.DuplicationsTestUtil;
import org.sonar.duplications.java.JavaStatementBuilder;
import org.sonar.duplications.java.JavaTokenProducer;
import org.sonar.duplications.statement.StatementChunker;
import org.sonar.duplications.token.TokenChunker;

import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FileBlockGroupTest {

  private File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest/CPDFile1.java");
  private FileCloneIndex fci;

  @Before
  public void pubicInitTest() {
    fci = new FileCloneIndex(testFile.getPath());
    init(fci, testFile);
  }

  @Test
  public void shouldGroupBlock() {
    assertThat(fci.getBlockList().size(), is(8));
  }

  @Test
  public void shouldAddBlockWithSameResourceId() {
    fci.addBlock(new Block(testFile.getPath(), "13dws2324d", 1, 1, 7));
  }

  @Test(expected = DuplicationsException.class)
  public void shouldNotAddBlockWithDifferentResourceId() {
    FileCloneIndex file = new FileCloneIndex("a");
    file.addBlock(new Block("b", "13dws2324d", 1, 1, 7));
  }

  public void init(FileCloneIndex fci, File file) {
    try {
      TokenChunker lexer = JavaTokenProducer.build();
      StatementChunker statementBuilder = JavaStatementBuilder.build();
      BlockChunker blockBuilder = new BlockChunker(5);
      for (Block block : blockBuilder.chunk(file.getPath(), statementBuilder.chunk(lexer.chunk(file)))) {
        fci.addBlock(block);
      }
    } catch (Exception e) {
      throw new DuplicationsException("Error in initialization", e);
    }
  }
}
