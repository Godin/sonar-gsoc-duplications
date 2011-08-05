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
import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.FileBlockGroup;
import org.sonar.duplications.index.CloneGroup;
import org.sonar.duplications.index.CloneIndex;
import org.sonar.duplications.index.ClonePart;

import java.util.*;

public class AdvancedCloneReporter implements CloneReporterAlgorithm {

  public static final CloneGroupFilter INTERVAL_FILTER = new IntervalTreeCloneGroupFilter();
  public static final CloneGroupFilter SIMPLE_FILTER = new BruteForceCloneGroupFilter();

  public static final String ALGORITHM_KEY = "algorithm";
  public static final String INIT_KEY = "init";
  public static final String DUPLIACATES_KEY = "duplicates";
  public static final String FILTER_KEY = "filter";
  public static final String GROUPS_KEY = "groups";

  public static final String[] TIME_KEYS = new String[]{ALGORITHM_KEY, INIT_KEY, DUPLIACATES_KEY, FILTER_KEY, GROUPS_KEY};

  private static final Comparator<TempClone> TEMP_CLONE_COMPARATOR = new Comparator<TempClone>() {
    public int compare(TempClone o1, TempClone o2) {
      return o1.getOrigPart().getUnitStart() - o2.getOrigPart().getUnitStart();
    }
  };

  private final CloneIndex cloneIndex;
  private Map<String, Long> workingTimes;
  private Map<String, Long> startTimes;


  private final static class TempClone {
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

  public AdvancedCloneReporter(CloneIndex cloneIndex) {
    this.cloneIndex = cloneIndex;
    workingTimes = Maps.newHashMap();
    startTimes = Maps.newHashMap();
  }

  public void printTimes() {
    long total = 0;
    for (String key : TIME_KEYS) {
      if (workingTimes.containsKey(key)) {
        total += workingTimes.get(key);
      }
    }
    for (String key : TIME_KEYS) {
      long time = 0;
      if (workingTimes.containsKey(key)) {
        time = workingTimes.get(key);
      }
      long percentage = Math.round(100.0 * time / total);
      System.out.println("Working time for '" + key + "':" + time + " - " + percentage);
    }
  }

  private void startTime(String key) {
    startTimes.put(key, System.currentTimeMillis());
  }

  private void endTime(String key) {
    long startTime = startTimes.get(key);
    long prevTime = 0;
    if (workingTimes.containsKey(key)) {
      prevTime = workingTimes.get(key);
    }
    prevTime += System.currentTimeMillis() - startTime;
    workingTimes.put(key, prevTime);
  }

  public List<CloneGroup> reportClones(FileBlockGroup fileBlockGroup) {
    startTime(INIT_KEY);
    List<CloneGroup> clones = Lists.newArrayList();

    List<Block> resourceBlocks = fileBlockGroup.getBlockList();

    ArrayList<List<Block>> sameHashBlockGroups = Lists.newArrayList();

    for (Block block : fileBlockGroup.getBlockList()) {
      List<Block> sameHashBlockGroup = Lists.newArrayList();
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
    Map<CloneKey, TempClone> prevActiveMap = Maps.newTreeMap();
    endTime(INIT_KEY);

    startTime(ALGORITHM_KEY);
    Iterator<Block> blockIterator = resourceBlocks.iterator();
    for (List<Block> blockGroup : sameHashBlockGroups) {
      //for (int i = 0; i < sameHashBlockGroups.size(); i++) {
      Map<CloneKey, TempClone> nextActiveMap = Maps.newTreeMap();

      Block origBlock = null;
      if (blockIterator.hasNext()) {
        origBlock = blockIterator.next();
      }

      for (Block block : blockGroup) {
        processBlock(prevActiveMap, nextActiveMap, origBlock, block);
      }
      endTime(ALGORITHM_KEY);

      startTime(GROUPS_KEY);
      clones.addAll(reportClones(prevActiveMap.values()));
      endTime(GROUPS_KEY);

      startTime(ALGORITHM_KEY);

      prevActiveMap = nextActiveMap;
    }
    endTime(ALGORITHM_KEY);

    startTime(DUPLIACATES_KEY);
    clones = removeDuplicates(clones);
    endTime(DUPLIACATES_KEY);

    startTime(FILTER_KEY);
    clones = INTERVAL_FILTER.filter(clones);
    endTime(FILTER_KEY);

    return clones;
  }

  private static List<CloneGroup> removeDuplicates(List<CloneGroup> clones) {
    HashSet<CloneGroup> set = Sets.newHashSet(clones);
    return Lists.newArrayList(set);
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
  private static void processBlock(Map<CloneKey, TempClone> prevActiveMap,
                                   Map<CloneKey, TempClone> nextActiveMap,
                                   Block origBlock, Block anotherBlock) {
    ClonePart origPart = new ClonePart(origBlock);
    ClonePart anotherPart = new ClonePart(anotherBlock);
    int cloneLength = 0;

    CloneKey curKey = new CloneKey(anotherBlock.getResourceId(), anotherBlock.getIndexInFile());
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

    CloneKey nextKey = new CloneKey(anotherBlock.getResourceId(), anotherBlock.getIndexInFile() + 1);
    nextActiveMap.put(nextKey, tempClone);
  }


  /**
   * @param tempClones, array of TempClone to report
   * @return list of reported CloneGroups
   */
  private static List<CloneGroup> reportClones(Collection<TempClone> tempClones) {
    List<CloneGroup> res = Lists.newArrayList();
    //sort elements of prevActiveMap by getOrigPart.getUnitStart()
    ArrayList<TempClone> sortedArr = Lists.newArrayList(tempClones);
    Collections.sort(sortedArr, TEMP_CLONE_COMPARATOR);

    CloneGroup curClone = null;
    int prevUnitStart = -1;
    for (int j = 0; j < sortedArr.size(); j++) {
      TempClone tempClone = sortedArr.get(j);
      int curUnitStart = tempClone.getOrigPart().getUnitStart();
      //if current sequence matches with different sequence in original file
      if (curUnitStart != prevUnitStart) {
        curClone = new CloneGroup(tempClone.getCloneLength());
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
