/*
 * Sonar, open source software quality management tool.
 * Written (W) 2011 Andrew Tereskin
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.sonar.duplications.api.Block;
import org.sonar.duplications.api.index.CloneIndexBackend;

public class CloneReporter {

  /**
   * Use this comparator in TreeSet to intersect only by fileName
   */
  private static class ResourceIdBlockComparator implements Comparator<Block> {

    public int compare(Block o1, Block o2) {
      return o1.getResourceId().compareTo(o2.getResourceId());
    }

    public boolean equals(Object obj) {
      return obj instanceof ResourceIdBlockComparator;
    }
  }

  private static class BlockWrap implements Comparable<BlockWrap> {

    private final Block block;

    private boolean compareUnitMode;

    private BlockWrap(Block block) {
      this(block, false);
    }

    private BlockWrap(Block block, boolean compareUnitMode) {
      this.block = block;
      this.compareUnitMode = compareUnitMode;
    }

    public Block getBlock() {
      return block;
    }

    public int compareTo(BlockWrap o) {
      if ((compareUnitMode || o.compareUnitMode)
          && block.getResourceId().equals(o.block.getResourceId())) {
        return block.getIndexInFile() - o.block.getIndexInFile();
      }
      return block.getResourceId().compareTo(o.block.getResourceId());
    }
  }

  public static List<Clone> reportClones(String filename, CloneIndexBackend index) {
    SortedSet<Block> resourceSet = index.getByResourceId(filename);

    int totalSequences = resourceSet.size();
    ArrayList<Set<BlockWrap>> tuplesC = new ArrayList<Set<BlockWrap>>(totalSequences);
    ArrayList<Block> resourceBlocks = new ArrayList<Block>(totalSequences);

    ArrayList<Clone> clones = new ArrayList<Clone>();

    prepareSets(resourceSet, tuplesC, resourceBlocks, index);

    for (int i = 0; i < totalSequences; i++) {
      boolean containsInPrev = i > 0 && tuplesC.get(i - 1).containsAll(tuplesC.get(i));
      if (tuplesC.get(i).size() < 2 || containsInPrev) {
        continue;
      }

      Set<BlockWrap> current = createNewSet(tuplesC.get(i));
      for (int j = i + 1; j < totalSequences + 1; j++) {
        Set<BlockWrap> intersected = createNewSet(current);
        //do intersection
        intersected.retainAll(tuplesC.get(j));

        //if intersection size is smaller than original
        if (intersected.size() < current.size()) {
          //report clones from tuplesC[i] to current
          int cloneLength = j - i;
          Set<BlockWrap> beginSet = tuplesC.get(i);
          Set<BlockWrap> endSet = tuplesC.get(j - 1);
          Set<BlockWrap> prebeginSet = createNewSet(null);
          if (i > 0) {
            prebeginSet = tuplesC.get(i - 1);
          }
          reportClone(resourceBlocks.get(i), resourceBlocks.get(j - 1), beginSet, endSet,
              prebeginSet, intersected, cloneLength, clones);
        }

        current = intersected;
        boolean inPrev = i > 0 && tuplesC.get(i - 1).containsAll(current);
        if (current.size() < 2 || inPrev) {
          break;
        }
      }
    }
    return clones;
  }

  private static Set<BlockWrap> createNewSet(Set<BlockWrap> set) {
    Set<BlockWrap> treeSet = new TreeSet<BlockWrap>();
    if (set != null) {
      treeSet.addAll(set);
    }
    return treeSet;
  }

  private static void prepareSets(SortedSet<Block> fileSet, List<Set<BlockWrap>> tuplesC,
                                  List<Block> fileBlocks, CloneIndexBackend index) {
    for (Block block : fileSet) {
      Set<Block> set = index.getBySequenceHash(block.getBlockHash());
      Set<BlockWrap> wrapSet = new TreeSet<BlockWrap>();
      for (Block foundBlock : set) {
        boolean compSameFile = foundBlock.getResourceId().equals(block.getResourceId());
        compSameFile = compSameFile && !foundBlock.equals(block);
        compSameFile = compSameFile && block.getIndexInFile() < foundBlock.getIndexInFile();
        wrapSet.add(new BlockWrap(foundBlock, compSameFile));
      }
      fileBlocks.add(block);
      tuplesC.add(wrapSet);
    }
    //to fix last element bug
    tuplesC.add(createNewSet(null));
  }

  private static void reportClone(Block beginTuple, Block endTuple, Set<BlockWrap> beginSet, Set<BlockWrap> endSet,
                                  Set<BlockWrap> prebeginSet, Set<BlockWrap> intersected, int cloneLength, List<Clone> clones) {
    String firstFile = beginTuple.getResourceId();
    int firstUnitIndex = beginTuple.getIndexInFile();
    int firstLineStart = beginTuple.getFirstLineNumber();
    int firstLineEnd = endTuple.getLastLineNumber();
    TreeMap<BlockWrap, Block> map = new TreeMap<BlockWrap, Block>();
    for (BlockWrap blockWrap : endSet) {
      Block block = blockWrap.getBlock();
      map.put(blockWrap, block);
    }

    //cycle in filenames in clone start position
    for (BlockWrap secondStartBlockWrap : beginSet) {
      // &&
      Block secondStartBlock = secondStartBlockWrap.getBlock();
      boolean condition = !firstFile.equals(secondStartBlock.getResourceId());
      condition = condition || beginTuple.getIndexInFile() != secondStartBlock.getIndexInFile();
      condition = condition && !intersected.contains(secondStartBlockWrap);
      condition = condition && !prebeginSet.contains(secondStartBlockWrap);
      if (condition) {
        String secondFile = secondStartBlock.getResourceId();
        int secondUnitIndex = secondStartBlock.getIndexInFile();
        int secondLineStart = secondStartBlock.getFirstLineNumber();
        Block secondEndBlock = map.get(secondStartBlockWrap);
        int secondLineEnd = secondEndBlock.getLastLineNumber();

        ClonePart part1 = new ClonePart(firstFile, firstUnitIndex, firstLineStart, firstLineEnd);
        ClonePart part2 = new ClonePart(secondFile, secondUnitIndex, secondLineStart, secondLineEnd);

        Clone item = new Clone(part1, part2, cloneLength);
        clones.add(item);
      }
    }
  }
}
