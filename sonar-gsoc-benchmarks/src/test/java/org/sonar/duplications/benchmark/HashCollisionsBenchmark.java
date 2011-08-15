package org.sonar.duplications.benchmark;

import org.sonar.duplications.DuplicationsException;
import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.BlockChunker;
import org.sonar.duplications.java.JavaStatementBuilder;
import org.sonar.duplications.java.JavaTokenProducer;
import org.sonar.duplications.statement.Statement;
import org.sonar.duplications.statement.StatementChunker;
import org.sonar.duplications.token.TokenChunker;
import org.sonar.duplications.token.TokenQueue;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HashCollisionsBenchmark extends Benchmark {

  private final String customName;
  private final List<File> files;
  private final BlockChunker blockChunker;

  private int totalBlocks;
  private int uniqueHashes;
  private int hashesChecksum;

  public HashCollisionsBenchmark(String customName, List<File> files, BlockChunker chnunker) {
    this.customName = customName;
    this.files = files;
    this.blockChunker = chnunker;
  }

  public int getTotalBlocks() {
    return totalBlocks;
  }

  public int getUniqueHashes() {
    return uniqueHashes;
  }

  public int getHashesChecksum() {
    return hashesChecksum;
  }

  @Override
  public String getName() {
    return getClass().getName() + " " + customName;
  }

  @Override
  public void runRound() throws Exception {
    TokenChunker tokenChunker = JavaTokenProducer.build();
    StatementChunker stmtChunker = JavaStatementBuilder.build();
    Map<String, List<Block>> fileBlocks = new LinkedHashMap<String, List<Block>>();
    Map<String, List<Block>> hashBlocks = new LinkedHashMap<String, List<Block>>();
    int totalBlocks = 0;
    for (File file : files) {
      try {
        TokenQueue tokenQueue = tokenChunker.chunk(file);
        List<Statement> statements = stmtChunker.chunk(tokenQueue);
        List<Block> blocks = blockChunker.chunk(file.getAbsolutePath(), statements);
        totalBlocks += blocks.size();
        fileBlocks.put(file.getAbsolutePath(), blocks);
        for (Block block : blocks) {
          List<Block> sameHash = hashBlocks.get(block.getBlockHash());
          if (sameHash == null) {
            sameHash = new ArrayList<Block>();
            hashBlocks.put(block.getBlockHash(), sameHash);
          }
          sameHash.add(block);
        }
      } catch (Exception e) {
        throw new DuplicationsException("Exception during chunking file: " + file.getAbsolutePath(), e);
      }
    }
    this.totalBlocks = totalBlocks;
    this.uniqueHashes = hashBlocks.size();
    this.hashesChecksum = 0;

    for (Map.Entry<String, List<Block>> entry : fileBlocks.entrySet()) {
      for (Block block : entry.getValue()) {
        int currentValue = hashBlocks.get(block.getBlockHash()).size();
        this.hashesChecksum = this.hashesChecksum * 31 + currentValue;
      }
    }
  }
}
