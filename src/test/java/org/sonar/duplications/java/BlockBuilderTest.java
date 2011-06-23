package org.sonar.duplications.java;

import java.io.File;
import java.util.List;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.sonar.duplications.DuplicationsTestUtil;
import org.sonar.duplications.api.Block;
import org.sonar.duplications.api.BlockBuilder;
import org.sonar.duplications.statement.StatementBuilder;
import org.sonar.duplications.token.Lexer;

public class BlockBuilderTest {

  File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest/CPDFile1.java");
  Lexer lexer = JavaLexer.build();
  StatementBuilder statementBuilder = JavaStatementBuilder.build();
  int blockSize = 3;
  BlockBuilder blockBuilder = new BlockBuilder(testFile);

  @Test
  @Ignore
  public void shouldTokenizeSource() {
    List<Block> blockList = blockBuilder.build(statementBuilder.build(lexer.lex(testFile)));

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
