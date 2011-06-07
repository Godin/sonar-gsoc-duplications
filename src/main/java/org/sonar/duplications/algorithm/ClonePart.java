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
package org.sonar.duplications.algorithm;

public class ClonePart implements Comparable<ClonePart>{

  private String resourceId;
  private int unitStart;
  private int lineStart;
  private int lineEnd;

  public ClonePart() {
  }

  public ClonePart(String resourceId, int unitStart, int lineStart, int lineEnd) {
    this.resourceId = resourceId;
    this.unitStart = unitStart;
    this.lineStart = lineStart;
    this.lineEnd = lineEnd;
  }

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public int getUnitStart() {
    return unitStart;
  }

  public void setUnitStart(int unitStart) {
    this.unitStart = unitStart;
  }

  public int getLineStart() {
    return lineStart;
  }

  public void setLineStart(int lineStart) {
    this.lineStart = lineStart;
  }

  public int getLineEnd() {
    return lineEnd;
  }

  public void setLineEnd(int lineEnd) {
    this.lineEnd = lineEnd;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ClonePart) {
      ClonePart another = (ClonePart) obj;
      return another.resourceId.equals(resourceId)
          && another.lineStart == lineStart
          && another.lineEnd == lineEnd
          && another.unitStart == unitStart;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return resourceId.hashCode() + 13 * lineStart + 31 * lineEnd + 413 * unitStart;
  }

  @Override
  public String toString() {
    return "'" + resourceId + "':[" + unitStart + "|" + lineStart + "-" + lineEnd + "]";
  }

  public int compareTo(ClonePart o) {
    if (resourceId.equals(o.resourceId)) {
      return unitStart - o.unitStart;
    }
    return resourceId.compareTo(o.resourceId);
  }
}
