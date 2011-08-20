package org.sonar.duplications.benchmark.hash;

import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.BlockChunker;
import org.sonar.duplications.block.ByteArray;
import org.sonar.duplications.statement.Statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: skydiver
 * Date: 14.08.11
 * Time: 22:52
 */
public class IntRabinKarpBlockChunker extends BlockChunker {

  private static final int PRIME_BASE = 31;
  private int power;

  private int blockSize;

  public IntRabinKarpBlockChunker(int blockSize) {
    super(blockSize);

    this.blockSize = blockSize;
    power = 1;
    for (int i = 0; i < blockSize; i++) {
      power = power * PRIME_BASE;
    }
  }

  public List<Block> chunk(String resourceId, List<Statement> statements) {
    if (statements.size() < blockSize) {
      return Collections.emptyList();
    }
    List<Block> blockList = new ArrayList<Block>();
    int hash = 0;
    for (int i = 0; i < statements.size(); i++) {
      // add current statement to hash
      Statement current = statements.get(i);
      hash = hash * PRIME_BASE + current.getValue().hashCode();
      // remove first statement from hash, if needed
      int j = i - blockSize + 1;
      if (j > 0) {
        hash -= power * statements.get(j - 1).getValue().hashCode();
      }
      // create block
      if (j >= 0) {
        Statement first = statements.get(j);
        blockList.add(new Block(resourceId, new ByteArray(hash), j, first.getStartLine(), current.getEndLine()));
      }
    }
    return blockList;
  }

}
