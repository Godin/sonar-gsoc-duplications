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
package org.sonar.duplications.algorithm;

import com.google.common.collect.Lists;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.FileBlockGroup;
import org.sonar.duplications.index.CloneGroup;
import org.sonar.duplications.index.CloneIndex;
import org.sonar.duplications.index.ClonePart;
import org.sonar.duplications.index.MemoryCloneIndex;
import org.sonar.duplications.junit.TestNamePrinter;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CloneDetectionAlgorithmTest {

  @Rule
  public TestNamePrinter name = new TestNamePrinter();

  private static int LINES_PER_BLOCK = 5;

  /**
   * To simplify testing we assume that each block starts from a new line and contains {@link #LINES_PER_BLOCK} lines,
   * so we can simply use index and hash.
   */
  private static Block newBlock(String resourceId, String hash, int index) {
    return new Block(resourceId, hash, index, index, index + LINES_PER_BLOCK);
  }

  private static ClonePart newClonePart(String resourceId, int unitStart, int cloneUnitLength) {
    return new ClonePart(resourceId, unitStart, unitStart, unitStart + cloneUnitLength + LINES_PER_BLOCK - 1);
  }

  /**
   * <pre>
   * x: 1 2 3 4 5 6
   * y:   2 3 4 5
   * z:     3 4
   * </pre>
   */
  @Test
  public void exampleFromPaper() {
    CloneIndex cloneIndex = createIndex(
        blocksForResource("y").withHashes("2", "3", "4", "5"),
        blocksForResource("z").withHashes("3", "4"));
    List<Block> fileBlocks = blocksForResource("x").withHashes("1", "2", "3", "4", "5", "6");
    CloneReporterAlgorithm cloneReporter = new OriginalCloneReporter(cloneIndex);
    List<CloneGroup> clones = cloneReporter.reportClones(new FileBlockGroup("x", fileBlocks));
    print(clones);
    assertThat(clones.size(), is(3));

    CloneGroup clone1 = clones.get(0);
    assertThat(clone1.getCloneUnitLength(), is(4));
    assertThat(clone1.getCloneParts().size(), is(2));
    assertThat(clone1.getCloneParts(), hasItem(newClonePart("x", 1, 4)));
    assertThat(clone1.getCloneParts(), hasItem(newClonePart("y", 0, 4)));

    CloneGroup clone2 = clones.get(1);
    assertThat(clone2.getCloneUnitLength(), is(2));
    assertThat(clone2.getCloneParts().size(), is(3));
    assertThat(clone2.getCloneParts(), hasItem(newClonePart("x", 2, 2)));
    assertThat(clone2.getCloneParts(), hasItem(newClonePart("y", 1, 2)));
    assertThat(clone2.getCloneParts(), hasItem(newClonePart("z", 0, 2)));

    CloneGroup clone3 = clones.get(2);
    assertThat(clone3.getCloneUnitLength(), is(3));
    assertThat(clone3.getCloneParts().size(), is(2));
    assertThat(clone3.getCloneParts(), hasItem(newClonePart("x", 2, 3)));
    assertThat(clone3.getCloneParts(), hasItem(newClonePart("y", 1, 3)));
  }

  /**
   * <pre>
   * a: 1 2 3 4 5 6 7 8 9
   * b:     3 4 5 6
   * c:         5 6 7
   * </pre>
   */
  @Test
  public void example1() {
    CloneIndex cloneIndex = createIndex(
        blocksForResource("b").withHashes("3", "4", "5", "6"),
        blocksForResource("c").withHashes("5", "6", "7"));
    List<Block> fileBlocks =
        blocksForResource("a").withHashes("1", "2", "3", "4", "5", "6", "7", "8", "9");
    CloneReporterAlgorithm cloneReporter = new OriginalCloneReporter(cloneIndex);
    List<CloneGroup> clones = cloneReporter.reportClones(new FileBlockGroup("a", fileBlocks));
    print(clones);
    assertThat(clones.size(), is(3));

    CloneGroup clone1 = clones.get(0);
    assertThat(clone1.getCloneUnitLength(), is(4));
    assertThat(clone1.getCloneParts().size(), is(2));
    assertThat(clone1.getCloneParts(), hasItem(newClonePart("a", 2, 4)));
    assertThat(clone1.getCloneParts(), hasItem(newClonePart("b", 0, 4)));

    CloneGroup clone2 = clones.get(1);
    assertThat(clone2.getCloneUnitLength(), is(2));
    assertThat(clone2.getCloneParts().size(), is(3));
    assertThat(clone2.getCloneParts(), hasItem(newClonePart("a", 4, 2)));
    assertThat(clone2.getCloneParts(), hasItem(newClonePart("b", 2, 2)));
    assertThat(clone2.getCloneParts(), hasItem(newClonePart("c", 0, 2)));

    CloneGroup clone3 = clones.get(2);
    assertThat(clone3.getCloneUnitLength(), is(3));
    assertThat(clone3.getCloneParts().size(), is(2));
    assertThat(clone3.getCloneParts(), hasItem(newClonePart("a", 4, 3)));
    assertThat(clone3.getCloneParts(), hasItem(newClonePart("c", 0, 3)));
  }

  @Test
  public void example2() {
    CloneIndex cloneIndex = createIndex(
        blocksForResource("b").withHashes("1", "2", "3", "4", "1", "2", "3", "4", "1", "2", "3", "4"),
        blocksForResource("c").withHashes("1", "2", "3", "4"));
    List<Block> fileBlocks =
        blocksForResource("a").withHashes("1", "2", "3", "5");
    CloneReporterAlgorithm cloneReporter = new OriginalCloneReporter(cloneIndex);
    List<CloneGroup> clones = cloneReporter.reportClones(new FileBlockGroup("a", fileBlocks));
    print(clones);
    assertThat(clones.size(), is(1));

    CloneGroup clone1 = clones.get(0);
    assertThat(clone1.getCloneUnitLength(), is(3));
    assertThat(clone1.getCloneParts().size(), is(5));
    assertThat(clone1.getCloneParts(), hasItem(newClonePart("a", 0, 3)));
    assertThat(clone1.getCloneParts(), hasItem(newClonePart("b", 0, 3)));
    assertThat(clone1.getCloneParts(), hasItem(newClonePart("b", 4, 3)));
    assertThat(clone1.getCloneParts(), hasItem(newClonePart("b", 8, 3)));
    assertThat(clone1.getCloneParts(), hasItem(newClonePart("c", 0, 3)));
  }

  @Test
  public void problemWithEndOfFile() {
    CloneIndex cloneIndex = createIndex(
        blocksForResource("b").withHashes("1", "2", "3", "4"));
    List<Block> fileBlocks =
        blocksForResource("a").withHashes("1", "2", "3", "4");
    CloneReporterAlgorithm cloneReporter = new OriginalCloneReporter(cloneIndex);
    List<CloneGroup> clones = cloneReporter.reportClones(new FileBlockGroup("a", fileBlocks));
    print(clones);
    assertThat(clones.size(), is(1));

    CloneGroup clone1 = clones.get(0);
    assertThat(clone1.getCloneUnitLength(), is(4));
    assertThat(clone1.getCloneParts().size(), is(2));
    assertThat(clone1.getCloneParts(), hasItem(newClonePart("a", 0, 4)));
    assertThat(clone1.getCloneParts(), hasItem(newClonePart("b", 0, 4)));
  }

  /**
   * FIXME fix and implement test
   */
  @Ignore("not implemented")
  @Test
  public void problemWithNestedCloneGroups() {
    CloneIndex cloneIndex = createIndex(
        blocksForResource("b").withHashes("1", "2", "1", "2", "1", "2", "1"));
    List<Block> fileBlocks =
        blocksForResource("a").withHashes("1", "2", "1", "2", "1", "2");
    CloneReporterAlgorithm cloneReporter = new OriginalCloneReporter(cloneIndex);
    List<CloneGroup> clones = cloneReporter.reportClones(new FileBlockGroup("a", fileBlocks));
    print(clones);
  }

  private void print(List<CloneGroup> clones) {
    for (CloneGroup clone : clones) {
      System.out.println(clone);
    }
    System.out.println();
  }

  private static CloneIndex createIndex(List<Block>... blocks) {
    CloneIndex cloneIndex = new MemoryCloneIndex();
    for (List<Block> b : blocks) {
      for (Block block : b) {
        cloneIndex.insert(block);
      }
    }
    return cloneIndex;
  }

  private static BlocksBuilder blocksForResource(String resourceId) {
    return new BlocksBuilder(resourceId);
  }

  private static class BlocksBuilder {
    String resourceId;

    public BlocksBuilder(String resourceId) {
      this.resourceId = resourceId;
    }

    List<Block> withHashes(String... hashes) {
      List<Block> result = Lists.newArrayList();
      int index = 0;
      for (String hash : hashes) {
        result.add(newBlock(resourceId, hash, index));
        index++;
      }
      return result;
    }
  }

}
