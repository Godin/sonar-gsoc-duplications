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
package org.sonar.duplications.index;

import org.sonar.duplications.block.Block;

import java.util.*;

public class CloneReporter {

  private static class Key implements Comparable<Key> {
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
  }

  private static class TempClone {
    private ClonePart origPart;
    private ClonePart anotherPart;
    private int cloneLength;

    private TempClone(ClonePart origPart, ClonePart anotherPart, int cloneLength) {
      this.origPart = origPart;
      this.anotherPart = anotherPart;
      this.cloneLength = cloneLength;
    }

    public ClonePart getOrigPart() {
      return origPart;
    }

    public ClonePart getAnotherPart() {
      return anotherPart;
    }

    public int getCloneLength() {
      return cloneLength;
    }

  }

  private static class TempCloneComparator implements Comparator<TempClone> {

    public int compare(TempClone o1, TempClone o2) {
      return o1.getOrigPart().getUnitStart() - o2.getOrigPart().getUnitStart();
    }
  }

  public static List<Clone> reportClones(List<Block> candidateBlocks, CloneIndex index) {
    ArrayList<Clone> clones = new ArrayList<Clone>();

    ArrayList<Block> resourceBlocks = new ArrayList<Block>(candidateBlocks);

    ArrayList<List<Block>> sameHashBlockGroups = new ArrayList<List<Block>>();

    for (Block block : candidateBlocks) {
      List<Block> sameHashBlockGroup = new ArrayList<Block>();
      for (Block shBlock : index.getBySequenceHash(block.getBlockHash())) {
        if (!shBlock.getResourceId().equals(block.getResourceId()) ||
            shBlock.getIndexInFile() > block.getIndexInFile()) {
          sameHashBlockGroup.add(shBlock);
        }
      }
      sameHashBlockGroups.add(sameHashBlockGroup);
    }

    //an empty list is needed a the end to report clone at the end of file
    sameHashBlockGroups.add(new ArrayList<Block>());

    TreeMap<Key, TempClone> prevActiveMap = new TreeMap<Key, TempClone>();

    for (int i = 0; i < sameHashBlockGroups.size(); i++) {
      TreeMap<Key, TempClone> nextActiveMap = new TreeMap<Key, TempClone>();

      for (Block block : sameHashBlockGroups.get(i)) {
        Block origBlock = resourceBlocks.get(i);
        processBlock(prevActiveMap, nextActiveMap, origBlock, block);
      }

      //sort elements of prevActiveMap by getOrigPart.getUnitStart()
      ArrayList<TempClone> sortedArr = new ArrayList<TempClone>(prevActiveMap.values());

      Collections.sort(sortedArr, new TempCloneComparator());

      clones.addAll(reportClones(sortedArr));

      prevActiveMap = nextActiveMap;
    }

    return removeDuplicates(clones);
  }

  /**
   * Checks if first Clone is contained in second Clone. Clone A is contained in another
   * Clone B if every ClonePart pA from A has ClonePart pB in B which satisfy the conditions
   * pA.resourceId == pB.resourceId and pA.unitStart >= pB.unitStart and pA.unitEnd <= pb.unitEnd
   *
   * @param first  Clone to check contains
   * @param second Clone where to check contains
   * @return
   */
  private static boolean containsIn(Clone first, Clone second) {
    if (!first.getOriginPart().getResourceId().equals(second.getOriginPart().getResourceId())) {
      return false;
    }
    for (int i = 0; i < first.getCloneParts().size(); i++) {
      ClonePart firstPart = first.getCloneParts().get(i);
      int firstUnitEnd = firstPart.getUnitStart() + first.getCloneLength();
      boolean found = false;

      for (int j = 0; j < second.getCloneParts().size(); j++) {
        ClonePart secondPart = second.getCloneParts().get(j);
        int secondUnitEnd = secondPart.getUnitStart() + second.getCloneLength();
        if (firstPart.getResourceId().equals(secondPart.getResourceId()) &&
            firstPart.getUnitStart() >= secondPart.getUnitStart() &&
            firstUnitEnd <= secondUnitEnd) {
          found = true;
          break;
        }
      }
      if (!found) {
        return false;
      }
    }
    return true;
  }

  private static List<Clone> removeDuplicates(List<Clone> clones) {
    HashSet<Clone> set = new HashSet<Clone>(clones);
    List<Clone> result = new ArrayList<Clone>(set);

    //O(n^2) filter for clones fully covered by another clones
    List<Clone> filtered = new ArrayList<Clone>();
    for (int i = 0; i < result.size(); i++) {
      Clone first = result.get(i);
      boolean covered = false;
      for (int j = 0; j < result.size(); j++) {
        if (i == j) {
          continue;
        }
        Clone second = result.get(j);
        covered |= containsIn(first, second);
        if (covered)
          break;
      }
      if (!covered) {
        filtered.add(first);
      }
    }

    return filtered;
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
  private static void processBlock(TreeMap<Key, TempClone> prevActiveMap,
                                   TreeMap<Key, TempClone> nextActiveMap,
                                   Block origBlock, Block anotherBlock) {
    ClonePart origPart = new ClonePart(origBlock);
    ClonePart anotherPart = new ClonePart(anotherBlock);
    int cloneLength = 0;

    Key curKey = new Key(anotherBlock.getResourceId(), anotherBlock.getIndexInFile());
    if (prevActiveMap.containsKey(curKey)) {
      TempClone prevTmp = prevActiveMap.get(curKey);

      ClonePart prevOrigPart = prevTmp.getOrigPart();
      origPart.setLineStart(prevOrigPart.getLineStart());
      origPart.setUnitStart(prevOrigPart.getUnitStart());

      ClonePart prevAnotherPart = prevTmp.getAnotherPart();
      anotherPart.setLineStart(prevAnotherPart.getLineStart());
      anotherPart.setUnitStart(prevAnotherPart.getUnitStart());

      cloneLength = prevTmp.getCloneLength();

      prevActiveMap.remove(curKey);
    }

    TempClone tempClone = new TempClone(origPart, anotherPart, cloneLength + 1);

    Key nextKey = new Key(anotherBlock.getResourceId(), anotherBlock.getIndexInFile() + 1);
    nextActiveMap.put(nextKey, tempClone);
  }


  /**
   * @param sortedArr, array of TempClone sorted by getOrigPart().getUnitStart()
   * @return list of reported clones
   */
  private static List<Clone> reportClones(List<TempClone> sortedArr) {
    List<Clone> res = new ArrayList<Clone>();
    Clone curClone = null;
    int prevUnitStart = -1;
    for (int j = 0; j < sortedArr.size(); j++) {
      TempClone tempClone = sortedArr.get(j);
      int curUnitStart = tempClone.getOrigPart().getUnitStart();
      //if current sequence matches with different sequence in original file
      if (curUnitStart != prevUnitStart) {
        curClone = new Clone(tempClone.getCloneLength());
        curClone.setOriginPart(tempClone.getOrigPart());
        curClone.addPart(tempClone.getOrigPart());
        curClone.addPart(tempClone.getAnotherPart());
        res.add(curClone);
      } else {
        curClone.addPart(tempClone.getAnotherPart());
      }
      prevUnitStart = curUnitStart;
    }
    return res;
  }

}
