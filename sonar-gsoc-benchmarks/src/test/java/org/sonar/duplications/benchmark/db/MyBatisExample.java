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
package org.sonar.duplications.benchmark.db;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.sonar.duplications.benchmark.Utils;
import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.BlockChunker;
import org.sonar.duplications.detector.original.OriginalCloneDetectionAlgorithm;
import org.sonar.duplications.index.CloneGroup;
import org.sonar.duplications.index.CloneIndex;
import org.sonar.duplications.index.MemoryCloneIndex;
import org.sonar.duplications.java.JavaStatementBuilder;
import org.sonar.duplications.java.JavaTokenProducer;
import org.sonar.duplications.statement.Statement;
import org.sonar.duplications.statement.StatementChunker;
import org.sonar.duplications.token.TokenChunker;
import org.sonar.duplications.token.TokenQueue;

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
 * <li>persistence of <tt>resourceId</tt> between different machines, which means that it can't be an absolute path to file, whereas this is a case here</li>
 * </ul>
 */
public class MyBatisExample {

  // private static final String environment = "h2";
  private static final String environment = "postgresql";

  private static final MyBatisIndex dbIndex = new MyBatisIndex(environment);

  private static void analyse(String project) {
    List<File> files = Utils.getProjectFiles(project);

    dbIndex.start(project);
    System.out.println("New snapshot created: " + project);

    TokenChunker tokenChunker = JavaTokenProducer.build();
    StatementChunker statementChunker = JavaStatementBuilder.build();
    BlockChunker blockChunker = new BlockChunker(13);

    CloneIndex combinedIndex = new CombinedCloneIndex(new MemoryCloneIndex(), dbIndex);

    for (File file : files) {
      TokenQueue tokenQueue = tokenChunker.chunk(file);
      List<Statement> statements = statementChunker.chunk(tokenQueue);
      List<Block> blocks = blockChunker.chunk(file.getAbsolutePath(), statements);
      for (Block block : blocks) {
        combinedIndex.insert(block);
      }
    }
    System.out.println("Index populated");

    int cloneGroups = 0;
    int cloneParts = 0;
    for (File file : files) {
      String resourceId = file.getAbsolutePath();

      dbIndex.prepareCache(resourceId);
      Collection<Block> fileBlocks = combinedIndex.getByResourceId(resourceId);

      List<CloneGroup> clones = OriginalCloneDetectionAlgorithm.detect(combinedIndex, fileBlocks);
      cloneGroups += clones.size();
      for (CloneGroup clone : clones) {
        cloneParts += clone.getCloneParts().size();
      }
    }

    dbIndex.done();
    System.out.println("Analysis completed");
    System.out.println(cloneGroups + " groups, " + cloneParts + " parts");
  }

  public static void main(String[] args) {
    dbIndex.removeAll();

    // struts-1.3.9 separately : 1098 groups, 8682 parts
    // struts-el-1.2.9 separately : 923 groups, 7452 parts

    // First analysis should provide same results as for separate analysis
    analyse("struts-1.3.9");
    // 1098 groups, 8682 parts

    // Next analysis should take into account all previous, i.e. will not provide same results as for separate analysis
    analyse("struts-el-1.2.9");
    // 1195 groups, 15256 parts

    // Next analysis should take into account all previous, i.e. will not provide same results as before
    analyse("struts-1.3.9");
    // 1328 groups, 15929 parts

    // Any future analysis will provide same results as before, because projects was not modified
    analyse("struts-el-1.2.9");
    // 1195 groups, 15256 parts
    analyse("struts-1.3.9");
    // 1328 groups, 15929 parts
  }
}
