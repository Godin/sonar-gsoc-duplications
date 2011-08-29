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

import org.sonar.duplications.index.ClonePart;
import org.sonar.duplications.index.ClonePartContainerBase;

import java.util.List;

public abstract class AbstractIntervalTreeCloneFilter implements CloneFilter {

  protected final static class PartWrapper<T extends ClonePartContainerBase> {
    public T clone;
    public ClonePart part;

    public PartWrapper(T clone, ClonePart part) {
      this.clone = clone;
      this.part = part;
    }

    public T getClone() {
      return clone;
    }

  }

  protected <T extends ClonePartContainerBase> boolean isCovered(IntervalTree tree, T clone) {
    ClonePart originPart = clone.getOriginPart();

    int unitStart = originPart.getUnitStart();
    int unitEnd = originPart.getUnitStart() + clone.getCloneUnitLength() - 1;

    List<Interval> intervals = tree.getCoveringIntervals(unitStart, unitEnd);

    boolean covered = false;
    for (Interval<PartWrapper<T>> interval : intervals) {
      T foundClone = interval.getData().getClone();
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
