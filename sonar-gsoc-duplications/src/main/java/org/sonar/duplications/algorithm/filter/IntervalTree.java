/*
 * Sonar, open source software quality management tool.
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
package org.sonar.duplications.algorithm.filter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * Interval tree data structure implementation with <tt>addInterval</tt> and
 * <tt>getCoveringIntervals</tt> operations.
 * <p>Algorithm is adaptation of that in M.D. Berg, M.V. Kreveld,
 * M. Overmars, and O. Schwarzkopf, <I>Computational Geometry: Algorithms
 * and Applications. Springer, 1997.</I>
 * <p>Interval Tree is a binary search tree that can also be
 * used to store a dynamic set of tuples [x, y]. Each node in an
 * interval tree is associated with a key that must be covered
 * by at least one range.
 * <p>Each node is allowed to store more than one range. The number of
 * nodes in the interval tree is O(N), where N is number of ranges. To insert
 * a range R = [e; f], if R covers the key of the root, R is stored in the root.
 * Otherwise, R is inserted in the left (right) subtree of the root when f
 * is smaller (e is larger) than the key of the root. When R does not cover the
 * key of any node that is traversed, a new node with the key selected from
 * addresses e to f is created and inserted as the left or right child of the
 * node that was last visited.
 * <p>Using the interval tree organization for ranges, ranges that cover some interval
 * can be found in O(log N + k) time, where k is the number of ranges in tree that
 * cover the given interval.
 * <p><strong>Note that this implementation doesn't dynamically modify the tree.
 * On every <tt>addInterval</tt> interval is stored in list and whole tree
 * is build on next <tt>getCoveringIntervals</tt> query. The correct usage of this structure
 * is: firstly add all intervals with <tt>addInterval</tt> and then query with
 * <tt>getCoveringIntervals</tt>. Do not mix query and add calls.
 * </strong>
 *
 * @param <T> the type of data values stored in intervals
 * @author Andrew Tereskin
 */
class IntervalTree<T> {

  private IntervalNode<T> head;
  private List<Interval<T>> intervalList;
  private boolean inSync;

  /**
   * Constructs a new, empty interval tree.
   */
  public IntervalTree() {
    SortedMap<Interval<T>, List<Interval<T>>> left = Maps.newTreeMap();
    SortedMap<Interval<T>, List<Interval<T>>> right = Maps.newTreeMap();
    this.head = new IntervalNode<T>()
        .setStartIntervals(left)
        .setEndIntervals(right);
    this.intervalList = Lists.newArrayList();
    this.inSync = true;
  }

  /**
   * Perform a covering intervals search query on the tree.
   * Method runs in O(log N + k) time, where N is total number of
   * intervals in tree and k is the number of intervals in tree that cover
   * the given interval [start; end].
   *
   * @param start start point (inclusive) of interval
   * @param end   end point (inclusive) of interval
   * @return all intervals covering [start; end] interval
   */
  public List<Interval<T>> getCoveringIntervals(int start, int end) {
    build();
    return queryRange(head, start, end);
  }

  /**
   * Adds interval to the tree, but doesn't perform tree structure modification.
   * Whole tree is rebuild on next <tt>getCoveringIntervals</tt> query.
   *
   * @param interval interval to add
   */
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
   * Get center/median point from all intervals start and end points
   *
   * @param intervalList list of intervals to look at
   * @return the median of the set
   */
  private int getMedian(List<Interval<T>> intervalList) {
    SortedSet<Integer> endpoints = Sets.newTreeSet();

    for (Interval<T> interval : intervalList) {
      endpoints.add(interval.getStart());
      endpoints.add(interval.getEnd());
    }

    int i = 0;
    int middle = endpoints.size() / 2;
    int median = 0;
    for (int point : endpoints) {
      if (i == middle) {
        median = point;
        break;
      }
      i++;
    }
    return median;
  }

  /**
   * Searches in the tree structure all intervals that cover [start; end] interval.
   * Search for intervals in current node:
   * <ul>
   * <li>If (start < node.getCenter()) then search in sorted start points of
   * intervals (node.getStartIntervals()) and stop when start become less then next start point.</li>
   * <li>If (start >= node.getCenter()) then search in sorted end points of
   * intervals (node.getEndIntervals()) and stop when end become greater then next end point.</li>
   * </ul>
   * <p/>
   * Conditions to search in child nodes:
   * <ul>
   * <li>If (end < node.getCenter()) then should also search in node.getLeft() child.</li>
   * <li>If (start > node.getCenter()) then should also search in node.getRight() child.</li>
   * <li>If (start < node.getCenter()) and (end > node.getCenter()) then [start; end] can only be
   * found in current node and not its children.</li>
   * </ul>
   *
   * @param node  tree node to start search query
   * @param start start point (inclusive) of interval
   * @param end   end point (inclusive) of interval
   * @return list of found covering intervals for [start; end]
   */
  private List<Interval<T>> queryRange(IntervalNode<T> node, int start, int end) {
    List<Interval<T>> result = Lists.newArrayList();

    if (start < node.getCenter()) {
      // go through sorted start points list
      for (Map.Entry<Interval<T>, List<Interval<T>>> entry : node.getStartIntervals().entrySet()) {
        if (entry.getKey().contains(start) && entry.getKey().contains(end)) {
          for (Interval<T> interval : entry.getValue()) {
            result.add(interval);
          }
        } else if (entry.getKey().getStart() > start) {
          // as points are sorted next entries will also be (entry.getKey().getStart() > start)
          // and (entry.getKey().contains(end)) will be false for them, so break
          break;
        }
      }
    } else { // start >= node.getCenter()
      // go through sorted end points list
      for (Map.Entry<Interval<T>, List<Interval<T>>> entry : node.getEndIntervals().entrySet()) {
        if (entry.getKey().contains(start) && entry.getKey().contains(end)) {
          for (Interval<T> interval : entry.getValue()) {
            result.add(interval);
          }
        } else if (entry.getKey().getEnd() < end) {
          // as points are sorted next entries will also be (entry.getKey().getEnd() < end)
          // and (entry.getKey().contains(end)) will be false for them, so break
          break;
        }
      }
    }

    if (end < node.getCenter() && node.getLeft() != null) {
      //add results of query for left child
      result.addAll(queryRange(node.getLeft(), start, end));
    } else if (start > node.getCenter() && node.getRight() != null) {
      //add results of query for right child
      result.addAll(queryRange(node.getRight(), start, end));
    }
    return result;
  }

  /**
   * Recursive create new node and its children. Intervals stored in current node are
   * sorted by its start and end points.
   *
   * @param intervalList list of intervals to store in the tree
   * @return
   */
  private IntervalNode<T> createNode(List<Interval<T>> intervalList) {
    SortedMap<Interval<T>, List<Interval<T>>> startIntervals = Maps.newTreeMap(Interval.START_COMPARATOR);
    SortedMap<Interval<T>, List<Interval<T>>> endIntervals = Maps.newTreeMap(Interval.END_COMPARATOR);

    int median = getMedian(intervalList);

    List<Interval<T>> leftIntervals = Lists.newArrayList();
    List<Interval<T>> rightIntervals = Lists.newArrayList();

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

    return new IntervalNode<T>()
        .setCenter(median)
        .setStartIntervals(startIntervals)
        .setEndIntervals(endIntervals)
        .setLeft(left)
        .setRight(right);
  }

}
