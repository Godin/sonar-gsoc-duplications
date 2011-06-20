package org.sonar.duplications.java;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.sonar.duplications.api.codeunit.Block;
import org.sonar.duplications.api.codeunit.BlockProvider;
import org.sonar.duplications.api.codeunit.StatementProvider;
import org.sonar.duplications.api.lexer.Lexer;
import org.sonar.duplications.api.lexer.family.JavaLexer;
import org.sonar.duplications.api.lexer.family.StatementExtractor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
@Ignore
public class BlockProviderTest {

  BlockProvider blockProvider;

  File testFile = new File("test-resources/org/sonar/duplications/cpd/CPDTest/CPDFile1.java");

  @Ignore//this test will be updated after statement builder framework integration is done
  public void initTest() {
    Lexer tokenizer = JavaLexer.build();
    StatementExtractor statementExtractor = StatementExtractor.getInstance();
    StatementProvider statementProvider = new StatementProvider(tokenizer, statementExtractor);
    blockProvider = new BlockProvider(statementProvider, BlockProvider.DEFAULT_BLOCK_SIZE);
    blockProvider.init(testFile);
  }

  @Ignore//this test will be updated after statement builder framework integration is done
  public void shouldTokenizeSource() {
    List<Block> blockList = new ArrayList<Block>();
    Block block;
    while ((block = blockProvider.getNext()) != null) {
      blockList.add(block);
      //	System.out.println(block);
    }

    Assert.assertEquals(0, blockList.get(0).getFirstUnitIndex());
    Assert.assertEquals(3, blockList.get(0).getFirstLineNumber());
    Assert.assertEquals(7, blockList.get(0).getLastLineNumber());

    Assert.assertEquals(7, blockList.get(blockList.size() - 1).getFirstUnitIndex());
    Assert.assertEquals(10, blockList.get(blockList.size() - 1).getFirstLineNumber());
    Assert.assertEquals(14, blockList.get(blockList.size() - 1).getLastLineNumber());

//		assertThat(blockList, hasItems(
//				new Block(filename, null, 0 , 1 , 4), 
//				new Block(filename, null, 7 , 9 , 11)));

  }

}
