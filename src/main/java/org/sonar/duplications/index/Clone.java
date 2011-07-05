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

  //clone length in units (not lines)
  private int cloneLength;

  public Clone(String resourceId1, int unitIndex1, int lineStart1, int lineEnd1,
               String resourceId2, int unitIndex2, int lineStart2, int lineEnd2,
               int cloneLength) {
    parts.add(new ClonePart(resourceId1, unitIndex1, lineStart1, lineEnd1));
    parts.add(new ClonePart(resourceId2, unitIndex2, lineStart2, lineEnd2));

    Collections.sort(parts, null);

    this.cloneLength = cloneLength;
  }

  public Clone(ClonePart part1, ClonePart part2, int cloneLength) {
    parts.add(part1);
    parts.add(part2);
    Collections.sort(parts, null);

    this.cloneLength = cloneLength;
  }

  public Clone(int cloneLength) {
    this.cloneLength = cloneLength;
  }

  public void addPart(ClonePart part) {
    parts.add(part);
    Collections.sort(parts, null);
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

      return result;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return parts.get(0).hashCode() + 31 * parts.get(1).hashCode() + 413 * cloneLength;
  }

  @Override
  public String toString() {
    return parts.get(0).toString() + " - " + parts.get(1).toString() + " " + cloneLength;
  }
}
