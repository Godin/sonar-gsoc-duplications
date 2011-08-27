package org.sonar.duplications.benchmark.hash;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.BlockChunker;
import org.sonar.duplications.block.ByteArray;
import org.sonar.duplications.statement.Statement;

import com.google.common.collect.Lists;

public abstract class AbstractHashBlockChunker extends BlockChunker {

  public AbstractHashBlockChunker(int blockSize) {
    super(blockSize);
  }

  @Override
  public List<Block> chunk(String resourceId, List<Statement> statements) {
    if (statements.size() < getBlockSize()) {
      return Collections.emptyList();
    }

    LinkedList<Statement> statementsForBlock = new LinkedList<Statement>();
    List<Block> blocks = Lists.newArrayListWithCapacity(statements.size() - getBlockSize() + 1);

    for (Statement stmt : statements) {
      statementsForBlock.add(stmt);
      if (statementsForBlock.size() == getBlockSize()) {
        Statement firstStatement = statementsForBlock.getFirst();
        Statement lastStatement = statementsForBlock.getLast();
        blocks.add(new Block(resourceId,
            buildBlockHash(statementsForBlock),
            blocks.size(),
            firstStatement.getStartLine(),
            lastStatement.getEndLine()));
        statementsForBlock.removeFirst();
      }
    }

    return blocks;
  }

  protected abstract ByteArray buildBlockHash(List<Statement> statements);

}
