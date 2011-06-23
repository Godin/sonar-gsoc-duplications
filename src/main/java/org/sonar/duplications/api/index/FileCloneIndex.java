package org.sonar.duplications.api.index;

import java.io.File;
import java.util.List;

import org.sonar.duplications.api.Block;
import org.sonar.duplications.api.BlockBuilder;
import org.sonar.duplications.api.DuplicationsException;
import org.sonar.duplications.api.Lexer;
import org.sonar.duplications.api.StatementBuilder;
import org.sonar.duplications.java.JavaLexer;
import org.sonar.duplications.java.JavaStatementBuilder;

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

      blockList = blockBuilder.build(statementBuilder.build(lexer.lex(sourceFile)));
    } catch (Exception e) {
      throw new DuplicationsException("Error in initialization", e);
    }
  }

}
