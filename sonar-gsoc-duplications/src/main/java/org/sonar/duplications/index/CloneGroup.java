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
package org.sonar.duplications.index;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.sonar.duplications.utils.SortedListsUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Groups a set of related {@link ClonePart}s.
 */
public class CloneGroup {

  private final ClonePart originPart;
  private final int cloneLength;
  private final List<ClonePart> parts;

  private int hash;

  /**
   * FIXME Godin: this constructor performs sorting of parts, whereas in fact algorithm should do this
   */
  public CloneGroup(int cloneLength, ClonePart origin, List<ClonePart> parts) {
    this.cloneLength = cloneLength;
    this.originPart = origin;
    List<ClonePart> sortedParts = Lists.newArrayList(parts);
    Collections.sort(sortedParts, null);
    this.parts = ImmutableList.copyOf(sortedParts);
  }

  public ClonePart getOriginPart() {
    return originPart;
  }

  /**
   * @return clone length in units (not in lines)
   */
  public int getCloneUnitLength() {
    return cloneLength;
  }

  public List<ClonePart> getCloneParts() {
    return parts;
  }

  public boolean containsIn(CloneGroup other) {
    return containsIn(this, other);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (ClonePart part : getCloneParts()) {
      builder.append(part).append(" - ");
    }
    builder.append(cloneLength);
    return builder.toString();
  }

  /**
   * Two groups are equal, if they have same length, same origins and contain same parts in same order.
   */
  @Override
  public boolean equals(Object object) {
    if (!(object instanceof CloneGroup)) {
      return false;
    }
    CloneGroup another = (CloneGroup) object;
    if (another.cloneLength != cloneLength || parts.size() != another.parts.size()) {
      return false;
    }
    if (!originPart.equals(another.originPart)) {
      return false;
    }
    boolean result = true;
    for (int i = 0; i < getCloneParts().size(); i++) {
      result &= another.getCloneParts().get(i).equals(getCloneParts().get(i));
    }
    return result;
  }

  @Override
  public int hashCode() {
    int h = hash;
    if (h == 0 && cloneLength != 0) {
      for (ClonePart part : parts) {
        h = 31 * h + part.hashCode();
      }
      h = 31 * h + originPart.hashCode();
      h = 31 * h + cloneLength;
      hash = h;
    }
    return h;
  }

  /**
   * Checks that second clone contains first one.
   * <p>
   * Clone A is contained in another clone B, if every part pA from A has part pB in B,
   * which satisfy the conditions:
   * <pre>
   * (pA.resourceId == pB.resourceId) and (pB.unitStart <= pA.unitStart) and (pA.unitEnd <= pB.unitEnd)
   * </pre>
   * And all resourcesId from B exactly the same as all resourceId from A, which means that also every part pB from B has part pA in A,
   * which satisfy the condition:
   * <pre>
   * pB.resourceId == pA.resourceId
   * </pre>
   * So this relation is:
   * <ul>
   * <li>reflexive - A in A</li>
   * <li>transitive - (A in B) and (B in C) => (A in C)</li>
   * <li>antisymmetric - (A in B) and (B in A) <=> (A = B)</li>
   * </ul>
   * </p>
   * <p>
   * This method uses the fact that all parts already sorted by resourceId and unitStart (see {@link #getCloneParts()}),
   * so running time - O(|A|+|B|).
   * TODO Godin: maybe {@link org.sonar.duplications.utils.FastStringComparator} can be used here to increase performance
   * </p>
   */
  private static boolean containsIn(CloneGroup first, CloneGroup second) {
    if (!first.getOriginPart().getResourceId().equals(second.getOriginPart().getResourceId())) {
      return false;
    }
    if (first.getCloneUnitLength() > second.getCloneUnitLength()) {
      return false;
    }
    List<ClonePart> firstParts = first.getCloneParts();
    List<ClonePart> secondParts = second.getCloneParts();
    return SortedListsUtils.contains(secondParts, firstParts, new ContainsInComparator(first.getCloneUnitLength(), second.getCloneUnitLength()))
        && SortedListsUtils.contains(firstParts, secondParts, RESOURCE_ID_COMPARATOR);
  }

  private static final Comparator<ClonePart> RESOURCE_ID_COMPARATOR = new Comparator<ClonePart>() {
    public int compare(ClonePart o1, ClonePart o2) {
      return o1.getResourceId().compareTo(o2.getResourceId());
    }
  };

  private static class ContainsInComparator implements Comparator<ClonePart> {
    private final int l1, l2;

    public ContainsInComparator(int l1, int l2) {
      this.l1 = l1;
      this.l2 = l2;
    }

    public int compare(ClonePart o1, ClonePart o2) {
      int c = o1.getResourceId().compareTo(o2.getResourceId());
      if (c == 0) {
        if (o2.getUnitStart() <= o1.getUnitStart()) {
          if (o1.getUnitStart() + l1 <= o2.getUnitStart() + l2) {
            return 0; // match found - stop search
          } else {
            return 1; // continue search
          }
        } else {
          return -1; // o1 < o2 by unitStart - stop search
        }
      } else {
        return c;
      }
    }
  }

}
