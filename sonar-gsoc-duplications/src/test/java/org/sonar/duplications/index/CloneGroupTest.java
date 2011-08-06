package org.sonar.duplications.index;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class CloneGroupTest {

  @Test
  public void testSorted() {
    CloneGroup group1 = spy(new CloneGroup(2));

    //should be sorted when parts list is empty
    assertThat(group1.isSorted(), is(true));

    ClonePart part11 = new ClonePart("a", 1, 1, 5);
    ClonePart part12 = new ClonePart("b", 1, 1, 5);
    group1.setOriginPart(part11);
    group1.addPart(part11);
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
    CloneGroup group1 = new CloneGroup(2);
    ClonePart part11 = new ClonePart("a", 1, 1, 5);
    ClonePart part12 = new ClonePart("b", 1, 1, 5);
    group1.setOriginPart(part11);
    group1.addPart(part11);
    group1.addPart(part12);

    CloneGroup group2 = new CloneGroup(1);
    ClonePart part21 = new ClonePart("a", 2, 2, 4);
    ClonePart part22 = new ClonePart("b", 2, 2, 4);
    group2.setOriginPart(part11);
    group2.addPart(part21);
    group2.addPart(part22);

    assertThat(group1.containsIn(group1), is(true));
    assertThat(group2.containsIn(group2), is(true));

    assertThat(group1.containsIn(group2), is(false));
    assertThat(group2.containsIn(group1), is(true));
  }

  @Test
  public void testContainsInExample2() {
    CloneGroup group1 = new CloneGroup(2);
    ClonePart part11 = new ClonePart("a", 1, 1, 2);
    ClonePart part12 = new ClonePart("b", 1, 1, 2);
    ClonePart part13 = new ClonePart("c", 1, 1, 2);
    group1.setOriginPart(part11);
    group1.addPart(part11);
    group1.addPart(part12);
    group1.addPart(part13);

    CloneGroup group2 = new CloneGroup(1);
    ClonePart part21 = new ClonePart("a", 1, 1, 2);
    ClonePart part22 = new ClonePart("c", 1, 1, 2);
    group2.setOriginPart(part11);
    group2.addPart(part21);
    group2.addPart(part22);

    assertThat(group2.containsIn(group1), is(true));
  }
}
