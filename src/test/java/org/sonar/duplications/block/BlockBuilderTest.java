package org.sonar.duplications.block;

import java.io.File;
import java.util.List;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.sonar.duplications.DuplicationsTestUtil;
import org.sonar.duplications.java.JavaTokenProducer;
import org.sonar.duplications.statement.JavaStatementBuilder;
import org.sonar.duplications.statement.StatementChunker;
import org.sonar.duplications.token.TokenChunker;

public class BlockBuilderTest {

  File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest/CPDFile1.java");
  TokenChunker lexer = JavaTokenProducer.build();
  StatementChunker statementBuilder = JavaStatementBuilder.build();
  int blockSize = 3;
  BlockChunker blockBuilder = new BlockChunker(5);

  @Test
  @Ignore
  public void shouldTokenizeSource() {
    List<Block> blockList = blockBuilder.chunk("myFile",statementBuilder.chunk(lexer.chunk(testFile)));

    Assert.assertEquals(0, blockList.get(0).getIndexInFile());
    Assert.assertEquals(3, blockList.get(0).getFirstLineNumber());
    Assert.assertEquals(6, blockList.get(0).getLastLineNumber());

    Assert.assertEquals(7, blockList.get(blockList.size() - 1).getIndexInFile());
    Assert.assertEquals(8, blockList.get(blockList.size() - 1).getFirstLineNumber());
    Assert.assertEquals(11, blockList.get(blockList.size() - 1).getLastLineNumber());

    // assertThat(blockList, hasItems(
    // new Block(filename, null, 0 , 1 , 4),
    // new Block(filename, null, 7 , 9 , 11)));
  }

}
