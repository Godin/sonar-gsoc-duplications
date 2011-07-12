package org.sonar.duplications.index;

import org.junit.Before;
import org.junit.Test;
import org.sonar.duplications.block.Block;

import java.util.Collection;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class MemoryIndexBackendTest {

  private CloneIndex memBack;

  @Before
  public void initialize() {
    memBack = new MemoryCloneIndex();
  }

  @Test
  public void testUniqueResources() {
    memBack.insert(new Block("a", "0", 0, 0, 10));
    memBack.insert(new Block("b", "1", 0, 0, 10));
    memBack.insert(new Block("c", "2", 0, 0, 10));
    assertThat(memBack.getAllUniqueResourceId().size(), is(3));
  }

  @Test
  public void testContainsResource() {
    memBack.insert(new Block("a", "0", 0, 0, 10));
    memBack.insert(new Block("b", "1", 0, 0, 10));
    memBack.insert(new Block("c", "2", 0, 0, 10));
    assertThat(memBack.containsResourceId("a"), is(true));
    assertThat(memBack.containsResourceId("b"), is(true));
    assertThat(memBack.containsResourceId("c"), is(true));
    assertThat(memBack.containsResourceId("d"), is(false));
  }

  @Test
  public void testClearAll() {
    assertThat(memBack.size(), is(0));
    for (int i = 0; i < 10; i++) {
      memBack.insert(new Block("a", "0", i, 0, 10));
    }
    assertThat(memBack.size(), is(10));

    memBack.removeAll();
    assertThat(memBack.size(), is(0));
  }

  @Test
  public void byFileName() {
    Block tuple1 = new Block("a", "0", 0, 0, 10);
    Block tuple2 = new Block("a", "0", 1, 10, 20);

    assertThat(memBack.getByResourceId("a").size(), is(0));

    memBack.insert(tuple1);
    assertThat(memBack.getByResourceId("a").size(), is(1));

    memBack.insert(tuple2);
    assertThat(memBack.getByResourceId("a").size(), is(2));
  }

  @Test
  public void bySequenceHash() {
    Block tuple1 = new Block("a", "0", 0, 0, 5);
    Block tuple2 = new Block("a", "0", 1, 1, 6);

    assertThat(memBack.getBySequenceHash("0").size(), is(0));

    memBack.insert(tuple1);
    assertThat(memBack.getBySequenceHash("0").size(), is(1));

    memBack.insert(tuple2);
    assertThat(memBack.getBySequenceHash("0").size(), is(2));
  }

  @Test
  public void insertSame() {
    Block tuple = new Block("a", "0", 0, 0, 5);
    Block tupleSame = new Block("a", "0", 0, 0, 5);

    assertThat(memBack.getByResourceId("a").size(), is(0));
    assertThat(memBack.getBySequenceHash("0").size(), is(0));

    memBack.insert(tuple);
    assertThat(memBack.getByResourceId("a").size(), is(1));
    assertThat(memBack.getBySequenceHash("0").size(), is(1));

    memBack.insert(tupleSame);
    assertThat(memBack.getByResourceId("a").size(), is(1));
    assertThat(memBack.getBySequenceHash("0").size(), is(1));
  }

  @Test
  public void testSorted() {
    for (int i = 0; i < 10; i++) {
      memBack.insert(new Block("a", "1", 10 - i, i, i + 5));
    }
    assertThat(memBack.getByResourceId("a").size(), is(10));
    assertThat(memBack.getBySequenceHash("1").size(), is(10));

    Collection<Block> set = memBack.getByResourceId("a");
    int prevStatementIndex = 0;
    for (Block tuple : set) {
      assertTrue(tuple.getIndexInFile() > prevStatementIndex);
      prevStatementIndex = tuple.getIndexInFile();
    }
  }
}
