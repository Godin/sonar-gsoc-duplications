package org.sonar.duplications.index;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.ByteArray;

public class PackedMemoryCloneIndexTest {

  private PackedMemoryCloneIndex index;

  @Before
  public void setUp() {
    index = new PackedMemoryCloneIndex();
  }

  @Test
  public void test() {
    index.insert(newBlock("a", 1));
    index.insert(newBlock("a", 2));
    index.insert(newBlock("b", 1));
    index.insert(newBlock("c", 1));
    index.insert(newBlock("d", 1));
    index.insert(newBlock("e", 1));
    index.insert(newBlock("e", 2));
    index.insert(newBlock("e", 3));

    assertThat(index.getBySequenceHash(new ByteArray(1L)).size(), is(5));
    assertThat(index.getBySequenceHash(new ByteArray(2L)).size(), is(2));
    assertThat(index.getByResourceId("a").size(), is(2));
    assertThat(index.getByResourceId("b").size(), is(1));
    assertThat(index.getByResourceId("e").size(), is(3));
  }

  private static Block newBlock(String resourceId, long hash) {
    return new Block(resourceId, new ByteArray(hash), 1, 1, 1);
  }

}
