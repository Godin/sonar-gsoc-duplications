package org.sonar.duplications.algorithm;

import org.junit.Before;
import org.junit.Test;
import org.sonar.duplications.api.codeunit.Block;
import org.sonar.duplications.api.index.CloneIndexBackend;
import org.sonar.duplications.backend.MemoryIndexBackend;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CloneReporterTest {

  private CloneIndexBackend indexBackend;

  @Before
  public void initialize() {
    indexBackend = new MemoryIndexBackend();
  }

  @Test
  public void testSimple() {
    for (int i = 0; i < 9; i++) {
      indexBackend.insert(new Block("a", new byte[]{(byte) i}, i, i, i + 5));
    }

    indexBackend.insert(new Block("b", new byte[]{3}, 2, 2, 7));
    indexBackend.insert(new Block("b", new byte[]{4}, 3, 3, 8));
    indexBackend.insert(new Block("b", new byte[]{5}, 4, 4, 9));
    indexBackend.insert(new Block("b", new byte[]{6}, 5, 5, 10));

    indexBackend.insert(new Block("c", new byte[]{5}, 1, 1, 6));
    indexBackend.insert(new Block("c", new byte[]{6}, 2, 2, 7));
    indexBackend.insert(new Block("c", new byte[]{7}, 3, 3, 8));

    List<Clone> items = CloneReporter.reportClones("a", indexBackend);
    assertThat(items.size(), is(2));
    assertThat(items, hasItem(new Clone("a", 3, 3, 11, "b", 2, 2, 10, 4)));
    assertThat(items, hasItem(new Clone("a", 5, 5, 12, "c", 1, 1, 8, 3)));
  }

  @Test
  public void testSameClones() {
    indexBackend.insert(new Block("a", new byte[]{0}, 0, 0, 5));
    indexBackend.insert(new Block("a", new byte[]{1}, 1, 1, 6));
    indexBackend.insert(new Block("a", new byte[]{2}, 2, 2, 7));
    indexBackend.insert(new Block("a", new byte[]{3}, 3, 3, 8));
    indexBackend.insert(new Block("a", new byte[]{4}, 4, 4, 9));

    indexBackend.insert(new Block("b", new byte[]{1}, 1, 1, 6));
    indexBackend.insert(new Block("b", new byte[]{2}, 2, 2, 7));
    indexBackend.insert(new Block("b", new byte[]{3}, 3, 3, 8));

    indexBackend.insert(new Block("c", new byte[]{1}, 1, 1, 6));
    indexBackend.insert(new Block("c", new byte[]{2}, 2, 2, 7));
    indexBackend.insert(new Block("c", new byte[]{3}, 3, 3, 8));

    List<Clone> items = CloneReporter.reportClones("a", indexBackend);
    assertThat(items.size(), is(2));
    assertThat(items, hasItem(new Clone("a", 1, 1, 8, "b", 1, 1, 8, 3)));
    assertThat(items, hasItem(new Clone("a", 1, 1, 8, "c", 1, 1, 8, 3)));
  }

  @Test
  public void testBegin() {
    indexBackend.insert(new Block("a", new byte[]{0}, 0, 0, 5));
    indexBackend.insert(new Block("a", new byte[]{1}, 1, 1, 6));
    indexBackend.insert(new Block("a", new byte[]{2}, 2, 2, 7));

    indexBackend.insert(new Block("b", new byte[]{0}, 0, 0, 5));
    indexBackend.insert(new Block("b", new byte[]{1}, 1, 1, 6));

    List<Clone> items = CloneReporter.reportClones("a", indexBackend);
    assertThat(items.size(), is(1));
    assertThat(items, hasItem(new Clone("a", 0, 0, 6, "b", 0, 0, 6, 2)));
  }

  @Test
  public void testEnd() {
    indexBackend.insert(new Block("a", new byte[]{0}, 0, 0, 5));
    indexBackend.insert(new Block("a", new byte[]{1}, 1, 1, 6));
    indexBackend.insert(new Block("a", new byte[]{2}, 2, 2, 7));

    indexBackend.insert(new Block("b", new byte[]{1}, 1, 1, 6));
    indexBackend.insert(new Block("b", new byte[]{2}, 2, 2, 7));

    List<Clone> items = CloneReporter.reportClones("a", indexBackend);
    assertThat(items.size(), is(1));
    assertThat(items, hasItem(new Clone("a", 1, 1, 7, "b", 1, 1, 7, 2)));
  }
}
