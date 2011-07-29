package org.sonar.duplications.index;

import org.junit.Before;
import org.junit.Test;
import org.sonar.duplications.block.Block;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CloneReporterTest {

  private CloneIndex indexBackend;

  @Before
  public void initialize() {
    indexBackend = new MemoryCloneIndex();
  }

  @Test
  public void testSimple() {
    for (int i = 0; i < 9; i++) {
      indexBackend.insert(new Block("a", "" + i, i, i, i + 5));
    }

    indexBackend.insert(new Block("b", "3", 2, 2, 7));
    indexBackend.insert(new Block("b", "4", 3, 3, 8));
    indexBackend.insert(new Block("b", "5", 4, 4, 9));
    indexBackend.insert(new Block("b", "6", 5, 5, 10));

    indexBackend.insert(new Block("c", "5", 1, 1, 6));
    indexBackend.insert(new Block("c", "6", 2, 2, 7));
    indexBackend.insert(new Block("c", "7", 3, 3, 8));
    List<Block> blocks = new ArrayList<Block>(indexBackend.getByResourceId("a"));
    List<Clone> items = CloneReporter.reportClones(blocks, indexBackend);
    assertThat(items.size(), is(2));

    Clone expected1 = new Clone(4)
        .addPart(new ClonePart("a", 3, 3, 11))
        .addPart(new ClonePart("b", 2, 2, 10));
    expected1.setOriginPart(new ClonePart("a", 3, 3, 11));

    Clone expected2 = new Clone(3)
        .addPart(new ClonePart("a", 5, 5, 12))
        .addPart(new ClonePart("c", 1, 1, 8));
    expected2.setOriginPart(new ClonePart("a", 5, 5, 12));

    assertThat(items, hasItem(expected1));
    assertThat(items, hasItem(expected2));
  }

  @Test
  public void testSameClones() {
    indexBackend.insert(new Block("a", "0", 0, 0, 5));
    indexBackend.insert(new Block("a", "1", 1, 1, 6));
    indexBackend.insert(new Block("a", "2", 2, 2, 7));
    indexBackend.insert(new Block("a", "3", 3, 3, 8));
    indexBackend.insert(new Block("a", "4", 4, 4, 9));

    indexBackend.insert(new Block("b", "1", 1, 1, 6));
    indexBackend.insert(new Block("b", "2", 2, 2, 7));
    indexBackend.insert(new Block("b", "3", 3, 3, 8));

    indexBackend.insert(new Block("c", "1", 1, 1, 6));
    indexBackend.insert(new Block("c", "2", 2, 2, 7));
    indexBackend.insert(new Block("c", "3", 3, 3, 8));

    List<Block> blocks = new ArrayList<Block>(indexBackend.getByResourceId("a"));
    List<Clone> items = CloneReporter.reportClones(blocks, indexBackend);
    assertThat(items.size(), is(1));
    Clone expected = new Clone(3)
        .addPart(new ClonePart("a", 1, 1, 8))
        .addPart(new ClonePart("b", 1, 1, 8))
        .addPart(new ClonePart("c", 1, 1, 8));
    expected.setOriginPart(new ClonePart("a", 1, 1, 8));
    assertThat(items, hasItem(expected));
  }

  @Test
  public void testBegin() {
    indexBackend.insert(new Block("a", "0", 0, 0, 5));
    indexBackend.insert(new Block("a", "1", 1, 1, 6));
    indexBackend.insert(new Block("a", "2", 2, 2, 7));

    indexBackend.insert(new Block("b", "0", 0, 0, 5));
    indexBackend.insert(new Block("b", "1", 1, 1, 6));

    List<Block> blocks = new ArrayList<Block>(indexBackend.getByResourceId("a"));
    List<Clone> items = CloneReporter.reportClones(blocks, indexBackend);
    assertThat(items.size(), is(1));

    ClonePart part1 = new ClonePart("a", 0, 0, 6);
    ClonePart part2 = new ClonePart("b", 0, 0, 6);
    Clone expected = new Clone(2)
        .addPart(part1)
        .addPart(part2);
    expected.setOriginPart(part1);
    assertThat(items, hasItem(expected));
  }

  @Test
  public void testEnd() {
    indexBackend.insert(new Block("a", "0", 0, 0, 5));
    indexBackend.insert(new Block("a", "1", 1, 1, 6));
    indexBackend.insert(new Block("a", "2", 2, 2, 7));

    indexBackend.insert(new Block("b", "1", 1, 1, 6));
    indexBackend.insert(new Block("b", "2", 2, 2, 7));

    List<Block> blocks = new ArrayList<Block>(indexBackend.getByResourceId("a"));
    List<Clone> items = CloneReporter.reportClones(blocks, indexBackend);
    assertThat(items.size(), is(1));
    ClonePart part1 = new ClonePart("a", 1, 1, 7);
    ClonePart part2 = new ClonePart("b", 1, 1, 7);
    Clone expected = new Clone(2)
        .addPart(part1)
        .addPart(part2);
    expected.setOriginPart(part1);
    assertThat(items, hasItem(expected));
  }

  @Test
  public void testDuplicatesSameFile1() {
    indexBackend.insert(new Block("a", "0", 0, 0, 5));
    indexBackend.insert(new Block("a", "1", 1, 1, 6));
    indexBackend.insert(new Block("a", "2", 2, 2, 7));

    indexBackend.insert(new Block("a", "3", 3, 3, 8));
    indexBackend.insert(new Block("a", "1", 4, 4, 9));
    indexBackend.insert(new Block("a", "4", 5, 5, 10));

    List<Block> blocks = new ArrayList<Block>(indexBackend.getByResourceId("a"));
    List<Clone> items = CloneReporter.reportClones(blocks, indexBackend);
    assertThat(items.size(), is(1));

    ClonePart part1 = new ClonePart("a", 1, 1, 6);
    ClonePart part2 = new ClonePart("a", 4, 4, 9);
    Clone expected = new Clone(1)
        .addPart(part1)
        .addPart(part2);
    expected.setOriginPart(part1);
    assertThat(items, hasItem(expected));
  }

  @Test
  public void testDuplicatesSameFile2() {
    indexBackend.insert(new Block("a", "0", 0, 0, 5));
    indexBackend.insert(new Block("a", "1", 1, 1, 6));
    indexBackend.insert(new Block("a", "2", 2, 2, 7));

    indexBackend.insert(new Block("a", "3", 3, 3, 8));
    indexBackend.insert(new Block("a", "4", 4, 4, 9));
    indexBackend.insert(new Block("a", "0", 5, 5, 10));

    List<Block> blocks = new ArrayList<Block>(indexBackend.getByResourceId("a"));
    List<Clone> items = CloneReporter.reportClones(blocks, indexBackend);
    assertThat(items.size(), is(1));
    ClonePart part1 = new ClonePart("a", 0, 0, 5);
    ClonePart part2 = new ClonePart("a", 5, 5, 10);
    Clone expected = new Clone(1)
        .addPart(part1)
        .addPart(part2);
    expected.setOriginPart(part1);
    assertThat(items, hasItem(expected));
  }

  @Test
  public void testDuplicatesSameFileTriangle() {
    indexBackend.insert(new Block("a", "0", 0, 0, 5));
    indexBackend.insert(new Block("a", "1", 1, 1, 6));
    indexBackend.insert(new Block("a", "2", 2, 2, 7));

    indexBackend.insert(new Block("a", "3", 3, 3, 8));
    indexBackend.insert(new Block("a", "1", 4, 4, 9));
    indexBackend.insert(new Block("a", "4", 5, 5, 10));

    indexBackend.insert(new Block("a", "5", 6, 6, 11));
    indexBackend.insert(new Block("a", "1", 7, 7, 12));
    indexBackend.insert(new Block("a", "6", 8, 8, 13));

    List<Block> blocks = new ArrayList<Block>(indexBackend.getByResourceId("a"));
    List<Clone> items = CloneReporter.reportClones(blocks, indexBackend);
    assertThat(items.size(), is(1));
    Clone expected = new Clone(1)
        .addPart(new ClonePart("a", 1, 1, 6))
        .addPart(new ClonePart("a", 4, 4, 9))
        .addPart(new ClonePart("a", 7, 7, 12));
    expected.setOriginPart(new ClonePart("a", 1, 1, 6));
    assertThat(items, hasItem(expected));
  }
}
