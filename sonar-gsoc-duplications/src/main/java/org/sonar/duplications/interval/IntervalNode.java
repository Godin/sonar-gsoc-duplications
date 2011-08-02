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


import java.util.*;

public class IntervalNode<T> {

  private SortedMap<Interval<T>, List<Interval<T>>> intervals;
  private int center = 0;
  private IntervalNode<T> left = null;
  private IntervalNode<T> right = null;

  public IntervalNode() {
    intervals = new TreeMap<Interval<T>, List<Interval<T>>>();
  }

  public IntervalNode(List<Interval<T>> intervalList) {
    intervals = new TreeMap<Interval<T>, List<Interval<T>>>();

    SortedSet<Integer> endpoints = new TreeSet<Integer>();

    for (Interval<T> interval : intervalList) {
      endpoints.add(interval.getStart());
      endpoints.add(interval.getEnd());
    }

    int median = getMedian(endpoints);
    center = median;

    List<Interval<T>> leftIntervals = new ArrayList<Interval<T>>();
    List<Interval<T>> rightIntervals = new ArrayList<Interval<T>>();

    for (Interval<T> interval : intervalList) {
      if (interval.getEnd() < median)
        leftIntervals.add(interval);
      else if (interval.getStart() > median)
        rightIntervals.add(interval);
      else {
        List<Interval<T>> current = intervals.get(interval);
        if (current == null) {
          current = new ArrayList<Interval<T>>();
          intervals.put(interval, current);
        }
        current.add(interval);
      }
    }

    if (leftIntervals.size() > 0)
      this.left = new IntervalNode<T>(leftIntervals);
    if (rightIntervals.size() > 0)
      this.right = new IntervalNode<T>(rightIntervals);
  }

  /**
   * Perform a stabbing query on the node
   *
   * @param point the point to query at
   * @return all intervals containing point
   */
  public List<Interval<T>> query(int point) {
    List<Interval<T>> result = new ArrayList<Interval<T>>();

    for (Map.Entry<Interval<T>, List<Interval<T>>> entry : intervals.entrySet()) {
      if (entry.getKey().contains(point))
        for (Interval<T> interval : entry.getValue())
          result.add(interval);
      else if (entry.getKey().getStart() > point)
        break;
    }

    if (point < center && left != null)
      result.addAll(left.query(point));
    else if (point > center && right != null)
      result.addAll(right.query(point));
    return result;
  }

  public int getCenter
      () {
    return center;
  }

  public void setCenter
      (
          int center) {
    this.center = center;
  }

  public IntervalNode<T> getLeft
      () {
    return left;
  }

  public void setLeft
      (IntervalNode<T> left) {
    this.left = left;
  }

  public IntervalNode<T> getRight
      () {
    return right;
  }

  public void setRight
      (IntervalNode<T> right) {
    this.right = right;
  }

  /**
   * @param set the set to look on
   * @return the median of the set
   */
  private int getMedian
  (SortedSet<Integer> set) {
    int i = 0;
    int middle = set.size() / 2;
    for (int point : set) {
      if (i == middle)
        return point;
      i++;
    }
    return 0;
  }

  @Override
  public String toString
      () {
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
