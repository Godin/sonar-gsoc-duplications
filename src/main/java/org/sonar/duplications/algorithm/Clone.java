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

public class Clone {

  private static final class ClonePart {
    String resourceId;
    int unitStart;
    int lineStart;
    int lineEnd;
  }

  private final ClonePart first = new ClonePart();
  private final ClonePart second = new ClonePart();

  //clone length in units (not lines)
  private int cloneLength;

  public Clone() {
  }

  public Clone(String firstFile, int firstUnitStart, int firstLineStart, int firstLineEnd,
               String secondFile, int secondUnitStart, int secondLineStart, int secondLineEnd,
               int cloneLength) {
    this.first.resourceId = firstFile;
    this.first.unitStart = firstUnitStart;
    this.first.lineStart = firstLineStart;
    this.first.lineEnd = firstLineEnd;
    this.second.resourceId = secondFile;
    this.second.unitStart = secondUnitStart;
    this.second.lineStart = secondLineStart;
    this.second.lineEnd = secondLineEnd;
    this.cloneLength = cloneLength;
  }

  public String getFirstResourceId() {
    return first.resourceId;
  }

  public void setFirstResourceId(String firstResourceId) {
    this.first.resourceId = firstResourceId;
  }

  public String getSecondResourceId() {
    return second.resourceId;
  }

  public void setSecondResourceId(String secondResourceId) {
    this.second.resourceId = secondResourceId;
  }

  public int getFirstUnitStart() {
    return first.unitStart;
  }

  public void setFirstUnitStart(int firstUnitStart) {
    this.first.unitStart = firstUnitStart;
  }

  public int getSecondUnitStart() {
    return second.unitStart;
  }

  public void setSecondUnitStart(int secondUnitStart) {
    this.second.unitStart = secondUnitStart;
  }

  public int getFirstLineStart() {
    return first.lineStart;
  }

  public void setFirstLineStart(int firstLineStart) {
    this.first.lineStart = firstLineStart;
  }

  public int getSecondLineStart() {
    return second.lineStart;
  }

  public void setSecondLineStart(int secondLineStart) {
    this.second.lineStart = secondLineStart;
  }

  public int getFirstLineEnd() {
    return first.lineEnd;
  }

  public void setFirstLineEnd(int firstLineEnd) {
    this.first.lineEnd = firstLineEnd;
  }

  public int getSecondLineEnd() {
    return second.lineEnd;
  }

  public void setSecondLineEnd(int secondLineEnd) {
    this.second.lineEnd = secondLineEnd;
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
      return another.first.resourceId.equals(first.resourceId)
          && another.first.unitStart == first.unitStart
          && another.first.lineStart == first.lineStart
          && another.first.lineEnd == first.lineEnd
          && another.second.resourceId.equals(second.resourceId)
          && another.second.unitStart == second.unitStart
          && another.second.lineStart == second.lineStart
          && another.second.lineEnd == second.lineEnd
          && another.cloneLength == cloneLength;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return first.resourceId.hashCode() + second.resourceId.hashCode() +
        first.unitStart + second.unitStart + first.lineStart +
        first.lineEnd + second.lineStart + second.lineEnd + 413 * cloneLength;
  }

  @Override
  public String toString() {
    return "'" + first.resourceId + "':[" + first.unitStart + "|" +
        first.lineStart + "-" + first.lineEnd + "] - '" +
        second.resourceId + "':[" + second.unitStart + "|" +
        second.lineStart + "-" + second.lineEnd + "] " + cloneLength;
  }
}
