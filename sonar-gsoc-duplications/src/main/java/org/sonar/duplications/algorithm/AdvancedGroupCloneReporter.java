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
import com.google.common.collect.Sets;
import org.sonar.duplications.algorithm.filter.CloneGroupFilter;
import org.sonar.duplications.algorithm.filter.IntervalTreeCloneGroupFilter;
import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.FileBlockGroup;
import org.sonar.duplications.index.CloneGroup;
import org.sonar.duplications.index.CloneIndex;
import org.sonar.duplications.index.ClonePair;

import java.util.*;

public class AdvancedGroupCloneReporter extends AbstractAdvancedCloneReporter {

  public static final CloneGroupFilter INTERVAL_FILTER = new IntervalTreeCloneGroupFilter();

  public static final String ALGORITHM_KEY = "main algorithm";
  public static final String INIT_KEY = "initialization";
  public static final String DUPLIACATES_KEY = "remove duplicates";
  public static final String FILTER_KEY = "filter covered";
  public static final String GROUPS_KEY = "report clones";

  public AdvancedGroupCloneReporter(CloneIndex cloneIndex) {
    this.cloneIndex = cloneIndex;
    this.statsCollector = new StatsCollector("AdvancedGrouped");
  }

  public List<CloneGroup> reportClones(FileBlockGroup fileBlockGroup) {
    statsCollector.startTime(INIT_KEY);
    List<CloneGroup> clones = Lists.newArrayList();
    List<Block> resourceBlocks = fileBlockGroup.getBlockList();
    List<List<Block>> sameHashBlockGroups = getIndexedBlockGroups(fileBlockGroup);
    //an empty list is needed a the end to report clone at the end of file
    sameHashBlockGroups.add(new ArrayList<Block>());
    Map<CloneKey, ClonePair> prevActiveMap = Maps.newTreeMap();
    statsCollector.stopTime(INIT_KEY);

    statsCollector.startTime(ALGORITHM_KEY);
    Iterator<Block> blockIterator = resourceBlocks.iterator();
    for (List<Block> blockGroup : sameHashBlockGroups) {
      Map<CloneKey, ClonePair> nextActiveMap = Maps.newTreeMap();
      Block origBlock = null;
      if (blockIterator.hasNext()) {
        origBlock = blockIterator.next();
      }
      for (Block block : blockGroup) {
        processBlock(prevActiveMap, nextActiveMap, origBlock, block);
      }
      statsCollector.stopTime(ALGORITHM_KEY);

      statsCollector.startTime(GROUPS_KEY);
      statsCollector.addNumber("reported pairs", prevActiveMap.values().size());
      clones.addAll(reportClones(prevActiveMap.values()));
      statsCollector.stopTime(GROUPS_KEY);

      statsCollector.startTime(ALGORITHM_KEY);
      prevActiveMap = nextActiveMap;
    }
    statsCollector.stopTime(ALGORITHM_KEY);

    statsCollector.addNumber("reported clones", clones.size());

    int sizeBefore = clones.size();
    statsCollector.startTime(DUPLIACATES_KEY);
    clones = removeDuplicates(clones);
    statsCollector.stopTime(DUPLIACATES_KEY);

    statsCollector.addNumber("removed duplicates", sizeBefore - clones.size());

    sizeBefore = clones.size();
    statsCollector.startTime(FILTER_KEY);
    clones = INTERVAL_FILTER.filter(clones);
    statsCollector.stopTime(FILTER_KEY);

    statsCollector.addNumber("removed covered", sizeBefore - clones.size());
    statsCollector.addNumber("total clone groups", clones.size());

    return clones;
  }

  private static List<CloneGroup> removeDuplicates(List<CloneGroup> clones) {
    HashSet<CloneGroup> set = Sets.newHashSet(clones);
    return Lists.newArrayList(set);
  }

  /**
   * @param clonePairs, array of ClonePair to report
   * @return list of reported CloneGroups
   */
  private static List<CloneGroup> reportClones(Collection<ClonePair> clonePairs) {
    List<CloneGroup> res = Lists.newArrayList();
    //sort elements of prevActiveMap by getOrigPart.getUnitStart()
    ArrayList<ClonePair> sortedArr = Lists.newArrayList(clonePairs);
    Collections.sort(sortedArr, CLONEPAIR_COMPARATOR);

    CloneGroup curClone = null;
    int prevUnitStart = -1;
    for (int j = 0; j < sortedArr.size(); j++) {
      ClonePair clonePair = sortedArr.get(j);
      int curUnitStart = clonePair.getOriginPart().getUnitStart();
      //if current sequence matches with different sequence in original file
      if (curUnitStart != prevUnitStart) {
        curClone = new CloneGroup()
            .setCloneUnitLength(clonePair.getCloneLength())
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
