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
package org.sonar.duplications.benchmark.index;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.sonar.duplications.benchmark.Benchmark;
import org.sonar.duplications.benchmark.BenchmarkResult;
import org.sonar.duplications.benchmark.BenchmarksDiff;
import org.sonar.duplications.benchmark.MemoryUtils;
import org.sonar.duplications.benchmark.SizeOf;
import org.sonar.duplications.benchmark.TimingProxy;
import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.BlockChunker;
import org.sonar.duplications.index.CloneIndex;
import org.sonar.duplications.index.MemoryCloneIndex;
import org.sonar.duplications.index.MemoryCloneIndex2;
import org.sonar.duplications.index.PackedMemoryCloneIndex;
import org.sonar.duplications.java.JavaStatementBuilder;
import org.sonar.duplications.java.JavaTokenProducer;
import org.sonar.duplications.statement.Statement;
import org.sonar.duplications.statement.StatementChunker;
import org.sonar.duplications.token.TokenChunker;
import org.sonar.duplications.token.TokenQueue;

public abstract class AbstractIndexesTestCase {

  protected static int BLOCK_SIZE = 13;
  protected static int WARMUP_ROUNDS = 1;
  protected static int BENCHMARK_ROUNDS = 2;

  protected static List<File> files;
  protected static BenchmarksDiff results = new BenchmarksDiff();

  protected BenchmarkResult run(IndexBenchmark benchmark) {
    return benchmark.runBenchmark(BENCHMARK_ROUNDS, WARMUP_ROUNDS);
  }

  @Before
  public void setUp() {
    MemoryUtils.cleanup();
  }

  @After
  public void tearDown() {
    MemoryUtils.cleanup();
  }

  @AfterClass
  public static void after() {
    results.print();
  }

  @Test
  public void test1() {
    results.setReference(run(new IndexBenchmark(files) {
      @Override
      protected CloneIndex createIndex() {
        return new MemoryCloneIndex();
      }

      @Override
      public String getName() {
        return "Memory";
      }
    }));
  }

  @Test
  public void test2() {
    results.add(run(new IndexBenchmark(files) {
      @Override
      protected CloneIndex createIndex() {
        return new MemoryCloneIndex2();
      }

      @Override
      public String getName() {
        return "Memory2";
      }
    }));
  }

  @Test
  public void test3() {
    results.add(run(new IndexBenchmark(files) {
      @Override
      protected CloneIndex createIndex() {
        return new PackedMemoryCloneIndex();
      }

      @Override
      public String getName() {
        return "Packed";
      }
    }));
  }

  private static abstract class IndexBenchmark extends Benchmark {
    private final List<File> files;

    public IndexBenchmark(List<File> files) {
      this.files = files;
    }

    @Override
    public void runRound() throws Exception {
      CloneIndex index = TimingProxy.newInstance(createIndex());
      TokenChunker tokenChunker = JavaTokenProducer.build();
      StatementChunker statementChunker = JavaStatementBuilder.build();
      BlockChunker blockChunker = new BlockChunker(BLOCK_SIZE);

      for (File file : files) {
        TokenQueue tokenQueue = tokenChunker.chunk(file);
        List<Statement> statements = statementChunker.chunk(tokenQueue);
        List<Block> blocks = blockChunker.chunk(file.getAbsolutePath(), statements);
        for (Block block : blocks) {
          index.insert(block);
        }
      }

      for (File file : files) {
        Collection<Block> fileBlocks = index.getByResourceId(file.getAbsolutePath());
        for (Block block : fileBlocks) {
          index.getBySequenceHash(block.getBlockHash());
        }
      }

      TimingProxy.getHandlerFor(index).printTimings();

      if (isLastRound()) {
        System.out.println("Size of index on 32bit / 64bit : " + SizeOf.sizeOf(index) / 1024 / 1024 + " / " + SizeOf.sizeOfOn64(index) / 1024 / 1024);
      }
    }

    protected abstract CloneIndex createIndex();

  }

}
