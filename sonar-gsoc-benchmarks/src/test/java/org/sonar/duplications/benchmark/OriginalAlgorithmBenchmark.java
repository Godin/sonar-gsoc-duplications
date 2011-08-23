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

import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.BlockChunker;
import org.sonar.duplications.detector.original.OriginalCloneDetectionAlgorithm;
import org.sonar.duplications.index.CloneIndex;
import org.sonar.duplications.index.PackedMemoryCloneIndex;
import org.sonar.duplications.java.JavaStatementBuilder;
import org.sonar.duplications.java.JavaTokenProducer;
import org.sonar.duplications.statement.Statement;
import org.sonar.duplications.statement.StatementChunker;
import org.sonar.duplications.token.TokenChunker;
import org.sonar.duplications.token.TokenQueue;

import com.google.common.collect.Lists;

public class OriginalAlgorithmBenchmark extends Benchmark {

  private final List<File> files;
  private final int blockSize;

  public OriginalAlgorithmBenchmark(List<File> files, int blockSize) {
    this.files = files;
    this.blockSize = blockSize;
  }

  @Override
  public void runRound() throws Exception {
    System.out.println("Count: " + singleRun(files, blockSize));
  }

  public static int singleRun(List<File> files, int blockSize) {
    CloneIndex delegate = new PackedMemoryCloneIndex();
    // CloneIndex index = new MemoryCloneIndex();
    CloneIndex index = TimingProxy.newInstance(delegate);
    TokenChunker tokenChunker = JavaTokenProducer.build();
    StatementChunker statementChunker = JavaStatementBuilder.build();
    BlockChunker blockChunker = new BlockChunker(blockSize);

    for (File file : files) {
      TokenQueue tokenQueue = tokenChunker.chunk(file);
      List<Statement> statements = statementChunker.chunk(tokenQueue);
      List<Block> blocks = blockChunker.chunk(file.getAbsolutePath(), statements);
      for (Block block : blocks) {
        index.insert(block);
      }
    }

    int count = 0;
    for (File file : files) {
      List<Block> fileBlocks = Lists.newArrayList(index.getByResourceId(file.getAbsolutePath()));
      count += OriginalCloneDetectionAlgorithm.detect(index, fileBlocks).size();
    }
    TimingProxy.getHandlerFor(index).printTimings();
    return count;
  }

  @Override
  public String getName() {
    return "OriginBenchmark";
  }

}
