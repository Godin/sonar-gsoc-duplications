package org.sonar.duplications.index;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sonar.duplications.block.Block;
import org.sonar.duplications.index.Clone;
import org.sonar.duplications.index.CloneIndex;
import org.sonar.duplications.index.CloneReporter;

public class CloneReporterTest {

  private CloneIndex indexBackend;

  @Before
  public void initialize() {
    indexBackend = new MemoryCloneIndex();
  }

  @Test
  public void testSimple() {
    for (int i = 0; i < 9; i++) {
      indexBackend.insert(new Block("a", ""+i, i, i, i + 5));
    }

    indexBackend.insert(new Block("b", "3", 2, 2, 7));
    indexBackend.insert(new Block("b", "4", 3, 3, 8));
    indexBackend.insert(new Block("b", "5", 4, 4, 9));
    indexBackend.insert(new Block("b", "6", 5, 5, 10));

    indexBackend.insert(new Block("c", "5", 1, 1, 6));
    indexBackend.insert(new Block("c", "6", 2, 2, 7));
    indexBackend.insert(new Block("c", "7", 3, 3, 8));

    List<Clone> items = CloneReporter.reportClones("a", indexBackend);
    assertThat(items.size(), is(2));
    assertThat(items, hasItem(new Clone("a", 3, 3, 11, "b", 2, 2, 10, 4)));
    assertThat(items, hasItem(new Clone("a", 5, 5, 12, "c", 1, 1, 8, 3)));
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

    List<Clone> items = CloneReporter.reportClones("a", indexBackend);
    assertThat(items.size(), is(2));
    assertThat(items, hasItem(new Clone("a", 1, 1, 8, "b", 1, 1, 8, 3)));
    assertThat(items, hasItem(new Clone("a", 1, 1, 8, "c", 1, 1, 8, 3)));
  }

  @Test
  public void testBegin() {
    indexBackend.insert(new Block("a", "0", 0, 0, 5));
    indexBackend.insert(new Block("a", "1", 1, 1, 6));
    indexBackend.insert(new Block("a", "2", 2, 2, 7));

    indexBackend.insert(new Block("b", "0", 0, 0, 5));
    indexBackend.insert(new Block("b", "1", 1, 1, 6));

    List<Clone> items = CloneReporter.reportClones("a", indexBackend);
    assertThat(items.size(), is(1));
    assertThat(items, hasItem(new Clone("a", 0, 0, 6, "b", 0, 0, 6, 2)));
  }

  @Test
  public void testEnd() {
    indexBackend.insert(new Block("a", "0", 0, 0, 5));
    indexBackend.insert(new Block("a", "1", 1, 1, 6));
    indexBackend.insert(new Block("a", "2", 2, 2, 7));

    indexBackend.insert(new Block("b", "1", 1, 1, 6));
    indexBackend.insert(new Block("b", "2", 2, 2, 7));

    List<Clone> items = CloneReporter.reportClones("a", indexBackend);
    assertThat(items.size(), is(1));
    assertThat(items, hasItem(new Clone("a", 1, 1, 7, "b", 1, 1, 7, 2)));
  }

  @Test
  public void testDuplicatesSameFile1() {
    indexBackend.insert(new Block("a", "0", 0, 0, 5));
    indexBackend.insert(new Block("a", "1", 1, 1, 6));
    indexBackend.insert(new Block("a", "2", 2, 2, 7));

    indexBackend.insert(new Block("a", "3", 3, 3, 8));
    indexBackend.insert(new Block("a", "1", 4, 4, 9));
    indexBackend.insert(new Block("a", "4", 5, 5, 10));

    List<Clone> items = CloneReporter.reportClones("a", indexBackend);
    assertThat(items.size(), is(1));
    assertThat(items, hasItem(new Clone("a", 1, 1, 6, "a", 4, 4, 9, 1)));
  }

  @Test
  public void testDuplicatesSameFile2() {
    indexBackend.insert(new Block("a", "0", 0, 0, 5));
    indexBackend.insert(new Block("a", "1", 1, 1, 6));
    indexBackend.insert(new Block("a", "2", 2, 2, 7));

    indexBackend.insert(new Block("a", "3", 3, 3, 8));
    indexBackend.insert(new Block("a", "4", 4, 4, 9));
    indexBackend.insert(new Block("a", "0", 5, 5, 10));

    List<Clone> items = CloneReporter.reportClones("a", indexBackend);
    assertThat(items.size(), is(1));
    assertThat(items, hasItem(new Clone("a", 5, 5, 10, "a", 0, 0, 5, 1)));
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

    List<Clone> items = CloneReporter.reportClones("a", indexBackend);
    assertThat(items.size(), is(3));
    assertThat(items, hasItem(new Clone("a", 1, 1, 6, "a", 4, 4, 9, 1)));
    assertThat(items, hasItem(new Clone("a", 1, 1, 6, "a", 7, 7, 12, 1)));
    assertThat(items, hasItem(new Clone("a", 7, 7, 12, "a", 4, 4, 9, 1)));
  }
}
