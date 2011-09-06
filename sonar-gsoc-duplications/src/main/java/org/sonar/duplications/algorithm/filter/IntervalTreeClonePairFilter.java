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
package org.sonar.duplications.algorithm.filter;

import java.util.List;
import java.util.Map;

import org.sonar.duplications.algorithm.ClonePair;
import org.sonar.duplications.index.ClonePart;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class IntervalTreeClonePairFilter extends AbstractIntervalTreeCloneFilter {

  private static Map<String, IntervalTree> buildTrees(List<ClonePair> clones) {
    Map<String, IntervalTree> trees = Maps.newHashMap();

    //populate interval tree structure
    for (ClonePair clonePair : clones) {
      String originResourceId = clonePair.getOriginPart().getResourceId();
      String otherResourceId = clonePair.getAnotherPart().getResourceId();
      IntervalTree tree = trees.get(otherResourceId);
      if (tree == null) {
        tree = new IntervalTree();
        trees.put(otherResourceId, tree);
      }
      List<ClonePart> parts = clonePair.getCloneParts();
      for (ClonePart part : parts) {
        if (part.getResourceId().equals(originResourceId)) {
          PartWrapper partWrap = new PartWrapper(clonePair, part);
          int unitStart = part.getUnitStart();
          int unitEnd = part.getUnitStart() + clonePair.getCloneUnitLength() - 1;

          tree.addInterval(new Interval(unitStart, unitEnd, partWrap));
        }
      }
    }

    return trees;
  }

  public List<ClonePair> filter(List<ClonePair> clones) {
    if (clones.isEmpty()) {
      return clones;
    }
    List<ClonePair> filtered = Lists.newArrayList();
    Map<String, IntervalTree> trees = buildTrees(clones);
    for (ClonePair clonePair : clones) {
      IntervalTree tree = trees.get(clonePair.getAnotherPart().getResourceId());
      if (!isCovered(tree, clonePair)) {
        filtered.add(clonePair);
      }
    }
    return filtered;
  }

  protected boolean isCovered(IntervalTree tree, ClonePair clone) {
    ClonePart originPart = clone.getOriginPart();

    int unitStart = originPart.getUnitStart();
    int unitEnd = originPart.getUnitStart() + clone.getCloneUnitLength() - 1;

    List<Interval> intervals = tree.getCoveringIntervals(unitStart, unitEnd);

    boolean covered = false;
    for (Interval<PartWrapper> interval : intervals) {
      ClonePair foundClone = (ClonePair) interval.getData().getClone();
      if (foundClone.equals(clone)) {
        continue;
      }
      if (clone.containsIn(foundClone)) {
        covered = true;
        break;
      }
    }

    return covered;
  }

}
