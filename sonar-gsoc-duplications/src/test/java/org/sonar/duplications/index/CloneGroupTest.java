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

  @Test
  public void testContainsInExample1() {
    ClonePart part11 = new ClonePart("a", 1, 1, 5);
    ClonePart part12 = new ClonePart("b", 1, 1, 5);
    CloneGroup group1 = new CloneGroup()
        .setCloneUnitLength(2)
        .setOriginPart(part11)
        .addPart(part11)
        .addPart(part12);

    ClonePart part21 = new ClonePart("a", 2, 2, 4);
    ClonePart part22 = new ClonePart("b", 2, 2, 4);
    CloneGroup group2 = new CloneGroup(1)
        .setCloneUnitLength(1)
        .setOriginPart(part21)
        .addPart(part21)
        .addPart(part22);

    assertThat(group1.containsIn(group1), is(true));
    assertThat(group2.containsIn(group2), is(true));

    assertThat(group1.containsIn(group2), is(false));
    assertThat(group2.containsIn(group1), is(true));
  }

  @Test
  public void testContainsInExample2() {
    ClonePart part11 = new ClonePart("a", 1, 1, 2);
    ClonePart part12 = new ClonePart("b", 1, 1, 2);
    ClonePart part13 = new ClonePart("c", 1, 1, 2);
    CloneGroup group1 = new CloneGroup()
        .setCloneUnitLength(2)
        .setOriginPart(part11)
        .addPart(part11)
        .addPart(part12)
        .addPart(part13);

    ClonePart part21 = new ClonePart("a", 1, 1, 2);
    ClonePart part22 = new ClonePart("c", 1, 1, 2);
    CloneGroup group2 = new CloneGroup()
        .setCloneUnitLength(1)
        .setOriginPart(part11)
        .addPart(part21)
        .addPart(part22);

    assertThat(group2.containsIn(group1), is(true));
    assertThat(group1.containsIn(group2), is(false));
  }

  /**
   * TODO Godin: I suppose that this test is correct
   * and demonstrates bug in {@link ClonePartContainerBase#containsIn(ClonePartContainerBase)},
   * which was fixed in {@link CloneGroup#containsIn(ClonePartContainerBase)}.
   */
  @Test
  public void one_part_of_B_covers_two_parts_of_A() {
    // Note that line numbers don't matter for method which we test.
    CloneGroup a = newCloneGroup(1,
        new ClonePart("a", 0, 1, 5),
        new ClonePart("a", 2, 2, 7),
        new ClonePart("b", 0, 1, 5),
        new ClonePart("b", 2, 2, 7));
    CloneGroup b = newCloneGroup(3,
        new ClonePart("a", 0, 1, 7),
        new ClonePart("b", 0, 1, 7));

    assertThat(a.containsIn(b), is(true));
    assertThat("antisymmetric relation", b.containsIn(a), is(false));
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
