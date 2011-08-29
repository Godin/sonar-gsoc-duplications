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

import com.google.common.collect.Lists;
import org.sonar.duplications.index.ClonePart;
import org.sonar.duplications.index.ClonePartContainerBase;

import java.util.List;

public class IntervalTreeCloneFilter extends AbstractIntervalTreeCloneFilter {

  private static <T extends ClonePartContainerBase> IntervalTree buildTrees(List<T> clones) {
    IntervalTree originTree = new IntervalTree();

    //populate interval tree structure
    for (T clone : clones) {
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

  public <T extends ClonePartContainerBase> List<T> filter(List<T> clones) {
    List<T> filtered = Lists.newArrayList();
    IntervalTree tree = buildTrees(clones);

    for (T clone : clones) {
      if (!isCovered(tree, clone)) {
        filtered.add(clone);
      }
    }
    return filtered;
  }

}
