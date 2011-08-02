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

import java.util.ArrayList;
import java.util.List;

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
    this.head = new IntervalNode<T>(intervalList);
    this.intervalList = new ArrayList<Interval<T>>();
    this.intervalList.addAll(intervalList);
    this.inSync = true;
    this.size = intervalList.size();
  }

  List<Interval<T>> getIntervals(int point) {
    build();
    return head.query(point);
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
      head = new IntervalNode<T>(intervalList);
      inSync = true;
      size = intervalList.size();
    }
  }

  @Override
  public String toString() {
    return nodeString(head, 0);
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
