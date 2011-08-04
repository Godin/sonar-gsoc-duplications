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

import com.google.common.collect.Maps;
import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.FileBlockGroup;
import org.sonar.duplications.index.CloneGroup;
import org.sonar.duplications.index.CloneIndex;
import org.sonar.duplications.index.ClonePart;
import org.sonar.duplications.interval.Interval;
import org.sonar.duplications.interval.IntervalTree;

import java.util.*;

public class AdvancedCloneReporter implements CloneReporterAlgorithm {

  public static final String ALGORITHM_KEY = "algorithm";
  public static final String INIT_KEY = "init";
  public static final String DUPLIACATES_KEY = "duplicates";
  public static final String FILTER_KEY = "filter";
  public static final String GROUPS_KEY = "groups";

  public static final String[] TIME_KEYS = new String[]{ALGORITHM_KEY, INIT_KEY, DUPLIACATES_KEY, FILTER_KEY, GROUPS_KEY};

  private final CloneIndex cloneIndex;
  private Map<String, Long> workingTimes;
  private Map<String, Long> startTimes;

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
    List<CloneGroup> clones = new ArrayList<CloneGroup>();

    ArrayList<Block> resourceBlocks = new ArrayList<Block>(fileBlockGroup.getBlockList());

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
    endTime(INIT_KEY);

    startTime(ALGORITHM_KEY);
    Map<Key, TempClone> prevActiveMap = new TreeMap<Key, TempClone>();
    for (int i = 0; i < sameHashBlockGroups.size(); i++) {
      Map<Key, TempClone> nextActiveMap = new TreeMap<Key, TempClone>();

      for (Block block : sameHashBlockGroups.get(i)) {
        Block origBlock = resourceBlocks.get(i);
        processBlock(prevActiveMap, nextActiveMap, origBlock, block);
      }
      endTime(ALGORITHM_KEY);

      startTime(GROUPS_KEY);
      //sort elements of prevActiveMap by getOrigPart.getUnitStart()
      ArrayList<TempClone> sortedArr = new ArrayList<TempClone>(prevActiveMap.values());
      Collections.sort(sortedArr, new TempCloneComparator());
      clones.addAll(reportClones(sortedArr));
      endTime(GROUPS_KEY);

      startTime(ALGORITHM_KEY);

      prevActiveMap = nextActiveMap;
    }
    endTime(ALGORITHM_KEY);

    startTime(DUPLIACATES_KEY);
    clones = removeDuplicates(clones);
    endTime(DUPLIACATES_KEY);

    startTime(FILTER_KEY);
    clones = filterCoveredIntervalTree(clones);
    endTime(FILTER_KEY);

    return clones;
  }

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

  private final static class PartWrapper {
    public CloneGroup clone;
    public ClonePart part;

    private PartWrapper(CloneGroup clone, ClonePart part) {
      this.clone = clone;
      this.part = part;
    }

    public CloneGroup getClone() {
      return clone;
    }

    public ClonePart getPart() {
      return part;
    }
  }

  private static class TempCloneComparator implements Comparator<TempClone> {

    public int compare(TempClone o1, TempClone o2) {
      return o1.getOrigPart().getUnitStart() - o2.getOrigPart().getUnitStart();
    }
  }

  /**
   * O(n^2) filter for clones fully covered by another clones
   *
   * @param clones original list of clones
   * @return filtered list of clones
   */
  private static List<CloneGroup> filterCovered(List<CloneGroup> clones) {
    List<CloneGroup> filtered = new ArrayList<CloneGroup>();
    for (int i = 0; i < clones.size(); i++) {
      CloneGroup first = clones.get(i);
      boolean covered = false;
      for (int j = 0; j < clones.size(); j++) {
        if (i == j) {
          continue;
        }

        CloneGroup second = clones.get(j);
        covered |= containsIn(first, second);
        if (covered)
          break;
      }
      if (!covered)
        filtered.add(first);
    }
    return filtered;
  }

  private static Map<String, IntervalTree> buildTrees(List<CloneGroup> clones) {
    Map<String, IntervalTree> trees = Maps.newHashMap();

    //populate interval tree structure
    for (CloneGroup clone : clones) {
      for (ClonePart part : clone.getCloneParts()) {
        PartWrapper partWrap = new PartWrapper(clone, part);
        IntervalTree tree = trees.get(part.getResourceId());
        if (tree == null) {
          tree = new IntervalTree();
          trees.put(part.getResourceId(), tree);
        }
        int unitStart = part.getUnitStart();
        int unitEnd = part.getUnitStart() + clone.getCloneUnitLength() - 1;

        tree.addInterval(new Interval(unitStart, unitEnd, partWrap));
      }
    }

    return trees;
  }

  /**
   * O(n log n) filter using interval tree for clones fully covered by another clones
   *
   * @param clones original list of clones
   * @return filtered list of clones
   */
  private static List<CloneGroup> filterCoveredIntervalTree(List<CloneGroup> clones) {
    List<CloneGroup> filtered = new ArrayList<CloneGroup>();

    Map<String, IntervalTree> trees = buildTrees(clones);

    for (CloneGroup clone : clones) {
      ClonePart originPart = clone.getOriginPart();
      IntervalTree tree = trees.get(originPart.getResourceId());

      int unitStart = originPart.getUnitStart();
      int unitEnd = originPart.getUnitStart() + clone.getCloneUnitLength() - 1;
      List<Interval> intervals = tree.getCoveringIntervals(unitStart, unitEnd);

      boolean covered = false;
      for (Interval<PartWrapper> interval : intervals) {
        CloneGroup foundClone = interval.getData().getClone();
        if (foundClone.equals(clone))
          continue;

        covered |= containsIn(clone, foundClone);
        if (covered)
          break;
      }

      if (!covered)
        filtered.add(clone);
    }
    return filtered;
  }

  private static boolean containsIn(CloneGroup first, CloneGroup second) {
    if (!first.getOriginPart().getResourceId().equals(second.getOriginPart().getResourceId())) {
      return false;
    }
    for (int i = 0; i < first.getCloneParts().size(); i++) {
      ClonePart firstPart = first.getCloneParts().get(i);
      int firstUnitEnd = firstPart.getUnitStart() + first.getCloneUnitLength();
      boolean found = false;

      for (int j = 0; j < second.getCloneParts().size(); j++) {
        ClonePart secondPart = second.getCloneParts().get(j);
        int secondUnitEnd = secondPart.getUnitStart() + second.getCloneUnitLength();
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

  private static List<CloneGroup> removeDuplicates(List<CloneGroup> clones) {
    HashSet<CloneGroup> set = new HashSet<CloneGroup>(clones);
    List<CloneGroup> result = new ArrayList<CloneGroup>(set);

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
  private static void processBlock(Map<Key, TempClone> prevActiveMap,
                                   Map<Key, TempClone> nextActiveMap,
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
  private static List<CloneGroup> reportClones(List<TempClone> sortedArr) {
    List<CloneGroup> res = new ArrayList<CloneGroup>();
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
