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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.sonar.duplications.BaseCloneReporterTest;
import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.ByteArray;
import org.sonar.duplications.block.FileBlockGroup;
import org.sonar.duplications.index.CloneGroup;
import org.sonar.duplications.index.CloneIndex;
import org.sonar.duplications.index.ClonePart;
import org.sonar.duplications.index.MemoryCloneIndex;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AdvancedCloneReporterTest extends BaseCloneReporterTest {

  private CloneIndex cloneIndex;
  private CloneReporterAlgorithm cloneReporter;

  public AdvancedCloneReporterTest(CloneReporterAlgorithmBuilder builder) {
    super(builder);
  }

  @Before
  public void initialize() {
    cloneIndex = new MemoryCloneIndex();
    cloneReporter = cloneReporterBuilder.build(cloneIndex);
  }

  @Test
  public void testSimple() {
    for (int i = 0; i < 9; i++) {
      cloneIndex.insert(new Block("a", new ByteArray(i), i, i, i + 5));
    }

    cloneIndex.insert(new Block("b", new ByteArray(3), 2, 2, 7));
    cloneIndex.insert(new Block("b", new ByteArray(4), 3, 3, 8));
    cloneIndex.insert(new Block("b", new ByteArray(5), 4, 4, 9));
    cloneIndex.insert(new Block("b", new ByteArray(6), 5, 5, 10));

    cloneIndex.insert(new Block("c", new ByteArray(5), 1, 1, 6));
    cloneIndex.insert(new Block("c", new ByteArray(6), 2, 2, 7));
    cloneIndex.insert(new Block("c", new ByteArray(7), 3, 3, 8));
    List<Block> blocks = new ArrayList<Block>(cloneIndex.getByResourceId("a"));
    FileBlockGroup blockGroup = FileBlockGroup.create("a", blocks);
    List<CloneGroup> items = cloneReporter.reportClones(blockGroup);
    assertThat(items.size(), is(2));

    CloneGroup expected1 = new CloneGroup(4)
        .addPart(new ClonePart("a", 3, 3, 11))
        .addPart(new ClonePart("b", 2, 2, 10));
    expected1.setOriginPart(new ClonePart("a", 3, 3, 11));

    CloneGroup expected2 = new CloneGroup(3)
        .addPart(new ClonePart("a", 5, 5, 12))
        .addPart(new ClonePart("c", 1, 1, 8));
    expected2.setOriginPart(new ClonePart("a", 5, 5, 12));

    assertThat(items, hasItem(expected1));
    assertThat(items, hasItem(expected2));
  }

  @Test
  public void testSameClones() {
    cloneIndex.insert(new Block("a", new ByteArray(0), 0, 0, 5));
    cloneIndex.insert(new Block("a", new ByteArray(1), 1, 1, 6));
    cloneIndex.insert(new Block("a", new ByteArray(2), 2, 2, 7));
    cloneIndex.insert(new Block("a", new ByteArray(3), 3, 3, 8));
    cloneIndex.insert(new Block("a", new ByteArray(4), 4, 4, 9));

    cloneIndex.insert(new Block("b", new ByteArray(1), 1, 1, 6));
    cloneIndex.insert(new Block("b", new ByteArray(2), 2, 2, 7));
    cloneIndex.insert(new Block("b", new ByteArray(3), 3, 3, 8));

    cloneIndex.insert(new Block("c", new ByteArray(1), 1, 1, 6));
    cloneIndex.insert(new Block("c", new ByteArray(2), 2, 2, 7));
    cloneIndex.insert(new Block("c", new ByteArray(3), 3, 3, 8));

    List<Block> blocks = new ArrayList<Block>(cloneIndex.getByResourceId("a"));
    FileBlockGroup blockGroup = FileBlockGroup.create("a", blocks);
    List<CloneGroup> items = cloneReporter.reportClones(blockGroup);
    assertThat(items.size(), is(1));
    CloneGroup expected = new CloneGroup(3)
        .addPart(new ClonePart("a", 1, 1, 8))
        .addPart(new ClonePart("b", 1, 1, 8))
        .addPart(new ClonePart("c", 1, 1, 8));
    expected.setOriginPart(new ClonePart("a", 1, 1, 8));
    assertThat(items, hasItem(expected));
  }

  @Test
  public void testBegin() {
    cloneIndex.insert(new Block("a", new ByteArray(0), 0, 0, 5));
    cloneIndex.insert(new Block("a", new ByteArray(1), 1, 1, 6));
    cloneIndex.insert(new Block("a", new ByteArray(2), 2, 2, 7));

    cloneIndex.insert(new Block("b", new ByteArray(0), 0, 0, 5));
    cloneIndex.insert(new Block("b", new ByteArray(1), 1, 1, 6));

    List<Block> blocks = new ArrayList<Block>(cloneIndex.getByResourceId("a"));
    FileBlockGroup blockGroup = FileBlockGroup.create("a", blocks);
    List<CloneGroup> items = cloneReporter.reportClones(blockGroup);
    assertThat(items.size(), is(1));

    ClonePart part1 = new ClonePart("a", 0, 0, 6);
    ClonePart part2 = new ClonePart("b", 0, 0, 6);
    CloneGroup expected = new CloneGroup(2)
        .addPart(part1)
        .addPart(part2);
    expected.setOriginPart(part1);
    assertThat(items, hasItem(expected));
  }

  @Test
  public void testEnd() {
    cloneIndex.insert(new Block("a", new ByteArray(0), 0, 0, 5));
    cloneIndex.insert(new Block("a", new ByteArray(1), 1, 1, 6));
    cloneIndex.insert(new Block("a", new ByteArray(2), 2, 2, 7));

    cloneIndex.insert(new Block("b", new ByteArray(1), 1, 1, 6));
    cloneIndex.insert(new Block("b", new ByteArray(2), 2, 2, 7));

    List<Block> blocks = new ArrayList<Block>(cloneIndex.getByResourceId("a"));
    FileBlockGroup blockGroup = FileBlockGroup.create("a", blocks);
    List<CloneGroup> items = cloneReporter.reportClones(blockGroup);
    assertThat(items.size(), is(1));
    ClonePart part1 = new ClonePart("a", 1, 1, 7);
    ClonePart part2 = new ClonePart("b", 1, 1, 7);
    CloneGroup expected = new CloneGroup(2)
        .addPart(part1)
        .addPart(part2);
    expected.setOriginPart(part1);
    assertThat(items, hasItem(expected));
  }

  @Test
  public void testDuplicatesSameFile1() {
    cloneIndex.insert(new Block("a", new ByteArray(0), 0, 0, 5));
    cloneIndex.insert(new Block("a", new ByteArray(1), 1, 1, 6));
    cloneIndex.insert(new Block("a", new ByteArray(2), 2, 2, 7));

    cloneIndex.insert(new Block("a", new ByteArray(3), 3, 3, 8));
    cloneIndex.insert(new Block("a", new ByteArray(1), 4, 4, 9));
    cloneIndex.insert(new Block("a", new ByteArray(4), 5, 5, 10));

    List<Block> blocks = new ArrayList<Block>(cloneIndex.getByResourceId("a"));
    FileBlockGroup blockGroup = FileBlockGroup.create("a", blocks);
    List<CloneGroup> items = cloneReporter.reportClones(blockGroup);
    assertThat(items.size(), is(1));

    ClonePart part1 = new ClonePart("a", 1, 1, 6);
    ClonePart part2 = new ClonePart("a", 4, 4, 9);
    CloneGroup expected = new CloneGroup(1)
        .addPart(part1)
        .addPart(part2);
    expected.setOriginPart(part1);
    assertThat(items, hasItem(expected));
  }

  @Test
  public void testDuplicatesSameFile2() {
    cloneIndex.insert(new Block("a", new ByteArray(0), 0, 0, 5));
    cloneIndex.insert(new Block("a", new ByteArray(1), 1, 1, 6));
    cloneIndex.insert(new Block("a", new ByteArray(2), 2, 2, 7));

    cloneIndex.insert(new Block("a", new ByteArray(3), 3, 3, 8));
    cloneIndex.insert(new Block("a", new ByteArray(4), 4, 4, 9));
    cloneIndex.insert(new Block("a", new ByteArray(0), 5, 5, 10));

    List<Block> blocks = new ArrayList<Block>(cloneIndex.getByResourceId("a"));
    FileBlockGroup blockGroup = FileBlockGroup.create("a", blocks);
    List<CloneGroup> items = cloneReporter.reportClones(blockGroup);
    assertThat(items.size(), is(1));
    ClonePart part1 = new ClonePart("a", 0, 0, 5);
    ClonePart part2 = new ClonePart("a", 5, 5, 10);
    CloneGroup expected = new CloneGroup(1)
        .addPart(part1)
        .addPart(part2);
    expected.setOriginPart(part1);
    assertThat(items, hasItem(expected));
  }

  @Ignore("TODO fix situation with duplicated clone with Paired and Grouped variants of algorithm")
  @Test
  public void testDuplicatesSameFileTriangle() {
    cloneIndex.insert(new Block("a", new ByteArray(0), 0, 0, 5));
    cloneIndex.insert(new Block("a", new ByteArray(1), 1, 1, 6));
    cloneIndex.insert(new Block("a", new ByteArray(2), 2, 2, 7));

    cloneIndex.insert(new Block("a", new ByteArray(3), 3, 3, 8));
    cloneIndex.insert(new Block("a", new ByteArray(1), 4, 4, 9));
    cloneIndex.insert(new Block("a", new ByteArray(4), 5, 5, 10));

    cloneIndex.insert(new Block("a", new ByteArray(5), 6, 6, 11));
    cloneIndex.insert(new Block("a", new ByteArray(1), 7, 7, 12));
    cloneIndex.insert(new Block("a", new ByteArray(6), 8, 8, 13));

    List<Block> blocks = new ArrayList<Block>(cloneIndex.getByResourceId("a"));
    FileBlockGroup blockGroup = FileBlockGroup.create("a", blocks);
    List<CloneGroup> items = cloneReporter.reportClones(blockGroup);

    assertThat(items.size(), is(1));
    CloneGroup expected = new CloneGroup(1)
        .addPart(new ClonePart("a", 1, 1, 6))
        .addPart(new ClonePart("a", 4, 4, 9))
        .addPart(new ClonePart("a", 7, 7, 12));
    expected.setOriginPart(new ClonePart("a", 1, 1, 6));
    assertThat(items, hasItem(expected));
  }
}
