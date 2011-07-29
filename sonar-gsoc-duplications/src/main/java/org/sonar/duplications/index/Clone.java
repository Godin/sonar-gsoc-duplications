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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Clone {

  private final List<ClonePart> parts = new ArrayList<ClonePart>();

  private ClonePart originPart;

  // clone length in units (not lines)
  private int cloneLength;

  private int hash;

  public Clone(String resourceId1, int unitIndex1, int lineStart1, int lineEnd1,
               String resourceId2, int unitIndex2, int lineStart2, int lineEnd2,
               int cloneLength) {
    addPart(new ClonePart(resourceId1, unitIndex1, lineStart1, lineEnd1));
    addPart(new ClonePart(resourceId2, unitIndex2, lineStart2, lineEnd2));

    this.cloneLength = cloneLength;
  }

  public Clone(int cloneLength) {
    this.cloneLength = cloneLength;
  }

  public void setOriginPart(ClonePart originPart) {
    this.originPart = originPart;
  }

  public ClonePart getOriginPart() {
    return originPart;
  }

  public Clone addPart(ClonePart part) {
    parts.add(part);
    Collections.sort(parts, null);
    return this;
  }

  public List<ClonePart> getCloneParts() {
    return Collections.unmodifiableList(parts);
  }

  public int getCloneLength() {
    return cloneLength;
  }

  public void setCloneLength(int cloneLength) {
    this.cloneLength = cloneLength;
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof Clone) {
      Clone another = (Clone) object;

      if (another.cloneLength != cloneLength
          || parts.size() != another.parts.size())
        return false;

      boolean result = true;
      for (int i = 0; i < parts.size(); i++) {
        result &= another.parts.get(i).equals(parts.get(i));
      }

      result &= another.originPart.equals(originPart);

      return result;
    }
    return false;
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

  @Override
  public String toString() {
    String res = "";
    for (ClonePart part : parts) {
      res = res + part.toString() + " - ";
    }
    return res + cloneLength;
  }
}
