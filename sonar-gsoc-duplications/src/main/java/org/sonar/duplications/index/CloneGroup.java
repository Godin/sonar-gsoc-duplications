/*
 * Sonar, open source software quality management tool.
 * Written (W) 2011 Andrew Tereskin
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
package org.sonar.duplications.index;

import java.util.Collections;
import java.util.List;

import org.sonar.duplications.detector.original.FastStringComparator;

import com.google.common.collect.Lists;

public class CloneGroup extends ClonePartContainerBase<CloneGroup> {

  private boolean sorted = true;

  public CloneGroup() {
    this.parts = Lists.newArrayList();
  }

  public CloneGroup(int cloneUnitLength) {
    this.parts = Lists.newArrayList();
    this.cloneLength = cloneUnitLength;
  }

  public CloneGroup addPart(ClonePart part) {
    parts.add(part);
    sorted = false;
    return this;
  }

  void sortParts() {
    Collections.sort(parts, null);
  }

  boolean isSorted() {
    return sorted;
  }

  @Override
  public List<ClonePart> getCloneParts() {
    if (!sorted) {
      sortParts();
      sorted = true;
    }
    return Collections.unmodifiableList(parts);
  }

  @Override
  public boolean containsIn(ClonePartContainerBase<CloneGroup> other) {
    // TODO Godin: method from superclass overridden in order to fix bug
    return containsIn(this, (CloneGroup) other);
  }

  /**
   * Clone A is contained in another clone B, if every part pA from A has part pB in B,
   * which satisfy the conditions:
   * <pre>
   * (pA.resourceId == pb.resourceId) and (pB.unitStart <= pA.unitStart) and (pA.unitEnd <= pb.unitEnd)
   * </pre>
   * TODO Godin: maybe {@link FastStringComparator} can be used here to increase performance
   */
  private static boolean containsIn(CloneGroup first, CloneGroup second) {
    if (!first.getOriginPart().getResourceId().equals(second.getOriginPart().getResourceId())) {
      return false;
    }

    List<ClonePart> firstParts = first.getCloneParts();
    List<ClonePart> secondParts = second.getCloneParts();

    for (int i = 0; i < firstParts.size(); i++) {
      ClonePart firstPart = firstParts.get(i);
      int firstPartUnitEnd = firstPart.getUnitStart() + first.getCloneUnitLength();
      boolean found = false;

      for (int j = 0; j < secondParts.size(); j++) {
        ClonePart secondPart = secondParts.get(j);
        int secondPartUnitEnd = secondPart.getUnitStart() + second.getCloneUnitLength();
        if ((firstPart.getResourceId().equals(secondPart.getResourceId())) &&
            (secondPart.getUnitStart() <= firstPart.getUnitStart()) &&
            (firstPartUnitEnd <= secondPartUnitEnd)) {
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

}
