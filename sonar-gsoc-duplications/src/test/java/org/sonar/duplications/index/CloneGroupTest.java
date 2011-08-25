package org.sonar.duplications.index;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;

public class CloneGroupTest {

  @Test
  public void testSorted() {
    CloneGroup group1 = spy(new CloneGroup(2));

    // should be sorted when parts list is empty
    assertThat(group1.isSorted(), is(true));

    ClonePart part11 = new ClonePart("a", 1, 1, 5);
    ClonePart part12 = new ClonePart("b", 1, 1, 5);
    group1.setOriginPart(part11)
        .addPart(part11);
    assertThat(group1.isSorted(), is(false));

    group1.getCloneParts();
    group1.getCloneParts();
    assertThat(group1.isSorted(), is(true));
    verify(group1, times(1)).sortParts();

    group1.addPart(part12);
    assertThat(group1.isSorted(), is(false));

    group1.getCloneParts();
    group1.getCloneParts();
    assertThat(group1.isSorted(), is(true));

    verify(group1, times(2)).sortParts();
  }

  /**
   * Given:
   * <pre>
   * c1: a[1-2], b[1-2]
   * c2: a[2-2], b[2-2]
   * </pre>
   * Expected: c1 in c1, c2 in c2, c1 not in c2, c2 in c1
   */
  @Test
  public void testContainsInExample1() {
    CloneGroup c1 = newCloneGroup(2,
        new ClonePart("a", 1, 1, 5),
        new ClonePart("b", 1, 1, 5));
    CloneGroup c2 = newCloneGroup(1,
        new ClonePart("a", 2, 2, 4),
        new ClonePart("b", 2, 2, 4));

    assertThat(c1.containsIn(c1), is(true));
    assertThat(c2.containsIn(c2), is(true));

    assertThat(c1.containsIn(c2), is(false));
    assertThat(c2.containsIn(c1), is(true));
  }

  /**
   * TODO Godin: I suppose that this test is correct
   * and demonstrates bug in {@link ClonePartContainerBase#containsIn(ClonePartContainerBase)},
   * which was fixed in {@link CloneGroup#containsIn(ClonePartContainerBase)}.
   * 
   * Given:
   * <pre>
   * c1: a[0-0], a[2-2], b[0-0], b[2-2]
   * c2: a[0-2], b[0-2]
   * </pre>
   * Expected:
   * <pre>
   * c1 in c2 (all parts of c1 covered by parts of c2 and all resources the same)
   * c2 not in c1 (not all parts of c2 covered by parts of c1 and all resources the same)
   * </pre>
   */
  @Test
  public void one_part_of_C2_covers_two_parts_of_C1() {
    // Note that line numbers don't matter for method which we test.
    CloneGroup c1 = newCloneGroup(1,
        new ClonePart("a", 0, 1, 5),
        new ClonePart("a", 2, 2, 7),
        new ClonePart("b", 0, 1, 5),
        new ClonePart("b", 2, 2, 7));
    CloneGroup c2 = newCloneGroup(3,
        new ClonePart("a", 0, 1, 7),
        new ClonePart("b", 0, 1, 7));

    assertThat(c1.containsIn(c2), is(true));
    assertThat(c2.containsIn(c1), is(false));
  }

  /**
   * Given:
   * <pre>
   * c1: a[0-0], a[2-2]
   * c2: a[0-2], b[0-2]
   * </pre>
   * Expected:
   * <pre>
   * c1 not in c2 (all parts of c1 covered by parts of c2 and resources not the same)
   * c2 not in c1 (not all parts of c2 covered by parts of c1 and resources not the same)
   * </pre>
   */
  @Test
  public void resources_are_not_the_same() {
    CloneGroup c1 = newCloneGroup(1,
        new ClonePart("a", 0, 1, 5),
        new ClonePart("a", 2, 2, 7));
    CloneGroup c2 = newCloneGroup(3,
        new ClonePart("a", 0, 1, 7),
        new ClonePart("b", 0, 1, 7));

    assertThat(c1.containsIn(c2), is(false));
    assertThat(c2.containsIn(c1), is(false));
  }

  private CloneGroup newCloneGroup(int len, ClonePart... parts) {
    CloneGroup group = new CloneGroup().setCloneUnitLength(len);
    group.setOriginPart(parts[0]);
    for (ClonePart part : parts) {
      group.addPart(part);
    }
    return group;
  }
}
