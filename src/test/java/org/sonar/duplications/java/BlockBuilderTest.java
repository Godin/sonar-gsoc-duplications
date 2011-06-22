package org.sonar.duplications.java;

import java.io.File;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.sonar.duplications.api.codeunit.Block;
import org.sonar.duplications.api.lexer.BlockBuilder;
import org.sonar.duplications.api.lexer.Lexer;
import org.sonar.duplications.api.lexer.StatementBuilder;
import org.sonar.duplications.api.lexer.family.JavaLexer;
import org.sonar.duplications.api.lexer.family.JavaStatementBuilder;

public class BlockBuilderTest {

	File testFile = new File("test-resources/org/sonar/duplications/cpd/CPDTest/CPDFile1.java");
	Lexer lexer = JavaLexer.build();
	StatementBuilder statementBuilder = JavaStatementBuilder.build();
	int blockSize = 3;
	BlockBuilder blockBuilder = new BlockBuilder(testFile);

	@Test
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
