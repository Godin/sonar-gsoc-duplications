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

import org.sonar.duplications.index.ClonePair;
import org.sonar.duplications.index.ClonePart;
import org.sonar.duplications.interval.Interval;
import org.sonar.duplications.interval.IntervalTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class IntervalTreeClonePairFilter implements ClonePairFilter {

  private final static class PartWrapper {
    public ClonePair clone;
    public ClonePart part;

    private PartWrapper(ClonePair clone, ClonePart part) {
      this.clone = clone;
      this.part = part;
    }

    public ClonePair getClone() {
      return clone;
    }

    public ClonePart getPart() {
      return part;
    }
  }

  private Map<String, IntervalTree> buildTrees(List<ClonePair> clones) {
    HashMap<String, IntervalTree> trees = new HashMap<String, IntervalTree>();

    //populate interval tree structure
    for (ClonePair clone : clones) {
      ClonePart part = clone.getOriginPart();
      PartWrapper partWrap = new PartWrapper(clone, part);
      IntervalTree tree = trees.get(part.getResourceId());
      if (tree == null) {
        tree = new IntervalTree();
        trees.put(part.getResourceId(), tree);
      }
      int unitStart = part.getUnitStart();
      int unitEnd = part.getUnitStart() + clone.getCloneLength() - 1;

      tree.addInterval(new Interval(unitStart, unitEnd, partWrap));
    }
    return trees;
  }

  public List<ClonePair> filter(List<ClonePair> clones) {
    List<ClonePair> filtered = new ArrayList<ClonePair>();
    Map<String, IntervalTree> trees = buildTrees(clones);

    for (ClonePair clone : clones) {
      ClonePart originPart = clone.getOriginPart();
      IntervalTree tree = trees.get(originPart.getResourceId());

      int unitStart = originPart.getUnitStart();
      int unitEnd = originPart.getUnitStart() + clone.getCloneLength() - 1;
      List<Interval> intervals = tree.getCoveringIntervals(unitStart, unitEnd);

      boolean covered = false;
      for (Interval<PartWrapper> interval : intervals) {
        ClonePair foundClone = interval.getData().getClone();
        if (foundClone.equals(clone)) {
          continue;
        }

        covered |= clone.containsIn(foundClone);
        if (covered) {
          break;
        }
      }

      if (!covered) {
        filtered.add(clone);
      }
    }
    return filtered;
  }
}
