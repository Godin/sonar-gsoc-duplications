package org.sonar.duplications.java;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Test;
import org.sonar.duplications.api.CloneIndexException;
import org.sonar.duplications.api.codeunit.Block;
import org.sonar.duplications.api.index.FileBlockGroup;
import org.sonar.duplications.api.lexer.BlockBuilder;
import org.sonar.duplications.api.lexer.Lexer;
import org.sonar.duplications.api.lexer.StatementBuilder;
import org.sonar.duplications.api.lexer.family.JavaLexer;
import org.sonar.duplications.api.lexer.family.JavaStatementBuilder;

public class FileBlockGroupTest {

	File testFile = new File("test-resources/org/sonar/duplications/cpd/CPDTest/CPDFile1.java");
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

  @Test(expected = CloneIndexException.class)
  public void testWrongResourceId() {
    FileBlockGroup file = new FileBlockGroup("a");
    file.addBlock(new Block("b", "13dws2324d", 1, 1, 7));
  }
}
