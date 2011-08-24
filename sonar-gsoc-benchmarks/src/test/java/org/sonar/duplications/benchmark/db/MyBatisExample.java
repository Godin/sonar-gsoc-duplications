package org.sonar.duplications.benchmark.db;

import java.io.File;
import java.util.List;

import org.sonar.duplications.benchmark.TimingProxy;
import org.sonar.duplications.benchmark.Utils;
import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.BlockChunker;
import org.sonar.duplications.detector.original.OriginalCloneDetectionAlgorithm;
import org.sonar.duplications.java.JavaStatementBuilder;
import org.sonar.duplications.java.JavaTokenProducer;
import org.sonar.duplications.statement.Statement;
import org.sonar.duplications.statement.StatementChunker;
import org.sonar.duplications.token.TokenChunker;
import org.sonar.duplications.token.TokenQueue;

import com.google.common.collect.Lists;

/**
 * This is example of usage of database to store index.
 *
 * TODO However incremental approach not covered here - table always cleared before analysis.
 * Incremental approach for index maintenance requires:
 * <ul>
 * <li>removal of resources, for which associated file was removed from project</li>
 * <li>renewal of blocks for files, to increase performance - only modified files should be processed</li>
 * </ul>
 * Distributed approach requires:
 * <ul>
 * <li>persistence of <tt>resourceId</tt> between different machines, which means that it can't be an absolute path to file, whereas this is a case now</li>
 * </ul>
 */
public class MyBatisExample {

  public static void main(String[] args) {
    List<File> files = Utils.filesFromJdk16();

    // String environment = "h2";
    String environment = "postgresql";

    BatchIndex index = TimingProxy.newInstance(new MyBatisIndex(environment));
    index.removeAll();
    System.out.println("Index initialized");

    TokenChunker tokenChunker = JavaTokenProducer.build();
    StatementChunker statementChunker = JavaStatementBuilder.build();
    BlockChunker blockChunker = new BlockChunker(13);

    for (File file : files) {
      TokenQueue tokenQueue = tokenChunker.chunk(file);
      List<Statement> statements = statementChunker.chunk(tokenQueue);
      List<Block> blocks = blockChunker.chunk(file.getAbsolutePath(), statements);
      for (Block block : blocks) {
        index.insert(block);
      }
    }
    System.out.println("Index created");

    int count = 0;
    for (File file : files) {
      index.prepareCache(file.getAbsolutePath());

      List<Block> fileBlocks = Lists.newArrayList(index.getByResourceId(file.getAbsolutePath()));
      count += OriginalCloneDetectionAlgorithm.detect(index, fileBlocks).size();
    }

    System.out.println("Clones: " + count);
    TimingProxy.getHandlerFor(index).printTimings();
  }

}
