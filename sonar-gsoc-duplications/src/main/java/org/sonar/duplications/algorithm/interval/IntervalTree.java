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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.*;

public class IntervalTree<T> {

  private IntervalNode<T> head;
  private List<Interval<T>> intervalList;
  private boolean inSync;

  public IntervalTree() {
    SortedMap<Interval<T>, List<Interval<T>>> left = Maps.newTreeMap();
    SortedMap<Interval<T>, List<Interval<T>>> right = Maps.newTreeMap();
    this.head = new IntervalNode<T>()
        .setStartIntervals(left)
        .setEndIntervals(right);
    this.intervalList = Lists.newArrayList();
    this.inSync = true;
  }

  public List<Interval<T>> getCoveringIntervals(int start, int end) {
    build();
    List<Interval<T>> result = queryRange(head, start, end);
    // old implementation with query one point:
    // List<Interval<T>> result = Lists.newArrayList();
    // List<Interval<T>> tmp = query(head, start);
    // for (Interval interval : tmp) {
    //   if (interval.contains(end)) {
    //     result.add(interval);
    //   }
    // }
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
  void build() {
    if (!inSync) {
      head = createNode(intervalList);
      inSync = true;
    }
  }

  /**
   * @param set the set to look on
   * @return the median of the set
   */
  private int getMedian(SortedSet<Integer> set) {
    int i = 0;
    int middle = set.size() / 2;
    for (int point : set) {
      if (i == middle) {
        return point;
      }
      i++;
    }
    return 0;
  }

  private List<Interval<T>> queryRange(IntervalNode<T> node, int start, int end) {
    List<Interval<T>> result = Lists.newArrayList();

    if (start < node.getCenter()) {
      for (Map.Entry<Interval<T>, List<Interval<T>>> entry : node.getStartIntervals().entrySet()) {
        if (entry.getKey().contains(start) && entry.getKey().contains(end)) {
          for (Interval<T> interval : entry.getValue()) {
            result.add(interval);
          }
        } else if (entry.getKey().getStart() > start) {
          break;
        }
      }
    } else { // point >= node.getCenter()
      for (Map.Entry<Interval<T>, List<Interval<T>>> entry : node.getEndIntervals().entrySet()) {
        if (entry.getKey().contains(start) && entry.getKey().contains(end)) {
          for (Interval<T> interval : entry.getValue()) {
            result.add(interval);
          }
        } else if (entry.getKey().getEnd() < end) {
          break;
        }
      }
    }

    if (end < node.getCenter() && node.getLeft() != null) {
      result.addAll(queryRange(node.getLeft(), start, end));
    } else if (start > node.getCenter() && node.getRight() != null) {
      result.addAll(queryRange(node.getRight(), start, end));
    }
    return result;
  }


  /**
   * Perform a stabbing query on the node
   *
   * @param point the point to query at
   * @return all intervals containing point
   */
  private List<Interval<T>> query(IntervalNode<T> node, int point) {
    List<Interval<T>> result = Lists.newArrayList();

    if (point < node.getCenter()) {
      for (Map.Entry<Interval<T>, List<Interval<T>>> entry : node.getStartIntervals().entrySet()) {
        if (entry.getKey().contains(point)) {
          for (Interval<T> interval : entry.getValue()) {
            result.add(interval);
          }
        } else if (entry.getKey().getStart() > point) {
          break;
        }
      }
    } else { // point >= node.getCenter()
      for (Map.Entry<Interval<T>, List<Interval<T>>> entry : node.getEndIntervals().entrySet()) {
        if (entry.getKey().contains(point)) {
          for (Interval<T> interval : entry.getValue()) {
            result.add(interval);
          }
        } else if (entry.getKey().getEnd() < point) {
          break;
        }
      }
    }

    if (point < node.getCenter() && node.getLeft() != null) {
      result.addAll(query(node.getLeft(), point));
    } else if (point > node.getCenter() && node.getRight() != null) {
      result.addAll(query(node.getRight(), point));
    }
    return result;
  }


  private IntervalNode<T> createNode(List<Interval<T>> intervalList) {
    SortedMap<Interval<T>, List<Interval<T>>> startIntervals = Maps.newTreeMap(Interval.START_COMPARATOR);
    SortedMap<Interval<T>, List<Interval<T>>> endIntervals = Maps.newTreeMap(Interval.END_COMPARATOR);

    SortedSet<Integer> endpoints = Sets.newTreeSet();

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
        List<Interval<T>> current = startIntervals.get(interval);
        if (current == null) {
          current = Lists.newArrayList();
          startIntervals.put(interval, current);
          endIntervals.put(interval, current);
        }
        current.add(interval);
      }
    }

    IntervalNode<T> left = null, right = null;
    if (!leftIntervals.isEmpty()) {
      left = createNode(leftIntervals);
    }
    if (!rightIntervals.isEmpty()) {
      right = createNode(rightIntervals);
    }

    IntervalNode<T> newNode = new IntervalNode<T>()
        .setCenter(median)
        .setStartIntervals(startIntervals)
        .setEndIntervals(endIntervals)
        .setLeft(left)
        .setRight(right);
    return newNode;
  }

}
