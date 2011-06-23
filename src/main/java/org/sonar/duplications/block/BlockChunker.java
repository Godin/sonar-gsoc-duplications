package org.sonar.duplications.block;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sonar.duplications.DuplicationsException;
import org.sonar.duplications.statement.Statement;

/**
 * this class provides a list of blocks from a list of statements
 * 
 * @author sharif
 */
public class BlockChunker {

  private static final long serialVersionUID = -7421443570641400239L;

  public final static int DEFAULT_BLOCK_SIZE = 5;

  private File sourceFile;

  private int blockSize;

  private final MessageDigest digest;

  public BlockChunker(File sourceFile) {
    this(sourceFile, DEFAULT_BLOCK_SIZE);
  }

  public BlockChunker(File sourceFile, int blockSize) {
    this.blockSize = blockSize;
    this.sourceFile = sourceFile;
    try {
      this.digest = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      throw new DuplicationsException(e.getMessage());
    }
  }

  public List<Block> chunk(List<Statement> statements) {
    Iterator<Statement> stmtIterator = statements.iterator();
    List<Statement> statementsForBlock = new ArrayList<Statement>();
    List<Block> blockList = new ArrayList<Block>();

    try {
      // build the first block
      for (int i = 0; i < blockSize && stmtIterator.hasNext(); i++) {
        statementsForBlock.add(stmtIterator.next());
      }
      blockList.add(new Block(sourceFile.getAbsolutePath(), buildBlockHash(statementsForBlock), statementsForBlock.get(0).getIndexInFile(),
          statementsForBlock.get(0).getStartLine(), statementsForBlock.get(statementsForBlock.size() - 1).getEndLine()));

      // and now build the remaining blocks
      while (stmtIterator.hasNext()) {

        statementsForBlock.remove(0);
        statementsForBlock.add(stmtIterator.next());
        blockList
            .add(new Block(sourceFile.getAbsolutePath(), buildBlockHash(statementsForBlock), statementsForBlock.get(0).getIndexInFile(),
                statementsForBlock.get(0).getStartLine(), statementsForBlock.get(statementsForBlock.size() - 1).getEndLine()));
      }
    } catch (Exception e) {
      throw new DuplicationsException(e.getMessage());
    }

    return blockList;
  }

  private String buildBlockHash(List<Statement> statementList) {
    digest.reset();
    for (Statement statement : statementList) {
      digest.update(statement.getValue().getBytes());
    }
    byte messageDigest[] = digest.digest();
    BigInteger number = new BigInteger(1, messageDigest);
    return String.format("%1$032X", number);
  }

}