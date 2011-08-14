package org.sonar.duplications.benchmark.hash;

import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.BlockChunker;
import org.sonar.duplications.statement.Statement;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MurmurHashBlockChunker extends BlockChunker {

  private int blockSize;

  public MurmurHashBlockChunker(int blockSize) {
    super(blockSize);
    this.blockSize = blockSize;
  }

  @Override
  public List<Block> chunk(String resourceId, List<Statement> statements) {
    LinkedList<Statement> statementsForBlock = new LinkedList<Statement>();
    List<Block> blockList = new ArrayList<Block>();

    for (Statement stmt : statements) {
      statementsForBlock.add(stmt);
      if (statementsForBlock.size() == blockSize) {
        Statement firstStmt = statementsForBlock.getFirst();
        Statement lastStmt = statementsForBlock.getLast();
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

  private String buildBlockHash(List<Statement> statementList) {
    int totalLen = 0;
    for (Statement statement : statementList) {
      totalLen += statement.getValue().getBytes().length;
    }
    byte[] bytes = new byte[totalLen];
    int current = 0;
    for (Statement statement : statementList) {
      byte[] stmtBytes = statement.getValue().getBytes();
      int length = stmtBytes.length;
      System.arraycopy(stmtBytes, 0, bytes, current, length);
      current += length;
    }
    int messageDigest = MurmurHash2.hash(bytes, 0x1234ABCD);
    return Integer.toHexString(messageDigest).toLowerCase();
  }
}
