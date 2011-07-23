package org.sonar.duplications.block;

import junit.framework.Assert;
import org.junit.Test;
import org.sonar.duplications.DuplicationsTestUtil;
import org.sonar.duplications.java.JavaStatementBuilder;
import org.sonar.duplications.java.JavaTokenProducer;
import org.sonar.duplications.statement.StatementChunker;
import org.sonar.duplications.token.TokenChunker;

import java.io.File;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class BlockBuilderTest {

  File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest/CPDFile1.java");
  TokenChunker lexer = JavaTokenProducer.build();
  StatementChunker statementBuilder = JavaStatementBuilder.build();
  BlockChunker blockBuilder = new BlockChunker(5);

  @Test
  public void shouldBuildBlockFromSource() {
    List<Block> blockList = blockBuilder.chunk(testFile.getPath(), statementBuilder.chunk(lexer.chunk(testFile)));

    assertThat(blockList.size(), is(8));

    Assert.assertEquals(0, blockList.get(0).getIndexInFile());
    Assert.assertEquals(3, blockList.get(0).getFirstLineNumber());
    Assert.assertEquals(6, blockList.get(0).getLastLineNumber());

    Assert.assertEquals(7, blockList.get(blockList.size() - 1).getIndexInFile());
    Assert.assertEquals(8, blockList.get(blockList.size() - 1).getFirstLineNumber());
    Assert.assertEquals(11, blockList.get(blockList.size() - 1).getLastLineNumber());
  }
}
