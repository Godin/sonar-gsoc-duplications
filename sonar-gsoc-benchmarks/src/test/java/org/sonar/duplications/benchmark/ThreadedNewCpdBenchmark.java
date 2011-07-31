/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2011 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.duplications.benchmark;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.sonar.duplications.DuplicationsException;
import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.BlockChunker;
import org.sonar.duplications.index.Clone;
import org.sonar.duplications.index.CloneIndex;
import org.sonar.duplications.index.CloneReporter;
import org.sonar.duplications.index.MemoryCloneIndex;
import org.sonar.duplications.java.JavaStatementBuilder;
import org.sonar.duplications.java.JavaTokenProducer;
import org.sonar.duplications.statement.Statement;
import org.sonar.duplications.statement.StatementChunker;
import org.sonar.duplications.token.TokenChunker;
import org.sonar.duplications.token.TokenQueue;

import com.google.common.collect.Lists;

public class ThreadedNewCpdBenchmark extends Benchmark {

  private final List<File> files;
  private final int threadsCount;
  private final int blockSize;

  public ThreadedNewCpdBenchmark(List<File> files, int blockSize, int threadsCount) {
    this.files = files;
    this.blockSize = blockSize;
    this.threadsCount = threadsCount;
  }

  @Override
  public void runRound() throws Exception {
    singleRun(files, threadsCount, blockSize);
  }

  public static List<Clone> singleRun(List<File> files, int threadsCount, int blockSize) throws Exception {
    MemoryCloneIndex cloneIndex = new MemoryCloneIndex();
    populateIndex(files, threadsCount, blockSize, cloneIndex);
    // find clones
    List<Clone> clones = Lists.newArrayList();
    for (File file : files) {
      List<Block> candidateBlockList = Lists.newArrayList(cloneIndex.getByResourceId(file.getAbsolutePath()));
      clones.addAll(CloneReporter.reportClones(candidateBlockList, cloneIndex));
    }
    return clones;
  }

  /**
   * Notes about implementation:
   * <ul>
   * <li>We don't know amount of work required to process each file, so we can't define list of files for each thread, thus we should use queue.</li>
   * <li>We can't directly use {@link CloneIndex} in {@link Worker}, because {@link MemoryCloneIndex} is not thread-safe for update operations.
   * Thus worker returns results of his work to caller thread via {@link Future},
   * which means that results from all workers would be kept in memory until they would not processed in this method.</li>
   * </ul>
   */
  private static void populateIndex(List<File> files, int threadsCount, int blockSize, MemoryCloneIndex cloneIndex) throws InterruptedException, ExecutionException {
    ChunkersFactory chunkersFactory = new ChunkersFactory(blockSize);
    // create one task per thread
    ConcurrentLinkedQueue<File> filesQueue = new ConcurrentLinkedQueue<File>(files);
    List<Future<List<Block>>> futureResults = Lists.newArrayList();
    ExecutorService executor = Executors.newFixedThreadPool(threadsCount);
    for (int i = 0; i < threadsCount; i++) {
      futureResults.add(executor.submit(new Worker(chunkersFactory, filesQueue)));
    }
    // wait for completion of all tasks and save results into index
    for (Future<List<Block>> result : futureResults) {
      for (Block block : result.get()) {
        cloneIndex.insert(block);
      }
    }
    // shutdown executor for proper shutdown of JVM
    executor.shutdownNow();
  }

  /**
   * We use this factory to provide different chunkers for different threads,
   * because at least {@link TokenChunker} and {@link StatementChunker} are not thread-safe.
   */
  private static class ChunkersFactory {
    private final int blockSize;

    public ChunkersFactory(int blockSize) {
      this.blockSize = blockSize;
    }

    public TokenChunker createTokenChunker() {
      return JavaTokenProducer.build();
    }

    public StatementChunker createStatementChunker() {
      return JavaStatementBuilder.build();
    }

    public BlockChunker createBlockChunker() {
      return new BlockChunker(blockSize);
    }
  }

  private static class Worker implements Callable<List<Block>> {
    private final ChunkersFactory factory;
    private final ConcurrentLinkedQueue<File> filesQueue;

    public Worker(ChunkersFactory factory, ConcurrentLinkedQueue<File> filesQueue) {
      this.factory = factory;
      this.filesQueue = filesQueue;
    }

    public List<Block> call() throws Exception {
      TokenChunker tokenChunker = factory.createTokenChunker();
      StatementChunker statementChunker = factory.createStatementChunker();
      BlockChunker blockChunker = factory.createBlockChunker();

      List<Block> blocks = Lists.newArrayList();
      File file;
      while ((file = filesQueue.poll()) != null) {
        try {
          TokenQueue tokenQueue = tokenChunker.chunk(file);
          List<Statement> statements = statementChunker.chunk(tokenQueue);
          blocks.addAll(blockChunker.chunk(file.getAbsolutePath(), statements));
        } catch (Exception e) {
          throw new DuplicationsException("Exception during processing of file: " + file, e);
        }
      }
      return blocks;
    }
  }

  @Override
  public String getName() {
    return "new CPD threads=" + threadsCount;
  }

}
