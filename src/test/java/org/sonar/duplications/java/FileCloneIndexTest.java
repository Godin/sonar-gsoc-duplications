package org.sonar.duplications.java;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.sonar.duplications.api.codeunit.Block;
import org.sonar.duplications.api.codeunit.BlockProvider;
import org.sonar.duplications.api.codeunit.StatementProvider;
import org.sonar.duplications.api.index.FileCloneIndex;
import org.sonar.duplications.api.lexer.Lexer;
import org.sonar.duplications.api.lexer.family.JavaLexer;
import org.sonar.duplications.api.lexer.family.StatementExtractor;

public class FileCloneIndexTest {
	
	BlockProvider blockProvider;
	File testFile = new File("test-resources/org/sonar/duplications/cpd/CPDTest/CPDFile1.java");

	@Before
	public void initTest(){
		Lexer tokenizer = JavaLexer.build();
		StatementExtractor statementExtractor = StatementExtractor.getInstance();
		StatementProvider statementProvider = new StatementProvider(tokenizer, statementExtractor);
		blockProvider = new BlockProvider(statementProvider, BlockProvider.DEFAULT_BLOCK_SIZE);
	}
	
	@Test
	public void shouldTokenizeSource(){
		FileCloneIndex  fci = new FileCloneIndex(testFile, blockProvider);
		assertThat(fci.getBlockList().size(), is(8));
	}
	
}
