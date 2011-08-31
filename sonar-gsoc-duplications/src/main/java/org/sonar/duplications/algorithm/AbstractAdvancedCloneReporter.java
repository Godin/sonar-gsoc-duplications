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
package org.sonar.duplications.algorithm;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.FileBlockGroup;
import org.sonar.duplications.index.CloneGroup;
import org.sonar.duplications.index.CloneIndex;
import org.sonar.duplications.index.ClonePair;
import org.sonar.duplications.index.ClonePart;

import java.util.*;

public abstract class AbstractAdvancedCloneReporter implements CloneReporterAlgorithm {

  protected static final Comparator<ClonePair> CLONEPAIR_COMPARATOR = new Comparator<ClonePair>() {
    public int compare(ClonePair o1, ClonePair o2) {
      return o1.getOriginPart().getUnitStart() - o2.getOriginPart().getUnitStart();
    }
  };

  public static final String ALGORITHM_KEY = "algorithm";
  public static final String INIT_KEY = "init";

  protected CloneIndex cloneIndex;
  protected StatsCollector statsCollector;

  public void printStatistics() {
    statsCollector.printAllStatistics();
  }

  public void resetStatistics() {
    statsCollector.reset();
  }

  /**
   * TODO Godin: performs several queries using same hash, which is inefficient in terms of performance
   */
  protected List<List<Block>> getIndexedBlockGroups(FileBlockGroup fileBlockGroup) {
    List<List<Block>> result = Lists.newArrayList();

    for (Block block : fileBlockGroup.getBlockList()) {
      List<Block> sameHashBlockGroup = Lists.newArrayList();
      Collection<Block> foundBlocks = cloneIndex.getBySequenceHash(block.getBlockHash());
      statsCollector.addNumber("total found blocks", foundBlocks.size());
      for (Block shBlock : foundBlocks) {
        if (!shBlock.getResourceId().equals(block.getResourceId()) ||
            shBlock.getIndexInFile() > block.getIndexInFile()) {

          sameHashBlockGroup.add(shBlock);
        }
      }
      result.add(sameHashBlockGroup);
    }

    return result;
  }

  protected List<ClonePair> reportClonePairs(FileBlockGroup fileBlockGroup) {
    statsCollector.startTime(INIT_KEY);
    SortedSet<Block> resourceBlocks = fileBlockGroup.getBlockList();
    List<List<Block>> sameHashBlockGroups = getIndexedBlockGroups(fileBlockGroup);
    //an empty list is needed a the end to report clone at the end of file
    sameHashBlockGroups.add(new ArrayList<Block>());
    Map<String, Map<CloneKey, ClonePair>> prevActiveChains = Maps.newLinkedHashMap();
    List<ClonePair> reportedPairs = Lists.newArrayList();
    statsCollector.stopTime(INIT_KEY);

    statsCollector.startTime(ALGORITHM_KEY);
    Iterator<Block> blockIterator = resourceBlocks.iterator();
    for (List<Block> blockGroup : sameHashBlockGroups) {
      Map<String, Map<CloneKey, ClonePair>> nextActiveChains = Maps.newLinkedHashMap();

      Block origBlock = null;
      if (blockIterator.hasNext()) {
        origBlock = blockIterator.next();
      }
      for (Block block : blockGroup) {
        String otherResourceId = block.getResourceId();
        Map<CloneKey, ClonePair> nextActiveMap = nextActiveChains.get(otherResourceId);
        if (nextActiveMap == null) {
          nextActiveMap = Maps.newTreeMap();
          nextActiveChains.put(otherResourceId, nextActiveMap);
        }
        Map<CloneKey, ClonePair> prevActiveMap = prevActiveChains.get(otherResourceId);
        if (prevActiveMap == null) {
          prevActiveMap = Maps.newTreeMap();
          prevActiveChains.put(otherResourceId, prevActiveMap);
        }

        processBlock(prevActiveMap, nextActiveMap, origBlock, block);
      }
      for (Map<CloneKey, ClonePair> prevActiveMap : prevActiveChains.values()) {
        reportedPairs.addAll(prevActiveMap.values());
      }
      prevActiveChains = nextActiveChains;
    }
    statsCollector.stopTime(ALGORITHM_KEY);

    return reportedPairs;
  }

  /**
   * processes curren block - checks if current block continues one of block sequences
   * or creates new block sequence. sequences (<tt>ClonePair</tt>) are put to
   * <tt>nextActiveMap</tt>
   *
   * @param prevActiveMap, map with active block sequences from previous cycle iteration
   * @param nextActiveMap, map with active block sequences after current cycle iteration
   * @param originBlock,   ClonePart of block of original file
   * @param otherBlock,    ClonePart of one of blocks with same hash as <tt>originBlock</tt>
   */
  protected void processBlock(Map<CloneKey, ClonePair> prevActiveMap,
                              Map<CloneKey, ClonePair> nextActiveMap,
                              Block originBlock, Block otherBlock) {
    ClonePair clonePair;

    String resourceId = otherBlock.getResourceId();
    int unitStart = otherBlock.getIndexInFile();

    CloneKey curKey = new CloneKey(resourceId, unitStart);
    CloneKey nextKey = new CloneKey(resourceId, unitStart + 1);

    ClonePair prevPair = prevActiveMap.remove(curKey);
    if (prevPair == null) {
      clonePair = new ClonePair(new ClonePart(originBlock), new ClonePart(otherBlock), 1);
    } else {
      clonePair = prevPair;
      clonePair.getOriginPart().setLineEnd(originBlock.getLastLineNumber());
      clonePair.getAnotherPart().setLineEnd(otherBlock.getLastLineNumber());
      clonePair.setCloneUnitLength(clonePair.getCloneUnitLength() + 1);
    }

    nextActiveMap.put(nextKey, clonePair);
  }

  /**
   * @param clones, array of TempClone sorted by getOriginPart().getUnitStart()
   * @return list of reported clones
   */
  protected List<CloneGroup> groupClonePairs(List<ClonePair> clones) {
    List<CloneGroup> res = Lists.newArrayList();
    //sort elements of prevActiveMap by getOriginPart.getUnitStart()
    Collections.sort(clones, CLONEPAIR_COMPARATOR);

    CloneGroup curClone = null;
    int prevUnitStart = -1;
    int prevLength = -1;
    for (ClonePair clonePair : clones) {
      int curUnitStart = clonePair.getOriginPart().getUnitStart();
      int curLength = clonePair.getCloneUnitLength();
      //if current sequence matches with different sequence in original file
      if (curUnitStart != prevUnitStart || prevLength != curLength) {
        prevLength = curLength;

        curClone = new CloneGroup()
            .setCloneUnitLength(clonePair.getCloneUnitLength())
            .setOriginPart(clonePair.getOriginPart())
            .addPart(clonePair.getOriginPart())
            .addPart(clonePair.getAnotherPart());

        res.add(curClone);
      } else {
        curClone.addPart(clonePair.getAnotherPart());
      }
      prevUnitStart = curUnitStart;
    }
    return res;
  }

}
