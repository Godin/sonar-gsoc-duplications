package org.sonar.duplications.algorithm.interval;


import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;


public class IntervalTreeTest {
  private IntervalTree tree;

  @Before
  public void setUp() {
    this.tree = new IntervalTree();
  }

  @Test
  public void emptyTreeTest() {
    assertThat(this.tree.getCoveringIntervals(1, 5).size(), is(0));
  }

  @Test
  public void testSimpleCoverEqual() {
    tree.addInterval(new Interval(1, 5, null));
    List<Interval> intervals = tree.getCoveringIntervals(1, 5);
    assertThat(intervals, hasItem(new Interval(1, 5, null)));
  }

  @Test
  public void testSimpleCoverEnclosed() {
    tree.addInterval(new Interval(1, 5, null));
    List<Interval> intervals = tree.getCoveringIntervals(2, 4);
    assertThat(intervals, hasItem(new Interval(1, 5, null)));
  }

  @Test
  public void testSimpleCoverRight() {
    tree.addInterval(new Interval(1, 5, null));
    List<Interval> intervals = tree.getCoveringIntervals(3, 5);
    assertThat(intervals, hasItem(new Interval(1, 5, null)));
  }

  @Test
  public void testSimpleCoverLeft() {
    tree.addInterval(new Interval(1, 5, null));
    List<Interval> intervals = tree.getCoveringIntervals(1, 3);
    assertThat(intervals, hasItem(new Interval(1, 5, null)));
  }

  @Test
  public void testSimpleNotCoverLeft() {
    tree.addInterval(new Interval(5, 10, null));
    assertThat(tree.getCoveringIntervals(0, 3).size(), is(0));
    assertThat(tree.getCoveringIntervals(2, 7).size(), is(0));
  }

  @Test
  public void testSimpleNotCoverRight() {
    tree.addInterval(new Interval(5, 10, null));
    assertThat(tree.getCoveringIntervals(11, 13).size(), is(0));
    assertThat(tree.getCoveringIntervals(8, 12).size(), is(0));
  }


  private Interval searchInterval(int low, int high) {
    List<Interval> intervals = tree.getCoveringIntervals(low, high);
    if (!intervals.isEmpty()) {
      return intervals.get(0);
    }
    return null;
  }


  private List<Interval> searchAllIntervals(int low, int high) {
    List<Interval> intervals = tree.getCoveringIntervals(low, high);
    java.util.Collections.sort(intervals);
    return intervals;
  }

  private void prepareTestCaseTree() {
    tree.addInterval(new Interval(0, 3, null));
    tree.addInterval(new Interval(5, 8, null));
    tree.addInterval(new Interval(6, 10, null));
    tree.addInterval(new Interval(8, 9, null));
    tree.addInterval(new Interval(15, 23, null));
    tree.addInterval(new Interval(16, 21, null));
    tree.addInterval(new Interval(17, 19, null));
    tree.addInterval(new Interval(19, 20, null));
    tree.addInterval(new Interval(25, 30, null));
    tree.addInterval(new Interval(26, 26, null));
  }

  @Test
  public void testWithPreparedTree() {
    prepareTestCaseTree();
    assertThat(searchInterval(1, 3), is(new Interval(0, 3, null)));
    assertThat(searchInterval(15, 16), is(new Interval(15, 23, null)));
    assertNull(searchInterval(14, 16));
    assertThat(searchInterval(26, 27), is(new Interval(25, 30, null)));
  }

  @Test
  public void testMultipleSearch() {
    prepareTestCaseTree();
    List<Interval> intervals = searchAllIntervals(6, 8);
    assertThat(intervals.size(), is(2));
    assertThat(intervals, hasItem(new Interval(5, 8, null)));
    assertThat(intervals, hasItem(new Interval(6, 10, null)));
  }

  @Test
  public void testSingleMatchWithMultipleSearch() {
    prepareTestCaseTree();
    List<Interval> intervals = searchAllIntervals(15, 20);
    assertThat(intervals.size(), is(1));
    assertThat(intervals, hasItem(new Interval(15, 23, null)));
  }


  @Test
  public void testSingleMatchWithMultipleSearchAndDuplicates() {
    prepareTestCaseTree();
    prepareTestCaseTree();
    prepareTestCaseTree();
    List expected = new ArrayList();
    expected.add(new Interval(15, 23, null));
    expected.add(new Interval(15, 23, null));
    expected.add(new Interval(15, 23, null));
    assertThat(searchAllIntervals(15, 20), is(expected));
  }

  @Test
  public void testOverlapping() {
    List expected = new ArrayList();
    int bignumber = 30000;
    for (int i = 0; i < bignumber; i++) {
      tree.addInterval(new Interval(0, bignumber + i, null));
      expected.add(new Interval(0, bignumber + i, null));
    }

    assertThat(searchAllIntervals(0, bignumber - 1), is(expected));

    assertThat(searchAllIntervals(0, bignumber / 2), is(expected));

    assertThat(searchAllIntervals(0, bignumber / 3), is(expected));
  }

}
