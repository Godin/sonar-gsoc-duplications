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
    assertThat(index.getBySequenceHash(new ByteArray(3L)).size(), is(1));
    assertThat(index.getBySequenceHash(new ByteArray(4L)).size(), is(0));
    assertThat(index.getByResourceId("a").size(), is(2));
    assertThat(index.getByResourceId("b").size(), is(1));
    assertThat(index.getByResourceId("e").size(), is(3));
    assertThat(index.getByResourceId("does not exist").size(), is(0));
  }

  /**
   * Given: index with initial capacity 1.
   * Expected: size and capacity should be increased after insertion of two blocks.
   */
  @Test
  public void testEnsureCapacity() {
    CloneIndex index = new PackedMemoryCloneIndex(8, 1);
    index.insert(newBlock("a", 1));
    index.insert(newBlock("a", 2));
    assertThat(index.getByResourceId("a").size(), is(2));
  }

  /**
   * Given: index, which accepts blocks with 4-byte hash.
   * Expected: exception during insertion of block with 8-byte hash.
   */
  @Test(expected = IllegalArgumentException.class)
  public void attempt_to_insert_hash_of_incorrect_size() {
    CloneIndex index = new PackedMemoryCloneIndex(4, 1);
    index.insert(newBlock("a", 1));
  }

  /**
   * Given: index, which accepts blocks with 4-byte hash.
   * Expected: exception during search by 8-byte hash.
   */
  @Test(expected = IllegalArgumentException.class)
  public void attempt_to_find_hash_of_incorrect_size() {
    CloneIndex index = new PackedMemoryCloneIndex(4, 1);
    index.getBySequenceHash(new ByteArray(1L));
  }

  private static Block newBlock(String resourceId, long hash) {
    return new Block(resourceId, new ByteArray(hash), 1, 1, 1);
  }

}
