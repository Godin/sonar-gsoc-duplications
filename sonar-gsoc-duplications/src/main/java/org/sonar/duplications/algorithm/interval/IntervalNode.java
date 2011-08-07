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
package org.sonar.duplications.algorithm.interval;


import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class IntervalNode<T> {

  private SortedMap<Interval<T>, List<Interval<T>>> intervals;
  private int center = 0;
  private IntervalNode<T> left = null;
  private IntervalNode<T> right = null;

  public IntervalNode() {
    intervals = new TreeMap<Interval<T>, List<Interval<T>>>();
  }

  public int getCenter() {
    return center;
  }

  public void setCenter(int center) {
    this.center = center;
  }

  public IntervalNode<T> getLeft() {
    return left;
  }

  public void setLeft(IntervalNode<T> left) {
    this.left = left;
  }

  public IntervalNode<T> getRight() {
    return right;
  }

  public void setRight(IntervalNode<T> right) {
    this.right = right;
  }

  public SortedMap<Interval<T>, List<Interval<T>>> getIntervals() {
    return intervals;
  }

  public void setIntervals(SortedMap<Interval<T>, List<Interval<T>>> intervals) {
    this.intervals = intervals;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(center + ": ");
    for (Map.Entry<Interval<T>, List<Interval<T>>> entry : intervals.entrySet()) {
      sb.append("[" + entry.getKey().getStart() + "-" + entry.getKey().getEnd() + "]:{");
      for (Interval<T> interval : entry.getValue()) {
        sb.append("(" + interval.getStart() + "-" + interval.getEnd() + ")");
      }
      sb.append("} ");
    }
    return sb.toString();
  }
}
