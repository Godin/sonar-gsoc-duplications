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
import com.google.common.collect.Maps;
import org.sonar.duplications.algorithm.filter.CloneFilter;
import org.sonar.duplications.algorithm.filter.IntervalTreeCloneFilter;
import org.sonar.duplications.algorithm.filter.IntervalTreeClonePairFilter;
import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.FileBlockGroup;
import org.sonar.duplications.index.CloneGroup;
import org.sonar.duplications.index.CloneIndex;
import org.sonar.duplications.index.ClonePair;

import java.util.*;

public class AdvancedPairCloneReporter extends AbstractAdvancedCloneReporter {

  private static final CloneFilter INTERVAL_FILTER = new IntervalTreeCloneFilter();
  private static final CloneFilter INTERVAL_PAIR_FILTER = new IntervalTreeClonePairFilter();

  public static final String ALGORITHM_KEY = "algorithm";
  public static final String INIT_KEY = "init";
  public static final String FILTER_KEY = "filter";
  public static final String GROUPS_KEY = "groups";

  public AdvancedPairCloneReporter(CloneIndex cloneIndex) {
    this.cloneIndex = cloneIndex;
    this.statsCollector = new StatsCollector("AdvancedPaired");
  }

  public List<CloneGroup> reportClones(FileBlockGroup fileBlockGroup) {
    statsCollector.startTime(INIT_KEY);
    ArrayList<ClonePair> clones = Lists.newArrayList();
    SortedSet<Block> fileBlocks = fileBlockGroup.getBlockList();
    List<List<Block>> sameHashBlockGroups = getIndexedBlockGroups(fileBlockGroup);

    //an empty list is needed a the end to report clone at the end of file
    sameHashBlockGroups.add(new ArrayList<Block>());
    Map<CloneKey, ClonePair> prevActiveMap = Maps.newTreeMap();
    statsCollector.stopTime(INIT_KEY);

    statsCollector.startTime(ALGORITHM_KEY);
    Iterator<Block> blockIterator = fileBlocks.iterator();
    for (List<Block> blockGroup : sameHashBlockGroups) {
      Map<CloneKey, ClonePair> nextActiveMap = Maps.newTreeMap();

      Block origBlock = null;
      if (blockIterator.hasNext()) {
        origBlock = blockIterator.next();
      }

      for (Block block : blockGroup) {
        processBlock(prevActiveMap, nextActiveMap, origBlock, block);
      }
      clones.addAll(prevActiveMap.values());
      statsCollector.addNumber("reported pairs", prevActiveMap.values().size());

      prevActiveMap = nextActiveMap;
    }
    statsCollector.stopTime(ALGORITHM_KEY);

    int sizeBefore = clones.size();
    statsCollector.startTime(FILTER_KEY);
    List<ClonePair> filtered = INTERVAL_PAIR_FILTER.filter(clones);
    statsCollector.stopTime(FILTER_KEY);

    statsCollector.addNumber("removed covered", sizeBefore - filtered.size());

    statsCollector.startTime(GROUPS_KEY);
    List<CloneGroup> groups = groupClonePairs(filtered);
    statsCollector.stopTime(GROUPS_KEY);

    statsCollector.addNumber("total clone groups", groups.size());
    return groups;
  }

  /**
   * @param clones, array of TempClone sorted by getOriginPart().getUnitStart()
   * @return list of reported clones
   */
  private List<CloneGroup> groupClonePairs(List<ClonePair> clones) {
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
