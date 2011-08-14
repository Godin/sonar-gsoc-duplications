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

import com.google.common.collect.Lists;
import org.sonar.duplications.DuplicationsException;
import org.sonar.duplications.algorithm.AdvancedGroupCloneReporter;
import org.sonar.duplications.algorithm.CloneReporterAlgorithm;
import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.BlockChunker;
import org.sonar.duplications.block.FileBlockGroup;
import org.sonar.duplications.index.CloneIndex;
import org.sonar.duplications.index.MemoryCloneIndex;
import org.sonar.duplications.java.JavaStatementBuilder;
import org.sonar.duplications.java.JavaTokenProducer;
import org.sonar.duplications.statement.Statement;
import org.sonar.duplications.statement.StatementChunker;
import org.sonar.duplications.token.TokenChunker;
import org.sonar.duplications.token.TokenQueue;

import java.io.File;
import java.util.List;
import java.util.concurrent.*;

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

  /**
   * Notes about implementation:
   * <ul>
   * <li>We don't know amount of work required to process each file, so we can't define list of files for each thread, thus we should use queue.</li>
   * <li>We can't directly use {@link CloneIndex} in {@link Worker}, because {@link MemoryCloneIndex} is not thread-safe for update operations,
   * thus we use {@link IndexUpdater}.</li>
   * </ul>
   */
  private static void singleRun(List<File> files, int threadsCount, int blockSize) throws Exception {
    MemoryCloneIndex index = new MemoryCloneIndex();

    ExecutorService executor = Executors.newFixedThreadPool(threadsCount);
    populateIndex(executor, threadsCount, files, new IndexUpdater(index), blockSize);
    search(executor, threadsCount, files, index);

    // shutdown executor for proper shutdown of JVM
    executor.shutdownNow();
  }

  private static void search(ExecutorService executor, int threadsCount, List<File> files, CloneIndex index) throws InterruptedException, ExecutionException {
    ConcurrentLinkedQueue<File> filesQueue = new ConcurrentLinkedQueue<File>(files);
    List<Future> futures = Lists.newArrayList();
    for (int i = 0; i < threadsCount; i++) {
      futures.add(executor.submit(new Worker2(filesQueue, index)));
    }
    // wait for completion of all tasks
    for (Future future : futures) {
      future.get();
    }
  }

  private static void populateIndex(ExecutorService executor, int threadsCount, List<File> files, IndexUpdater index, int blockSize) throws InterruptedException, ExecutionException {
    ChunkersFactory chunkersFactory = new ChunkersFactory(blockSize);
    ConcurrentLinkedQueue<File> filesQueue = new ConcurrentLinkedQueue<File>(files);
    List<Future> futures = Lists.newArrayList();
    for (int i = 0; i < threadsCount; i++) {
      futures.add(executor.submit(new Worker(chunkersFactory, filesQueue, index)));
    }
    // wait for completion of all tasks
    for (Future future : futures) {
      future.get();
    }
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

  public static class IndexUpdater {
    private final CloneIndex index;

    public IndexUpdater(CloneIndex index) {
      this.index = index;
    }

    public void save(String resourceId, List<Block> blocks) {
      synchronized (this) {
        for (Block block : blocks) {
          index.insert(block);
        }
      }
    }
  }

  private static abstract class AbstractWorker implements Callable<Object> {
    private final ConcurrentLinkedQueue<File> filesQueue;

    public AbstractWorker(ConcurrentLinkedQueue<File> filesQueue) {
      this.filesQueue = filesQueue;
    }

    public final Object call() throws Exception {
      File file;
      while ((file = filesQueue.poll()) != null) {
        try {
          processFile(file);
        } catch (Exception e) {
          throw new DuplicationsException("Exception during processing of file: " + file, e);
        }
      }
      return null;
    }

    protected abstract void processFile(File file);
  }

  private static class Worker extends AbstractWorker {
    private final IndexUpdater index;

    private final TokenChunker tokenChunker;
    private final StatementChunker statementChunker;
    private final BlockChunker blockChunker;

    public Worker(ChunkersFactory factory, ConcurrentLinkedQueue<File> filesQueue, IndexUpdater index) {
      super(filesQueue);
      this.index = index;
      tokenChunker = factory.createTokenChunker();
      statementChunker = factory.createStatementChunker();
      blockChunker = factory.createBlockChunker();
    }

    @Override
    protected void processFile(File file) {
      TokenQueue tokenQueue = tokenChunker.chunk(file);
      List<Statement> statements = statementChunker.chunk(tokenQueue);
      List<Block> blocks = blockChunker.chunk(file.getAbsolutePath(), statements);
      index.save(file.getAbsolutePath(), blocks);
    }
  }

  private static class Worker2 extends AbstractWorker {
    private final CloneIndex cloneIndex;
    private final CloneReporterAlgorithm cloneReporter;

    public Worker2(ConcurrentLinkedQueue<File> filesQueue, CloneIndex cloneIndex) {
      super(filesQueue);
      this.cloneIndex = cloneIndex;
      cloneReporter = new AdvancedGroupCloneReporter(cloneIndex);
    }

    @Override
    protected void processFile(File file) {
      List<Block> fileBlocks = Lists.newArrayList(cloneIndex.getByResourceId(file.getAbsolutePath()));
      FileBlockGroup fileBlockGroup = FileBlockGroup.create(file.getAbsolutePath(), fileBlocks);
      cloneReporter.reportClones(fileBlockGroup);
    }
  }

  @Override
  public String getName() {
    return "new CPD threads=" + threadsCount;
  }

}
