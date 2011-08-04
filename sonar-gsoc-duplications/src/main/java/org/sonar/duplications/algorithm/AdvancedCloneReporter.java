/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2011 SonarSource
 * Written (W) 2011 Andrew Tereskin
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

import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.FileBlockGroup;
import org.sonar.duplications.index.CloneGroup;
import org.sonar.duplications.index.CloneIndex;
import org.sonar.duplications.index.ClonePair;
import org.sonar.duplications.index.ClonePart;

import java.util.*;

public class AdvancedCloneReporter implements CloneReporterAlgorithm {

  private final static class Key implements Comparable<Key> {

    private String resourceId;
    private int unitNum;

    private Key(String resourceId, int unitNum) {
      this.resourceId = resourceId;
      this.unitNum = unitNum;
    }

    public int compareTo(Key o) {
      if (this.resourceId.equals(o.resourceId)) {
        return this.unitNum - o.unitNum;
      }
      return this.resourceId.compareTo(o.resourceId);
    }

    @Override
    public boolean equals(Object object) {
      if (object instanceof Key) {
        Key other = (Key) object;

        if (other.resourceId.equals(resourceId) && other.unitNum == unitNum) {
          return true;
        }
      }
      return false;
    }

    @Override
    public int hashCode() {
      int h = 0;
      h = 31 * h + resourceId.hashCode();
      h = 31 * h + unitNum;
      return h;
    }

  }

  private final CloneIndex cloneIndex;

  public AdvancedCloneReporter(CloneIndex cloneIndex) {
    this.cloneIndex = cloneIndex;
  }

  public List<CloneGroup> reportClones(FileBlockGroup fileBlockGroup) {
    ArrayList<ClonePair> clones = new ArrayList<ClonePair>();

    List<Block> resourceBlocks = fileBlockGroup.getBlockList();

    ArrayList<List<Block>> sameHashBlockGroups = new ArrayList<List<Block>>();

    for (Block block : fileBlockGroup.getBlockList()) {
      List<Block> sameHashBlockGroup = new ArrayList<Block>();
      for (Block shBlock : cloneIndex.getBySequenceHash(block.getBlockHash())) {
        if (!shBlock.getResourceId().equals(block.getResourceId()) ||
            shBlock.getIndexInFile() > block.getIndexInFile()) {
          sameHashBlockGroup.add(shBlock);
        }
      }
      sameHashBlockGroups.add(sameHashBlockGroup);
    }

    //an empty list is needed a the end to report clone at the end of file
    sameHashBlockGroups.add(new ArrayList<Block>());

    Map<Key, ClonePair> prevActiveMap = new TreeMap<Key, ClonePair>();

    Iterator<Block> blockIterator = resourceBlocks.iterator();
    for (List<Block> blockGroup : sameHashBlockGroups) {
      Map<Key, ClonePair> nextActiveMap = new TreeMap<Key, ClonePair>();

      Block origBlock = null;
      if (blockIterator.hasNext()) {
        origBlock = blockIterator.next();
      }

      for (Block block : blockGroup) {
        processBlock(prevActiveMap, nextActiveMap, origBlock, block);
      }
      clones.addAll(prevActiveMap.values());

      prevActiveMap = nextActiveMap;
    }

    List<ClonePair> filtered = removeDuplicates(clones);
    filtered = new IntervalTreeCloneFilter().filter(filtered);
    List<CloneGroup> groups = groupClonePairs(filtered);

    return groups;
  }

  private List<ClonePair> removeDuplicates(List<ClonePair> clones) {
    HashSet<ClonePair> set = new HashSet<ClonePair>(clones);
    List<ClonePair> result = new ArrayList<ClonePair>(set);
    return result;
  }

  /**
   * processes curren block - checks if current block continues one of block sequences
   * or creates new block sequence. sequences (<tt>TempClone</tt>) are put to
   * <tt>nextActiveMap</tt>
   *
   * @param prevActiveMap, map with active block sequences from previous cycle iteration
   * @param nextActiveMap, map with active block sequences after current cycle iteration
   * @param origBlock,     block of original file
   * @param anotherBlock,  one of blocks with same hash as <tt>origBlock</tt>
   */
  private void processBlock(Map<Key, ClonePair> prevActiveMap,
                            Map<Key, ClonePair> nextActiveMap,
                            Block origBlock, Block anotherBlock) {
    ClonePart origPart = new ClonePart(origBlock);
    ClonePart anotherPart = new ClonePart(anotherBlock);
    int cloneLength = 0;

    Key curKey = new Key(anotherBlock.getResourceId(), anotherBlock.getIndexInFile());
    if (prevActiveMap.containsKey(curKey)) {
      ClonePair prevTmp = prevActiveMap.get(curKey);

      ClonePart prevOrigPart = prevTmp.getOriginPart();
      origPart.setLineStart(prevOrigPart.getLineStart());
      origPart.setUnitStart(prevOrigPart.getUnitStart());

      ClonePart prevAnotherPart = prevTmp.getAnotherPart();
      anotherPart.setLineStart(prevAnotherPart.getLineStart());
      anotherPart.setUnitStart(prevAnotherPart.getUnitStart());

      cloneLength = prevTmp.getCloneLength();

      prevActiveMap.remove(curKey);
    }

    ClonePair tempClone = new ClonePair(origPart, anotherPart, cloneLength + 1);

    Key nextKey = new Key(anotherBlock.getResourceId(), anotherBlock.getIndexInFile() + 1);
    nextActiveMap.put(nextKey, tempClone);
  }


  /**
   * @param clones, array of TempClone sorted by getOriginPart().getUnitStart()
   * @return list of reported clones
   */
  private List<CloneGroup> groupClonePairs(List<ClonePair> clones) {
    List<CloneGroup> res = new ArrayList<CloneGroup>();
    //sort elements of prevActiveMap by getOriginPart.getUnitStart()
    Comparator<ClonePair> comp = new Comparator<ClonePair>() {
      public int compare(ClonePair o1, ClonePair o2) {
        return o1.getOriginPart().getUnitStart() - o2.getOriginPart().getUnitStart();
      }
    };
    Collections.sort(clones, comp);

    CloneGroup curClone = null;
    int prevUnitStart = -1;
    int prevLength = -1;
    for (ClonePair clonePair : clones) {
      int curUnitStart = clonePair.getOriginPart().getUnitStart();
      int curLength = clonePair.getCloneLength();
      //if current sequence matches with different sequence in original file
      if (curUnitStart != prevUnitStart || prevLength != curLength) {
        prevLength = curLength;
        curClone = new CloneGroup(clonePair.getCloneLength());
        curClone.setOriginPart(clonePair.getOriginPart());
        curClone.addPart(clonePair.getOriginPart());
        curClone.addPart(clonePair.getAnotherPart());
        res.add(curClone);
      } else {
        curClone.addPart(clonePair.getAnotherPart());
      }
      prevUnitStart = curUnitStart;
    }
    return res;
  }

}
