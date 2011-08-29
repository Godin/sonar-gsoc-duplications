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

import com.google.common.collect.Maps;
import org.sonar.duplications.DuplicationsException;
import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.BlockChunker;
import org.sonar.duplications.block.ByteArray;
import org.sonar.duplications.java.JavaStatementBuilder;
import org.sonar.duplications.java.JavaTokenProducer;
import org.sonar.duplications.statement.Statement;
import org.sonar.duplications.statement.StatementChunker;
import org.sonar.duplications.token.TokenChunker;
import org.sonar.duplications.token.TokenQueue;

import java.io.File;
import java.util.ArrayList;
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
    Map<String, List<Block>> fileBlocks = Maps.newLinkedHashMap();
    Map<ByteArray, List<Block>> hashBlocks = Maps.newLinkedHashMap();
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
