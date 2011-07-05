package org.sonar.duplications.block;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.sonar.duplications.DuplicationsException;
import org.sonar.duplications.statement.Statement;

/**
 * this class provides a list of blocks from a list of statements
 * 
 * @author sharif
 */
public class BlockChunker {

  private int blockSize;

  private final MessageDigest digest;

  public BlockChunker(int blockSize) {
    this.blockSize = blockSize;
    try {
      this.digest = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      throw new DuplicationsException("Unable to create a MD5 generator", e);
    }
  }

  public List<Block> chunk(String resourceId, List<Statement> statements) {
    List<Statement> statementsForBlock = new LinkedList<Statement>();
    List<Block> blockList = new ArrayList<Block>();

    for (Statement stmt : statements) {
      statementsForBlock.add(stmt);
      if (statementsForBlock.size() == blockSize) {
        Statement firstStmt = statementsForBlock.get(0);
        Statement lastStmt = statementsForBlock.get(blockSize - 1);
        blockList.add(new Block(resourceId, buildBlockHash(statementsForBlock), firstStmt.getIndexInFile(), firstStmt.getStartLine(),
            lastStmt.getEndLine()));
        statementsForBlock.remove(0);
      }
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