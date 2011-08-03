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
package org.sonar.duplications.index;

import java.util.ArrayList;
import java.util.List;

public class ClonePair {
  private ClonePart originPart;
  private ClonePart anotherPart;
  private int cloneLength;

  public ClonePair(ClonePart originPart, ClonePart anotherPart, int cloneLength) {
    this.originPart = originPart;
    this.anotherPart = anotherPart;
    this.cloneLength = cloneLength;
  }

  public ClonePart getOriginPart() {
    return originPart;
  }

  public ClonePart getAnotherPart() {
    return anotherPart;
  }

  public List<ClonePart> getCloneParts() {
    List<ClonePart> result = new ArrayList<ClonePart>();
    result.add(originPart);
    result.add(anotherPart);
    return result;
  }

  public int getCloneLength() {
    return cloneLength;
  }

  /**
   * Checks if first <tt>Clone</tt> is contained in second <tt>Clone</tt>. Clone A is contained in another
   * Clone B if every ClonePart pA from A has ClonePart pB in B which satisfy the conditions
   * pA.resourceId == pB.resourceId and pA.unitStart >= pB.unitStart and pA.unitEnd <= pb.unitEnd
   *
   * @param other Clone where current Clone should be contained
   * @return
   */
  public boolean containsIn(ClonePair other) {
    if (!getOriginPart().getResourceId().equals(other.getOriginPart().getResourceId())) {
      return false;
    }
    List<ClonePart> parts1 = new ArrayList<ClonePart>();
    parts1.add(originPart);
    parts1.add(anotherPart);
    List<ClonePart> parts2 = new ArrayList<ClonePart>();
    parts2.add(other.getOriginPart());
    parts2.add(other.getAnotherPart());
    for (ClonePart firstPart : parts1) {
      int firstUnitEnd = firstPart.getUnitStart() + getCloneLength();
      boolean found = false;
      for (ClonePart secondPart : parts2) {
        int secondUnitEnd = secondPart.getUnitStart() + other.getCloneLength();
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
}
