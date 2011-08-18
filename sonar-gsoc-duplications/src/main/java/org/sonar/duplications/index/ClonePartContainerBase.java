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

import java.util.Collections;
import java.util.List;

public abstract class ClonePartContainerBase<E> {

  protected ClonePart originPart;
  protected int cloneLength;
  protected List<ClonePart> parts;

  // Cache for hash code.
  private int hash;

  public ClonePart getOriginPart() {
    return originPart;
  }

  public E setOriginPart(ClonePart originPart) {
    this.originPart = originPart;
    return (E) this;
  }

  /**
   * @return clone length in units (not in lines)
   */
  public int getCloneUnitLength() {
    return cloneLength;
  }

  /**
   * @param cloneLength clone length in units (not in lines)
   */
  public E setCloneUnitLength(int cloneLength) {
    this.cloneLength = cloneLength;
    return (E) this;
  }

  /**
   * @return list of ClonePart elements stored in this object
   */
  public List<ClonePart> getCloneParts() {
    return Collections.unmodifiableList(parts);
  }

  /**
   * Checks if first <tt>Clone</tt> is contained in second <tt>Clone</tt>. Clone A is contained in another
   * Clone B if every ClonePart pA from A has ClonePart pB in B which satisfy the conditions
   * pA.resourceId == pB.resourceId and pA.unitStart >= pB.unitStart and pA.unitEnd <= pb.unitEnd
   *
   * @param other Clone where current Clone should be contained
   * @return
   */
  public boolean containsIn(ClonePartContainerBase<E> other) {
    if (!getOriginPart().getResourceId().equals(other.getOriginPart().getResourceId())) {
      return false;
    }
    for (ClonePart first : this.getCloneParts()) {
      int firstUnitEnd = first.getUnitStart() + getCloneUnitLength();
      boolean found = false;
      for (ClonePart second : other.getCloneParts()) {
        int secondUnitEnd = second.getUnitStart() + other.getCloneUnitLength();

        if (first.getResourceId().equals(second.getResourceId()) &&
            first.getUnitStart() >= second.getUnitStart() &&
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

  @Override
  public int hashCode() {
    int h = hash;
    if (h == 0 && cloneLength != 0) {
      for (ClonePart part : getCloneParts()) {
        h = 31 * h + part.hashCode();
      }
      h = 31 * h + getOriginPart().hashCode();
      h = 31 * h + cloneLength;
      hash = h;
    }
    return h;
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

  @Override
  public boolean equals(Object object) {
    if (object instanceof ClonePartContainerBase) {
      ClonePartContainerBase another = (ClonePartContainerBase) object;

      if (another.cloneLength != cloneLength ||
          getCloneParts().size() != another.getCloneParts().size()) {
        return false;
      }

      boolean result = true;
      for (int i = 0; i < getCloneParts().size(); i++) {
        result &= another.getCloneParts().get(i).equals(getCloneParts().get(i));
      }

      result &= another.getOriginPart().equals(getOriginPart());

      return result;
    }
    return false;
  }
}
