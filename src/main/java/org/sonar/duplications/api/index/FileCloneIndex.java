package org.sonar.duplications.api.index;

import java.io.File;
import java.util.List;

import org.sonar.duplications.api.CloneIndexException;
import org.sonar.duplications.api.codeunit.Block;
import org.sonar.duplications.api.lexer.BlockBuilder;
import org.sonar.duplications.api.lexer.Lexer;
import org.sonar.duplications.api.lexer.StatementBuilder;
import org.sonar.duplications.api.lexer.family.JavaLexer;
import org.sonar.duplications.api.lexer.family.JavaStatementBuilder;

/**
 * @author sharif
 */
public class FileCloneIndex {

	private File sourceFile;
	private List<Block> blockList;

	public FileCloneIndex(File sourceFile) {
		this.sourceFile = sourceFile;
	}

	public File getSourceFile() {
		return sourceFile;
	}

	public List<Block> getBlockList() {
		return blockList;
	}

	public void init() {
		try {
			Lexer lexer = JavaLexer.build();
			StatementBuilder statementBuilder = JavaStatementBuilder.build();
			BlockBuilder blockBuilder = new BlockBuilder(sourceFile);

			blockList = blockBuilder.build(statementBuilder.build(lexer
					.lex(sourceFile)));
		} catch (Exception e) {
			throw new CloneIndexException("Error in initialization", e);
		}
	}

}
