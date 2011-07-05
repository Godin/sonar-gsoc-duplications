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

    public void setOrigPart(ClonePart origPart) {
      this.origPart = origPart;
    }

    public ClonePart getAnotherPart() {
      return anotherPart;
    }

    public void setAnotherPart(ClonePart anotherPart) {
      this.anotherPart = anotherPart;
    }

    public int getCloneLength() {
      return cloneLength;
    }

    public void setCloneLength(int cloneLength) {
      this.cloneLength = cloneLength;
    }
  }


  public static List<Clone> reportClones(String filename, CloneIndex index) {
    SortedSet<Block> resourceSet = index.getByResourceId(filename);
    ArrayList<Block> resourceBlocks = new ArrayList<Block>();

    ArrayList<Clone> groupedClones = new ArrayList<Clone>();

    ArrayList<Collection<Block>> foundBlockClones = new ArrayList<Collection<Block>>();

    for (Block block : resourceSet) {
      List<Block> filteredList = new ArrayList<Block>();

      for (Block foundBlock : index.getBySequenceHash(block.getBlockHash())) {
        //we process Block's clones from same file only if
        // clone.getIndexInFile > original.getIndexInFile
        if (!foundBlock.getResourceId().equals(block.getResourceId()) ||
            foundBlock.getIndexInFile() > block.getIndexInFile()) {
          filteredList.add(foundBlock);
        }
      }
      foundBlockClones.add(filteredList);
      resourceBlocks.add(block);
    }
    foundBlockClones.add(new ArrayList<Block>());

    TreeMap<Key, TempClone> prevActiveMap = new TreeMap<Key, TempClone>();

    for (int i = 0; i < foundBlockClones.size(); i++) {
      TreeMap<Key, TempClone> nextActiveMap = new TreeMap<Key, TempClone>();

      for (Block block : foundBlockClones.get(i)) {
        Block origBlock = resourceBlocks.get(i);
        processBlock(prevActiveMap, nextActiveMap, origBlock, block);
      }

      for (TempClone tempClone : prevActiveMap.values()) {
        Clone clone = new Clone(tempClone.getOrigPart(), tempClone.getAnotherPart(), tempClone.getCloneLength());
        groupedClones.add(clone);
      }

      prevActiveMap = nextActiveMap;
    }

    return groupedClones;
  }

  private static void processBlock(TreeMap<Key, TempClone> prevActiveMap, TreeMap<Key, TempClone> nextActiveMap,
                                   Block origBlock, Block block) {
    ClonePart origPart = new ClonePart(origBlock);
    ClonePart anotherPart = new ClonePart(block);
    int cloneLength = 0;

    Key curKey = new Key(block.getResourceId(), block.getIndexInFile());
    if (prevActiveMap.containsKey(curKey)) {
      TempClone prevPart = prevActiveMap.get(curKey);

      origPart.setLineStart(prevPart.getOrigPart().getLineStart());
      origPart.setUnitStart(prevPart.getOrigPart().getUnitStart());

      anotherPart.setLineStart(prevPart.getAnotherPart().getLineStart());
      anotherPart.setUnitStart(prevPart.getAnotherPart().getUnitStart());

      cloneLength = prevPart.getCloneLength();

      prevActiveMap.remove(curKey);
    }

    TempClone tempClone = new TempClone(origPart, anotherPart, cloneLength + 1);

    Key nextKey = new Key(block.getResourceId(), block.getIndexInFile() + 1);
    nextActiveMap.put(nextKey, tempClone);
  }
}
