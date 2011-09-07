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
package org.sonar.duplications.index;

import java.util.Collection;
import java.util.List;

import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.ByteArray;
import org.sonar.duplications.utils.FastStringComparator;

import com.google.common.collect.Lists;

/**
 * Provides an index optimized by memory.
 * <p>
 * Each object in Java has an overhead - see
 * <a href="http://devblog.streamy.com/2009/07/24/determine-size-of-java-object-class/">"HOWTO: Determine the size of a Java Object or Class"</a>.
 * So to optimize memory consumption, we use flat arrays, however this increases time of queries.
 * During  usual detection of duplicates most time consuming method is a {@link #getByResourceId(String)}:
 * around 50% of time spent in this class and number of invocations of this method is 1% of total invocations,
 * however total time spent in this class less than 1 second for small projects and around 2 seconds for projects like JDK.
 * </p>
 * <p>
 * Note that this implementation currently does not support deletion, however it's possible to implement.
 * </p>
 */
public class PackedMemoryCloneIndex extends AbstractCloneIndex {

  private static final int DEFAULT_INITIAL_CAPACITY = 1024;

  private static final int BLOCK_INTS = 3;

  private final int hashInts;

  private final int blockInts;

  /**
   * Indicates that index requires sorting to perform queries.
   */
  private boolean sorted;

  /**
   * Current number of blocks in index.
   */
  private int size;

  private String[] resourceIds;
  private int[] blockData;

  private int[] resourceIdsIndex;

  public PackedMemoryCloneIndex() {
    this(8, DEFAULT_INITIAL_CAPACITY);
  }

  /**
   * @param hashBytes size of hash in bytes
   * @param initialCapacity the initial capacity
   */
  public PackedMemoryCloneIndex(int hashBytes, int initialCapacity) {
    this.sorted = false;
    this.hashInts = hashBytes / 4;
    this.blockInts = hashInts + BLOCK_INTS;
    this.size = 0;
    this.resourceIds = new String[initialCapacity];
    this.blockData = new int[initialCapacity * blockInts];
    this.resourceIdsIndex = new int[initialCapacity];
  }

  /**
   * {@inheritDoc}
   * <p>
   * <strong>Note that this implementation does not guarantee that blocks would be sorted by index.</strong>
   * </p>
   */
  public Collection<Block> getByResourceId(String resourceId) {
    ensureSorted();

    // prepare resourceId for binary search
    resourceIds[size] = resourceId;
    resourceIdsIndex[size] = size;

    int index = DataUtils.binarySearch(byResourceId);

    List<Block> result = Lists.newArrayList();
    int realIndex = resourceIdsIndex[index];
    while (index < size && FastStringComparator.INSTANCE.compare(resourceIds[realIndex], resourceId) == 0) {
      // extract block (note that there is no need to extract resourceId)
      int offset = realIndex * blockInts;
      int[] hash = new int[hashInts];
      for (int j = 0; j < hashInts; j++) {
        hash[j] = blockData[offset++];
      }
      int indexInFile = blockData[offset++];
      int firstLineNumber = blockData[offset++];
      int lastLineNumber = blockData[offset];

      result.add(new Block(resourceId, new ByteArray(hash), indexInFile, firstLineNumber, lastLineNumber));

      index++;
      realIndex = resourceIdsIndex[index];
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  public Collection<Block> getBySequenceHash(ByteArray sequenceHash) {
    ensureSorted();

    // prepare hash for binary search
    int[] hash = sequenceHash.toIntArray();
    if (hash.length != hashInts) {
      throw new IllegalArgumentException("Expected " + hashInts + " ints in hash, but got " + hash.length);
    }
    int offset = size * blockInts;
    for (int i = 0; i < hashInts; i++) {
      blockData[offset++] = hash[i];
    }

    int index = DataUtils.binarySearch(byBlockHash);

    List<Block> result = Lists.newArrayList();
    while (index < size && !isLessByHash(size, index)) {
      // extract block (note that there is no need to extract hash)
      String resourceId = resourceIds[index];
      offset = index * blockInts + hashInts;
      int indexInFile = blockData[offset++];
      int firstLineNumber = blockData[offset++];
      int lastLineNumber = blockData[offset];

      result.add(new Block(resourceId, sequenceHash, indexInFile, firstLineNumber, lastLineNumber));
      index++;
    }
    return result;
  }

  /**
   * {@inheritDoc}
   * <p>
   * <strong>Note that this implementation allows insertion of two blocks with same index for one resource.</strong>
   * </p>
   */
  public void insert(Block block) {
    sorted = false;
    ensureCapacity();

    resourceIds[size] = block.getResourceId();

    int[] hash = block.getBlockHash().toIntArray();
    if (hash.length != hashInts) {
      throw new IllegalArgumentException("Expected " + hashInts + " ints in hash, but got " + hash.length);
    }
    int offset = size * blockInts;
    for (int i = 0; i < hashInts; i++) {
      blockData[offset++] = hash[i];
    }
    blockData[offset++] = block.getIndexInFile();
    blockData[offset++] = block.getFirstLineNumber();
    blockData[offset] = block.getLastLineNumber();

    size++;
  }

  /**
   * Increases the capacity, if necessary.
   */
  private void ensureCapacity() {
    if (size < resourceIds.length) {
      return;
    }
    int newCapacity = (resourceIds.length * 3) / 2 + 1;
    // Increase size of resourceIds
    String[] oldResourceIds = resourceIds;
    resourceIds = new String[newCapacity];
    System.arraycopy(oldResourceIds, 0, resourceIds, 0, oldResourceIds.length);
    // Increase size of blockData
    int[] oldBlockData = blockData;
    blockData = new int[newCapacity * blockInts];
    System.arraycopy(oldBlockData, 0, blockData, 0, oldBlockData.length);
    // Increase size of byResourceIndices (no need to copy old, because would be restored in method ensureSorted)
    resourceIdsIndex = new int[newCapacity];
    sorted = false;
  }

  /**
   * Performs sorting, if necessary.
   */
  private void ensureSorted() {
    if (sorted) {
      return;
    }

    ensureCapacity();

    DataUtils.sort(byBlockHash);
    for (int i = 0; i < size; i++) {
      resourceIdsIndex[i] = i;
    }
    DataUtils.sort(byResourceId);

    sorted = true;
  }

  private boolean isLessByHash(int i, int j) {
    i *= blockInts;
    j *= blockInts;
    for (int k = 0; k < hashInts; k++, i++, j++) {
      if (blockData[i] < blockData[j]) {
        return true;
      }
      if (blockData[i] > blockData[j]) {
        return false;
      }
    }
    return false;
  }

  private final DataUtils.Sortable byBlockHash = new DataUtils.Sortable() {
    public void swap(int i, int j) {
      String tmp = resourceIds[i];
      resourceIds[i] = resourceIds[j];
      resourceIds[j] = tmp;

      i *= blockInts;
      j *= blockInts;
      for (int k = 0; k < blockInts; k++, i++, j++) {
        int x = blockData[i];
        blockData[i] = blockData[j];
        blockData[j] = x;
      }
    }

    public boolean isLess(int i, int j) {
      return isLessByHash(i, j);
    }

    public int size() {
      return size;
    }
  };

  private final DataUtils.Sortable byResourceId = new DataUtils.Sortable() {
    public void swap(int i, int j) {
      int tmp = resourceIdsIndex[i];
      resourceIdsIndex[i] = resourceIdsIndex[j];
      resourceIdsIndex[j] = tmp;
    }

    public boolean isLess(int i, int j) {
      String s1 = resourceIds[resourceIdsIndex[i]];
      String s2 = resourceIds[resourceIdsIndex[j]];
      return FastStringComparator.INSTANCE.compare(s1, s2) < 0;
    }

    public int size() {
      return size;
    }
  };

}
