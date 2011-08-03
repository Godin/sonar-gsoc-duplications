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
package org.sonar.duplications.interval;

public class Interval<T> implements Comparable<Interval<T>> {
  private int start;
  private int end;
  private T data;

  public Interval(int start, int end, T data) {
    this.start = start;
    this.end = end;
    this.data = data;
  }

  public int getStart() {
    return start;
  }

  public void setStart(int start) {
    this.start = start;
  }

  public int getEnd() {
    return end;
  }

  public void setEnd(int end) {
    this.end = end;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  public boolean contains(int point) {
    return point <= end && point >= start;
  }

  public boolean intersects(Interval<?> other) {
    return other.getEnd() >= start && other.getStart() <= end;
  }

  public int compareTo(Interval<T> other) {
    if (start < other.getStart())
      return -1;
    else if (start > other.getStart())
      return 1;
    else if (end < other.getEnd())
      return -1;
    else if (end > other.getEnd())
      return 1;
    else
      return 0;
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof Interval) {
      Interval other = (Interval) object;

      if (other.getStart() == start && other.getEnd() == end) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h = 0;
    h = 31 * h + start;
    h = 31 * h + end;
    return h;
  }
}
