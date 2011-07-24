package org.sonar.duplications.java;

import org.sonar.duplications.CloneGroupFinder;
import org.sonar.duplications.block.BlockChunker;
import org.sonar.duplications.index.CloneIndex;

/**
 * @author sharif
 */
public class JavaCloneGroupFinder
{

  private JavaCloneGroupFinder() {
  }

  public static CloneGroupFinder build(CloneIndex cloneIndex) {
    return JavaCloneGroupFinder.build(cloneIndex, 5);
  }

  public static CloneGroupFinder build(CloneIndex cloneIndex, int blockSize) {
	  CloneGroupFinder.Builder builder = CloneGroupFinder.build()
        .setTokenChunker(JavaTokenProducer.build())
        .setStatementChunker(JavaStatementBuilder.build())
        .setBlockChunker(new BlockChunker(blockSize))
        .setCloneIndex(cloneIndex);
    return builder.build();
  }
}
