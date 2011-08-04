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
package org.sonar.duplications.detector.original;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.duplications.DuplicationsTestUtil;
import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.BlockChunker;
import org.sonar.duplications.detector.original.OriginalCloneDetectionAlgorithm;
import org.sonar.duplications.index.CloneGroup;
import org.sonar.duplications.index.ClonePart;
import org.sonar.duplications.index.MemoryCloneIndex;
import org.sonar.duplications.java.JavaStatementBuilder;
import org.sonar.duplications.java.JavaTokenProducer;
import org.sonar.duplications.junit.TestNamePrinter;
import org.sonar.duplications.statement.Statement;
import org.sonar.duplications.statement.StatementChunker;
import org.sonar.duplications.token.TokenChunker;
import org.sonar.duplications.token.TokenQueue;

public class OriginalCloneDetectionAlgorithmIntegrationTest {

  @Rule
  public TestNamePrinter name = new TestNamePrinter();

  private File file1 = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest/CPDFile1.java");
  private File file2 = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest/CPDFile2.java");
  private File file3 = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest/CPDFile3.java");

  private MemoryCloneIndex cloneIndex;
  private TokenChunker tokenChunker;
  private StatementChunker statementChunker;
  private BlockChunker blockChunker;

  @Before
  public void setUp() {
    tokenChunker = JavaTokenProducer.build();
    statementChunker = JavaStatementBuilder.build();
    blockChunker = new BlockChunker(5);
    cloneIndex = new MemoryCloneIndex();
  }

  @Test
  public void test1() {
    register(getBlocks(file1));
    List<Block> fileBlocks = getBlocks(file2);
    List<CloneGroup> clones = OriginalCloneDetectionAlgorithm.detect(cloneIndex, fileBlocks);

    assertThat(clones.size(), is(1));
    CloneGroup clone = clones.get(0);
    assertThat(clone.getCloneUnitLength(), is(5));
    List<ClonePart> cloneParts = clone.getCloneParts();
    assertThat(cloneParts.size(), is(2));
    ClonePart part1 = new ClonePart(file1.getAbsolutePath(), 3, 4, 12);
    ClonePart part2 = new ClonePart(file2.getAbsolutePath(), 9, 25, 33);
    assertThat(cloneParts, hasItem(part1));
    assertThat(cloneParts, hasItem(part2));
  }

  @Ignore("not implemented")
  @Test
  public void test2() {
    register(getBlocks(file1));
    register(getBlocks(file2));
    List<Block> fileBlocks = getBlocks(file3);
    List<CloneGroup> clones = OriginalCloneDetectionAlgorithm.detect(cloneIndex, fileBlocks);
    assertThat(clones.size(), is(3));
    for (CloneGroup clone : clones) {
      System.out.println(clone);
    }
  }

  private void register(List<Block> blocks) {
    for (Block block : blocks) {
      cloneIndex.insert(block);
    }
  }

  private List<Block> getBlocks(File file) {
    TokenQueue tokenQueue = tokenChunker.chunk(file);
    List<Statement> statements = statementChunker.chunk(tokenQueue);
    return blockChunker.chunk(file.getAbsolutePath(), statements);
  }

}
