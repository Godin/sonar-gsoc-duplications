package org.sonar.duplications.benchmark.hash;

import org.sonar.duplications.DuplicationsException;
import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.BlockChunker;
import org.sonar.duplications.statement.Statement;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class DigestHashBlockChunker extends BlockChunker {

  protected final MessageDigest digest;
  private int blockSize;

  public static enum Algorithm {
    MD5, SHA;
  }

  public DigestHashBlockChunker(Algorithm algorithm, int blockSize) {
    super(blockSize);
    this.blockSize = blockSize;
    try {
      this.digest = MessageDigest.getInstance(algorithm.toString());
    } catch (NoSuchAlgorithmException e) {
      throw new DuplicationsException("Unable to create a digest generator", e);
    }
  }

  @Override
  public List<Block> chunk(String resourceId, List<Statement> statements) {
    List<Statement> statementsForBlock = new ArrayList<Statement>();
    List<Block> blockList = new ArrayList<Block>();

    for (Statement stmt : statements) {
      statementsForBlock.add(stmt);
      if (statementsForBlock.size() == blockSize) {
        Statement firstStmt = statementsForBlock.get(0);
        Statement lastStmt = statementsForBlock.get(blockSize - 1);
        blockList.add(new Block(resourceId,
            buildBlockHash(statementsForBlock),
            blockList.size(),
            firstStmt.getStartLine(),
            lastStmt.getEndLine()));
        statementsForBlock.remove(0);
      }
    }

    return blockList;
  }

  private static final String HEXES = "0123456789abcdef";

  private String getHex(byte[] raw) {
    if (raw == null) {
      return null;
    }
    final StringBuilder hex = new StringBuilder(2 * raw.length);
    for (final byte b : raw) {
      hex.append(HEXES.charAt((b & 0xF0) >> 4))
          .append(HEXES.charAt((b & 0x0F)));
    }
    return hex.toString();
  }

  private String buildBlockHash(List<Statement> statementList) {
    digest.reset();
    for (Statement statement : statementList) {
      digest.update(statement.getValue().getBytes());
    }
    byte[] messageDigest = digest.digest();
    return getHex(messageDigest);
  }
}
