package org.sonar.duplications.java;

import org.sonar.duplications.CloneFinder;
import org.sonar.duplications.algorithm.AdvancedGroupCloneReporter;
import org.sonar.duplications.algorithm.CloneReporterAlgorithm;
import org.sonar.duplications.block.BlockChunker;
import org.sonar.duplications.index.CloneIndex;

/**
 * @author sharif
 */
public final class JavaCloneFinder {

  private JavaCloneFinder() {
  }

  public static CloneFinder build(CloneIndex cloneIndex) {
    return JavaCloneFinder.build(cloneIndex, 5);
  }

  public static CloneFinder build(CloneIndex cloneIndex, int blockSize) {
    return JavaCloneFinder.build(cloneIndex, blockSize, new AdvancedGroupCloneReporter(cloneIndex));
  }

  public static CloneFinder build(CloneIndex cloneIndex, int blockSize, CloneReporterAlgorithm cloneReporter) {
    CloneFinder.Builder builder = CloneFinder.build()
        .setTokenChunker(JavaTokenProducer.build())
        .setStatementChunker(JavaStatementBuilder.build())
        .setBlockChunker(new BlockChunker(blockSize))
        .setCloneIndex(cloneIndex)
        .setCloneReporter(cloneReporter);
    return builder.build();
  }
}
