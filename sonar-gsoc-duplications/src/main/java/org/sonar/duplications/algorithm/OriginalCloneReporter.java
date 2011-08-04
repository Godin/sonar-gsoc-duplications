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
import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.FileBlockGroup;
import org.sonar.duplications.index.CloneGroup;
import org.sonar.duplications.index.CloneIndex;
import org.sonar.duplications.index.ClonePart;

import java.util.List;

/**
 * Implementation of algorithm described in paper
 * <a href="http://www4.in.tum.de/~juergens/publications/icsm2010_crc.pdf">Index-Based Code Clone Detection: Incremental, Distributed, Scalable</a>
 * by Benjamin Hummel, Elmar Juergens, Michael Conradt and Lars Heinemann.
 */
public class OriginalCloneReporter implements CloneReporterAlgorithm {

  private final CloneIndex cloneIndex;
  private final List<CloneGroup> clones = Lists.newArrayList();

  public OriginalCloneReporter(CloneIndex cloneIndex) {
    this.cloneIndex = cloneIndex;
  }

  /**
   * Performs detection and returns list of clone groups between index and file, which represented as sorted list of blocks.
   */
  public List<CloneGroup> reportClones(FileBlockGroup fileBlockGroup) {
    findClones(fileBlockGroup.getBlockList());
    return this.clones;
  }

  private void findClones(List<Block> fileBlocks) {
    // 2: let f be the list of tuples corresponding to filename sorted by statement index
    // either read from the index or calculated on the fly

    // 3: let c be a list with c(0) = empty
    BlocksGroup[] sameHashBlocksGroups = new BlocksGroup[fileBlocks.size() + 2];
    sameHashBlocksGroups[0] = BlocksGroup.empty();

    // 4: for i := 1 to length(f) do
    for (int i = 0; i < fileBlocks.size(); i++) {
      Block block = fileBlocks.get(i);
      List<Block> blocks = Lists.newArrayList();
      // TODO Godin: we assume that file still not in index, so we add blocks explicitly, but not from index
      // moreover - we don't detect clones in same file
      blocks.add(block);
      // 5: retrieve tuples with same sequence hash as f(i)
      blocks.addAll(cloneIndex.getBySequenceHash(block.getBlockHash()));
      // 6: store this set as c(i)
      sameHashBlocksGroups[i + 1] = BlocksGroup.from(blocks);
    }

    // allows to report clones at the end of file, because condition at line 13 would be evaluated as true
    // TODO Godin: not sure about this hack
    sameHashBlocksGroups[fileBlocks.size() + 1] = BlocksGroup.empty();

    // 7: for i := 1 to length(c) do
    for (int i = 1; i < sameHashBlocksGroups.length; i++) {
      // 8: if |c(i)| < 2 or c(i) subsumed by c(i - 1) then
      if (sameHashBlocksGroups[i].size() < 2 || sameHashBlocksGroups[i].subsumedBy(sameHashBlocksGroups[i - 1])) {
        // 9: continue with next loop iteration
        continue;
      }
      // 10: let a := c(i)
      BlocksGroup currentBlocksGroup = sameHashBlocksGroups[i];
      // 11: for j := i + 1 to length(c) do
      for (int j = i + 1; j < sameHashBlocksGroups.length; j++) {
        // 12: let a0 := a intersect c(j)
        BlocksGroup intersectedBlocksGroup = currentBlocksGroup.intersect(sameHashBlocksGroups[j]);
        // 13: if |a0| < |a| then
        if (intersectedBlocksGroup.size() < currentBlocksGroup.size()) {
          // 14: report clones from c(i) to a (see text)

          // TODO Godin: problem described in original paper
          // One problem of this algorithm is that clone classes with
          // multiple instances in the same file are encountered and re
          // reported multiple times. Furthermore, when calculating the clone
          // groups for all files in a system, clone groups will be reported
          // more than once as well. Both cases can be avoided, by
          // checking whether the first element of a (with respect to a
          // fixed order) is equal to f (j) and only report in this case.

          reportClones(sameHashBlocksGroups[i], currentBlocksGroup, j - i);
        }
        // 15: a := a0
        currentBlocksGroup = intersectedBlocksGroup;
        // 16: if |a| < 2 or a subsumed by c(i -1) then
        if (currentBlocksGroup.size() < 2 || currentBlocksGroup.subsumedBy(sameHashBlocksGroups[i - 1])) {
          // 17: break inner loop
          break;
        }
      }
    }
  }

  private void reportClones(BlocksGroup beginGroup, BlocksGroup endGroup, int cloneLength) {
    CloneGroup clone = new CloneGroup(cloneLength);
    for (Block[] pair : beginGroup.pairs(endGroup, cloneLength)) {
      ClonePart part = new ClonePart(pair[0].getResourceId(), pair[0].getIndexInFile(), pair[0].getFirstLineNumber(), pair[1].getLastLineNumber());
      clone.addPart(part);
    }
    clones.add(clone);
  }
}
