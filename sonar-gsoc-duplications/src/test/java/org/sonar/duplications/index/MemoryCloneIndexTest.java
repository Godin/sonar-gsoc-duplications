package org.sonar.duplications.index;

import org.junit.Before;
import org.junit.Test;
import org.sonar.duplications.block.Block;

import java.util.Collection;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class MemoryCloneIndexTest {

  private CloneIndex cloneIndex;

  @Before
  public void initialize() {
    cloneIndex = new MemoryCloneIndex();
  }

  @Test
  public void testUniqueResources() {
    cloneIndex.insert(new Block("a", "0", 0, 0, 10));
    cloneIndex.insert(new Block("b", "1", 0, 0, 10));
    cloneIndex.insert(new Block("c", "2", 0, 0, 10));
    assertThat(cloneIndex.getAllUniqueResourceId().size(), is(3));
  }

  @Test
  public void testContainsResource() {
    cloneIndex.insert(new Block("a", "0", 0, 0, 10));
    cloneIndex.insert(new Block("b", "1", 0, 0, 10));
    cloneIndex.insert(new Block("c", "2", 0, 0, 10));
    assertThat(cloneIndex.containsResourceId("a"), is(true));
    assertThat(cloneIndex.containsResourceId("b"), is(true));
    assertThat(cloneIndex.containsResourceId("c"), is(true));
    assertThat(cloneIndex.containsResourceId("d"), is(false));
  }

  @Test
  public void testClearAll() {
    assertThat(cloneIndex.size(), is(0));
    for (int i = 0; i < 10; i++) {
      cloneIndex.insert(new Block("a", "0", i, 0, 10));
    }
    assertThat(cloneIndex.size(), is(10));

    cloneIndex.removeAll();
    assertThat(cloneIndex.size(), is(0));
  }

  @Test
  public void byFileName() {
    Block tuple1 = new Block("a", "0", 0, 0, 10);
    Block tuple2 = new Block("a", "0", 1, 10, 20);

    assertThat(cloneIndex.getByResourceId("a").size(), is(0));

    cloneIndex.insert(tuple1);
    assertThat(cloneIndex.getByResourceId("a").size(), is(1));

    cloneIndex.insert(tuple2);
    assertThat(cloneIndex.getByResourceId("a").size(), is(2));
  }

  @Test
  public void bySequenceHash() {
    Block tuple1 = new Block("a", "0", 0, 0, 5);
    Block tuple2 = new Block("a", "0", 1, 1, 6);

    assertThat(cloneIndex.getBySequenceHash("0").size(), is(0));

    cloneIndex.insert(tuple1);
    assertThat(cloneIndex.getBySequenceHash("0").size(), is(1));

    cloneIndex.insert(tuple2);
    assertThat(cloneIndex.getBySequenceHash("0").size(), is(2));
  }

  @Test
  public void insertSame() {
    Block tuple = new Block("a", "0", 0, 0, 5);
    Block tupleSame = new Block("a", "0", 0, 0, 5);

    assertThat(cloneIndex.getByResourceId("a").size(), is(0));
    assertThat(cloneIndex.getBySequenceHash("0").size(), is(0));

    cloneIndex.insert(tuple);
    assertThat(cloneIndex.getByResourceId("a").size(), is(1));
    assertThat(cloneIndex.getBySequenceHash("0").size(), is(1));

    cloneIndex.insert(tupleSame);
    assertThat(cloneIndex.getByResourceId("a").size(), is(1));
    assertThat(cloneIndex.getBySequenceHash("0").size(), is(1));
  }

  @Test
  public void testSorted() {
    for (int i = 0; i < 10; i++) {
      cloneIndex.insert(new Block("a", "1", 10 - i, i, i + 5));
    }
    assertThat(cloneIndex.getByResourceId("a").size(), is(10));
    assertThat(cloneIndex.getBySequenceHash("1").size(), is(10));

    Collection<Block> set = cloneIndex.getByResourceId("a");
    int prevStatementIndex = 0;
    for (Block tuple : set) {
      assertTrue(tuple.getIndexInFile() > prevStatementIndex);
      prevStatementIndex = tuple.getIndexInFile();
    }
  }
}
