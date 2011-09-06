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

import org.sonar.duplications.index.CloneGroup;
import org.sonar.duplications.index.ClonePart;

import com.google.common.collect.Lists;

public class IntervalTreeCloneFilter extends AbstractIntervalTreeCloneFilter {

  private static IntervalTree buildTrees(List<CloneGroup> clones) {
    IntervalTree originTree = new IntervalTree();

    //populate interval tree structure
    for (CloneGroup clone : clones) {
      String originResourceId = clone.getOriginPart().getResourceId();
      List<ClonePart> parts = clone.getCloneParts();
      for (ClonePart part : parts) {
        if (part.getResourceId().equals(originResourceId)) {
          PartWrapper partWrap = new PartWrapper(clone, part);
          int unitStart = part.getUnitStart();
          int unitEnd = part.getUnitStart() + clone.getCloneUnitLength() - 1;

          originTree.addInterval(new Interval(unitStart, unitEnd, partWrap));
        }
      }
    }
    return originTree;
  }

  public List<CloneGroup> filter(List<CloneGroup> clones) {
    List<CloneGroup> filtered = Lists.newArrayList();
    IntervalTree tree = buildTrees(clones);
    for (CloneGroup clone : clones) {
      if (!isCovered(tree, clone)) {
        filtered.add(clone);
      }
    }
    return filtered;
  }

  protected boolean isCovered(IntervalTree tree, CloneGroup clone) {
    ClonePart originPart = clone.getOriginPart();

    int unitStart = originPart.getUnitStart();
    int unitEnd = originPart.getUnitStart() + clone.getCloneUnitLength() - 1;

    List<Interval> intervals = tree.getCoveringIntervals(unitStart, unitEnd);

    boolean covered = false;
    for (Interval<PartWrapper> interval : intervals) {
      CloneGroup foundClone = (CloneGroup) interval.getData().getClone();
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
