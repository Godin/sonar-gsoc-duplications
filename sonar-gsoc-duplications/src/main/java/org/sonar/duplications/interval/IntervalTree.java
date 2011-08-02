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

public class IntervalTree<T> {

  private IntervalNode<T> head;
  private List<Interval<T>> intervalList;
  private boolean inSync;
  private int size;

  public IntervalTree() {
    this.head = new IntervalNode<T>();
    this.intervalList = new ArrayList<Interval<T>>();
    this.inSync = true;
    this.size = 0;
  }

  public IntervalTree(List<Interval<T>> intervalList) {
    this.head = createNode(intervalList);
    this.intervalList = new ArrayList<Interval<T>>();
    this.intervalList.addAll(intervalList);
    this.inSync = true;
    this.size = intervalList.size();
  }

  List<Interval<T>> getIntervals(int point) {
    build();
    return query(head, point);
  }

  public List<Interval<T>> getCoveringIntervals(int start, int end) {
    List<Interval<T>> result = new ArrayList<Interval<T>>();
    List<Interval<T>> intervals = getIntervals(start);

    for (Interval<T> interval : intervals)
      if (interval.contains(end))
        result.add(interval);

    return result;
  }

  public void addInterval(Interval<T> interval) {
    intervalList.add(interval);
    inSync = false;
  }

  /**
   * Build the interval tree to reflect the list of intervals,
   * Will not run if this is currently in sync
   */
  public void build() {
    if (!inSync) {
      head = createNode(intervalList);
      inSync = true;
      size = intervalList.size();
    }
  }

  @Override
  public String toString() {
    return nodeString(head, 0);
  }

  /**
   * @param set the set to look on
   * @return the median of the set
   */
  private int getMedian(SortedSet<Integer> set) {
    int i = 0;
    int middle = set.size() / 2;
    for (int point : set) {
      if (i == middle)
        return point;
      i++;
    }
    return 0;
  }

  /**
   * Perform a stabbing query on the node
   *
   * @param point the point to query at
   * @return all intervals containing point
   */
  public List<Interval<T>> query(IntervalNode<T> node, int point) {
    List<Interval<T>> result = new ArrayList<Interval<T>>();

    for (Map.Entry<Interval<T>, List<Interval<T>>> entry : node.getIntervals().entrySet()) {
      if (entry.getKey().contains(point))
        for (Interval<T> interval : entry.getValue())
          result.add(interval);
      else if (entry.getKey().getStart() > point)
        break;
    }

    if (point < node.getCenter() && node.getLeft() != null)
      result.addAll(query(node.getLeft(), point));
    else if (point > node.getCenter() && node.getRight() != null)
      result.addAll(query(node.getRight(), point));
    return result;
  }


  private IntervalNode<T> createNode(List<Interval<T>> intervalList) {
    SortedMap<Interval<T>, List<Interval<T>>> intervals = new TreeMap<Interval<T>, List<Interval<T>>>();

    SortedSet<Integer> endpoints = new TreeSet<Integer>();

    for (Interval<T> interval : intervalList) {
      endpoints.add(interval.getStart());
      endpoints.add(interval.getEnd());
    }

    int median = getMedian(endpoints);

    List<Interval<T>> leftIntervals = new ArrayList<Interval<T>>();
    List<Interval<T>> rightIntervals = new ArrayList<Interval<T>>();

    for (Interval<T> interval : intervalList) {
      if (interval.getEnd() < median) {
        leftIntervals.add(interval);
      } else if (interval.getStart() > median) {
        rightIntervals.add(interval);
      } else {
        List<Interval<T>> current = intervals.get(interval);
        if (current == null) {
          current = new ArrayList<Interval<T>>();
          intervals.put(interval, current);
        }
        current.add(interval);
      }
    }

    IntervalNode<T> left = null, right = null;
    if (leftIntervals.size() > 0) {
      left = createNode(leftIntervals);
    }
    if (rightIntervals.size() > 0) {
      right = createNode(rightIntervals);
    }

    IntervalNode<T> newNode = new IntervalNode<T>();
    newNode.setCenter(median);
    newNode.setIntervals(intervals);
    newNode.setLeft(left);
    newNode.setRight(right);
    return newNode;
  }


  private String nodeString(IntervalNode<T> node, int level) {
    if (node == null)
      return "";

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < level; i++)
      sb.append("\t");
    sb.append(node + "\n");
    sb.append(nodeString(node.getLeft(), level + 1));
    sb.append(nodeString(node.getRight(), level + 1));
    return sb.toString();
  }

}
