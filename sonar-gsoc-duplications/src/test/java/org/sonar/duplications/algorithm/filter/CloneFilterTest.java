package org.sonar.duplications.algorithm.filter;

import com.google.common.collect.Lists;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.sonar.duplications.index.CloneGroup;
import org.sonar.duplications.index.ClonePair;
import org.sonar.duplications.index.ClonePart;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(value = Parameterized.class)
public class CloneFilterTest {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    CloneFilter filter1 = new BruteForceCloneFilter();
    CloneFilter filter2 = new IntervalTreeCloneFilter();
    Object[][] data = new Object[][]{{filter1}, {filter2}};
    return Arrays.asList(data);
  }

  private CloneFilter filter;

  public CloneFilterTest(CloneFilter filter) {
    this.filter = filter;
  }

  @Test
  public void emptyTest() {
    List<ClonePair> pairs = Lists.newArrayList();
    assertThat(filter.filter(pairs).size(), is(0));
  }

  @Test
  public void shouldNotFilterTest() {
    List<ClonePair> pairs = Lists.newArrayList();
    pairs.add(new ClonePair(
        new ClonePart("a", 0, 0, 5),
        new ClonePart("b", 0, 0, 5),
        1));
    pairs.add(new ClonePair(
        new ClonePart("a", 1, 1, 6),
        new ClonePart("c", 0, 0, 5),
        1));
    assertThat(filter.filter(pairs).size(), is(2));
  }

  @Test
  public void shouldFilterSimpleTest() {
    List<ClonePair> pairs = Lists.newArrayList();
    pairs.add(new ClonePair(
        new ClonePart("a", 0, 0, 5),
        new ClonePart("b", 0, 0, 5),
        10));
    pairs.add(new ClonePair(
        new ClonePart("a", 3, 3, 6),
        new ClonePart("b", 3, 3, 5),
        1));
    List<ClonePair> filtered = filter.filter(pairs);
    assertThat(filtered.size(), is(1));
    assertThat(filtered, hasItem(pairs.get(0)));
  }

  /**
   * TODO Godin: ignored due to changes for {@link CloneGroupTest#resources_are_not_the_same()}
   */
  @Ignore
  @Test
  public void shouldFilterGroupsTest() {
    List<CloneGroup> groups = Lists.newArrayList();
    ClonePart part11 = new ClonePart("a", 0, 0, 5);
    ClonePart part12 = new ClonePart("b", 0, 0, 5);
    ClonePart part13 = new ClonePart("c", 0, 0, 5);
    groups.add(new CloneGroup()
        .addPart(part11)
        .addPart(part12)
        .addPart(part13)
        .setOriginPart(part11)
        .setCloneUnitLength(10)
    );
    ClonePart part21 = new ClonePart("a", 2, 2, 7);
    ClonePart part22 = new ClonePart("b", 2, 2, 7);
    groups.add(new CloneGroup()
        .addPart(part21)
        .addPart(part22)
        .setOriginPart(part21)
        .setCloneUnitLength(2)
    );
    List<CloneGroup> filtered = filter.filter(groups);
    assertThat(filtered.size(), is(1));
    assertThat(filtered, hasItem(groups.get(0)));
  }

  @Test
  public void nestedIntervalsTest() {
    List<ClonePair> pairs = Lists.newArrayList();

    for (int i = 0; i < 100; i++) {
      pairs.add(new ClonePair(
          new ClonePart("a", i, i, i + 5),
          new ClonePart("b", i, i, i + 5),
          1));
    }
    pairs.add(new ClonePair(
        new ClonePart("a", 0, 0, 5),
        new ClonePart("b", 0, 0, 5),
        200));
    List<ClonePair> filtered = filter.filter(pairs);
    assertThat(filtered.size(), is(1));
    assertThat(filtered, hasItem(pairs.get(pairs.size() - 1)));
  }

}
