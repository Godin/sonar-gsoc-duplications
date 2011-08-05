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

class CloneKey implements Comparable<CloneKey> {
  private String resourceId;
  private int unitNum;

  CloneKey(String resourceId, int unitNum) {
    this.resourceId = resourceId;
    this.unitNum = unitNum;
  }

  public int compareTo(CloneKey o) {
    if (this.resourceId.equals(o.resourceId)) {
      return this.unitNum - o.unitNum;
    }
    return this.resourceId.compareTo(o.resourceId);
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof CloneKey) {
      CloneKey other = (CloneKey) object;
      if (other.resourceId.equals(resourceId) && other.unitNum == unitNum) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h = 0;
    h = 31 * h + resourceId.hashCode();
    h = 31 * h + unitNum;
    return h;
  }
}
