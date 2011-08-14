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


import com.google.common.collect.Lists;
import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.FileBlockGroup;
import org.sonar.duplications.index.CloneIndex;
import org.sonar.duplications.index.ClonePair;
import org.sonar.duplications.index.ClonePart;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public abstract class AbstractAdvancedCloneReporter implements CloneReporterAlgorithm {

  protected static final Comparator<ClonePair> CLONEPAIR_COMPARATOR = new Comparator<ClonePair>() {
    public int compare(ClonePair o1, ClonePair o2) {
      return o1.getOriginPart().getUnitStart() - o2.getOriginPart().getUnitStart();
    }
  };

  protected CloneIndex cloneIndex;
  protected StatsCollector statsCollector;

  public void printStatistics() {
    statsCollector.printAllStatistics();
  }

  public void resetStatistics() {
    statsCollector.reset();
  }

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

  /**
   * processes curren block - checks if current block continues one of block sequences
   * or creates new block sequence. sequences (<tt>ClonePair</tt>) are put to
   * <tt>nextActiveMap</tt>
   *
   * @param prevActiveMap, map with active block sequences from previous cycle iteration
   * @param nextActiveMap, map with active block sequences after current cycle iteration
   * @param originBlock,   ClonePart of block of original file
   * @param anotherBlock,  ClonePart of one of blocks with same hash as <tt>originBlock</tt>
   */
  protected void processBlock(Map<CloneKey, ClonePair> prevActiveMap,
                              Map<CloneKey, ClonePair> nextActiveMap,
                              Block originBlock, Block anotherBlock) {
    ClonePair clonePair;

    String resourceId = anotherBlock.getResourceId();
    int unitStart = anotherBlock.getIndexInFile();

    CloneKey curKey = new CloneKey(resourceId, unitStart);
    CloneKey nextKey = new CloneKey(resourceId, unitStart + 1);

    ClonePair prevPair = prevActiveMap.remove(curKey);
    if (prevPair == null) {
      clonePair = new ClonePair(new ClonePart(originBlock), new ClonePart(anotherBlock), 1);
    } else {
      clonePair = prevPair;
      clonePair.getOriginPart().setLineEnd(originBlock.getLastLineNumber());
      clonePair.getAnotherPart().setLineEnd(anotherBlock.getLastLineNumber());
      clonePair.setCloneLength(clonePair.getCloneLength() + 1);
    }

    nextActiveMap.put(nextKey, clonePair);
  }

}
