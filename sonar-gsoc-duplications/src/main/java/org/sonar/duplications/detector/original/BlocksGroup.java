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
package org.sonar.duplications.detector.original;

import java.util.Comparator;
import java.util.List;

import org.sonar.duplications.block.Block;

import com.google.common.collect.Lists;

/**
 * Set of {@link Block}s, which internally stored as a sorted list.
 */
class BlocksGroup {

  /**
   * Factory method.
   * 
   * @return new empty group
   */
  public static BlocksGroup empty() {
    return new BlocksGroup();
  }

  protected final List<Block> blocks;

  protected BlocksGroup() {
    this.blocks = Lists.newArrayList();
  }

  public int size() {
    return blocks.size();
  }

  /**
   * @return true, if this group subsumed by specified group
   * @see #subsumedBy(BlocksGroup, BlocksGroup, int)
   */
  public boolean subsumedBy(BlocksGroup other) {
    if (other.size() == 0) {
      return false;
    }
    int indexCorrection = this.blocks.get(0).getIndexInFile() - other.blocks.get(0).getIndexInFile();
    return subsumedBy(this, other, indexCorrection);
  }

  /**
   * @return intersection of this group with specified
   * @see #intersect(BlocksGroup, BlocksGroup)
   */
  public BlocksGroup intersect(BlocksGroup other) {
    return intersect(this, other);
  }

  public List<Block[]> pairs(BlocksGroup other, int len) {
    return pairs(this, other, len);
  }

  @Override
  public String toString() {
    return blocks.toString();
  }

  protected BlocksGroup intersect(BlocksGroup group1, BlocksGroup group2) {
    BlocksGroup intersection = new BlocksGroup();
    List<Block> list1 = group1.blocks;
    List<Block> list2 = group2.blocks;
    int i = 0;
    int j = 0;
    while (i < list1.size() && j < list2.size()) {
      Block block1 = list1.get(i);
      Block block2 = list2.get(j);
      int c = block1.getResourceId().compareTo(block2.getResourceId());
      if (c > 0) {
        j++;
        continue;
      }
      if (c < 0) {
        i++;
        continue;
      }
      if (c == 0) {
        c = block1.getIndexInFile() + 1 - block2.getIndexInFile();
      }
      if (c == 0) { // list1[i] == list2[j]
        i++;
        j++;
        intersection.blocks.add(block2);
      }
      if (c > 0) { // list1[i] > list2[j]
        j++;
      }
      if (c < 0) { // list1[i] < list2[j]
        i++;
      }
    }
    return intersection;
  }

  /**
   * One group is subsumed by another group, when each block from first group has corresponding block from second group with corrected index.
   */
  protected boolean subsumedBy(BlocksGroup group1, BlocksGroup group2, int indexCorrection) {
    List<Block> list1 = group1.blocks;
    List<Block> list2 = group2.blocks;
    int i = 0;
    int j = 0;
    while (i < list1.size() && j < list2.size()) {
      Block block1 = list1.get(i);
      Block block2 = list2.get(j);
      int c = block1.getResourceId().compareTo(block2.getResourceId());
      if (c != 0) {
        j++;
        continue;
      }
      if (c == 0) {
        c = block1.getIndexInFile() - indexCorrection - block2.getIndexInFile();
      }
      if (c < 0) { // list1[i] < list2[j]
        break;
      }
      if (c != 0) { // list1[i] != list2[j]
        j++;
      }
      if (c == 0) { // list1[i] == list2[j]
        i++;
        j++;
      }
    }
    return i == list1.size();
  }

  protected List<Block[]> pairs(BlocksGroup beginGroup, BlocksGroup endGroup, int len) {
    List<Block[]> result = Lists.newArrayList();
    List<Block> beginBlocks = beginGroup.blocks;
    List<Block> endBlocks = endGroup.blocks;
    int i = 0;
    int j = 0;
    while (i < beginBlocks.size() && j < endBlocks.size()) {
      Block beginBlock = beginBlocks.get(i);
      Block endBlock = endBlocks.get(j);
      int c = beginBlock.getResourceId().compareTo(endBlock.getResourceId());
      if (c == 0) {
        c = beginBlock.getIndexInFile() + len - 1 - endBlock.getIndexInFile();
      }
      if (c == 0) {
        result.add(new Block[] { beginBlock, endBlock });
        i++;
        j++;
      }
      if (c > 0) {
        j++;
      }
      if (c < 0) {
        i++;
      }
    }
    return result;
  }

  /**
   * Compares {@link Block}s first using {@link Block#getResourceId() resource id} and then using {@link Block#getIndexInFile() index in file}.
   */
  public static class BlockComparator implements Comparator<Block> {

    public static final BlockComparator INSTANCE = new BlockComparator();

    public int compare(Block b1, Block b2) {
      int c = b1.getResourceId().compareTo(b2.getResourceId());
      if (c == 0) {
        return b1.getIndexInFile() - b2.getIndexInFile();
      }
      return c;
    }

  }

}
