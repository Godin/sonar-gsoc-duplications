package org.sonar.duplications.benchmark.hash;

import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.BlockChunker;
import org.sonar.duplications.statement.Statement;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractHashBlockChunker extends BlockChunker {

  protected int blockSize;

  public AbstractHashBlockChunker(int blockSize) {
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

  protected abstract String buildBlockHash(List<Statement> statements);

}
