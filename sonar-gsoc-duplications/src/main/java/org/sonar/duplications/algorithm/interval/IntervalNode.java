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
import java.util.SortedMap;

public class IntervalNode<T> {

  private SortedMap<Interval<T>, List<Interval<T>>> startIntervals;
  private SortedMap<Interval<T>, List<Interval<T>>> endIntervals;
  private int center = 0;
  private IntervalNode<T> left = null;
  private IntervalNode<T> right = null;

  public int getCenter() {
    return center;
  }

  public IntervalNode<T> setCenter(int center) {
    this.center = center;
    return this;
  }

  public IntervalNode<T> getLeft() {
    return left;
  }

  public IntervalNode<T> setLeft(IntervalNode<T> left) {
    this.left = left;
    return this;
  }

  public IntervalNode<T> getRight() {
    return right;
  }

  public IntervalNode<T> setRight(IntervalNode<T> right) {
    this.right = right;
    return this;
  }

  public SortedMap<Interval<T>, List<Interval<T>>> getStartIntervals() {
    return startIntervals;
  }

  public IntervalNode<T> setStartIntervals(SortedMap<Interval<T>, List<Interval<T>>> startIntervals) {
    this.startIntervals = startIntervals;
    return this;
  }

  public SortedMap<Interval<T>, List<Interval<T>>> getEndIntervals() {
    return endIntervals;
  }

  public IntervalNode<T> setEndIntervals(SortedMap<Interval<T>, List<Interval<T>>> endIntervals) {
    this.endIntervals = endIntervals;
    return this;
  }
}
