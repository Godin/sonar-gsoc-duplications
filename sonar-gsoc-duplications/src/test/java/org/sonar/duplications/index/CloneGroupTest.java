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
package org.sonar.duplications.index;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

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
        newClonePart("a", 1),
        newClonePart("b", 1));
    CloneGroup c2 = newCloneGroup(1,
        newClonePart("a", 2),
        newClonePart("b", 2));

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
        newClonePart("a", 0),
        newClonePart("a", 2),
        newClonePart("b", 0),
        newClonePart("b", 2));
    CloneGroup c2 = newCloneGroup(3,
        newClonePart("a", 0),
        newClonePart("b", 0));

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
   * c1 not in c2 (all parts of c1 covered by parts of c2, but different resources)
   * c2 not in c1 (not all parts of c2 covered by parts of c1 and different resources)
   * </pre>
   */
  @Test
  public void different_resources() {
    CloneGroup c1 = newCloneGroup(1,
        newClonePart("a", 0),
        newClonePart("a", 2));
    CloneGroup c2 = newCloneGroup(3,
        newClonePart("a", 0),
        newClonePart("b", 0));

    assertThat(c1.containsIn(c2), is(false));
    assertThat(c2.containsIn(c1), is(false));
  }

  /**
   * Given:
   * <pre>
   * c1: a[2-2]
   * c2: a[0-1], a[2-3]
   * </pre>
   * Expected:
   * <pre>
   * c1 in c2
   * c2 not in c1
   * </pre>
   */
  @Test
  public void second_part_of_C2_covers_first_part_of_C1() {
    CloneGroup c1 = newCloneGroup(1,
        newClonePart("a", 2));
    CloneGroup c2 = newCloneGroup(2,
        newClonePart("a", 0),
        newClonePart("a", 2));

    assertThat(c1.containsIn(c2), is(true));
    assertThat(c2.containsIn(c1), is(false));
  }

  /**
   * Given:
   * <pre>
   * c1: a[0-0], b[0-0]
   * c2: b[0-0], a[0-0]
   * </pre>
   * Expected:
   * <pre>
   * c1 not in c2
   * </pre>
   * because of different origins
   */
  @Test
  public void different_origins() {
    CloneGroup c1 = spy(newCloneGroup(1,
        newClonePart("a", 0),
        newClonePart("b", 0)));
    CloneGroup c2 = spy(newCloneGroup(1,
        newClonePart("b", 0),
        newClonePart("a", 0)));

    assertThat(c1.containsIn(c2), is(false));
    verify(c1).containsIn(c2);
    // containsIn method should check only resourceId of origins - no need to compare all parts
    verify(c1).getOriginPart();
    verify(c2).getOriginPart();
    verifyNoMoreInteractions(c1);
    verifyNoMoreInteractions(c2);
  }

  /**
   * Given:
   * <pre>
   * c1: a[0-2]
   * c2: a[0-0]
   * </pre>
   * Expected:
   * <pre>
   * c1 not in c2
   * </pre>
   */
  @Test
  public void length_of_C1_bigger_than_length_of_C2() {
    CloneGroup c1 = spy(newCloneGroup(3,
        newClonePart("a", 0)));
    CloneGroup c2 = spy(newCloneGroup(1,
        newClonePart("a", 0)));

    assertThat(c1.containsIn(c2), is(false));
    verify(c1).containsIn(c2);
    // containsIn method should check only origin and length - no need to compare all parts
    verify(c1).getOriginPart();
    verify(c2).getOriginPart();
    verify(c1).getCloneUnitLength();
    verify(c2).getCloneUnitLength();
    verifyNoMoreInteractions(c1);
    verifyNoMoreInteractions(c2);
  }

  /**
   * Creates new part with specified resourceId and unitStart, and 0 for lineStart and lineEnd.
   */
  private ClonePart newClonePart(String resourceId, int unitStart) {
    return new ClonePart(resourceId, unitStart, 0, 0);
  }

  /**
   * Creates new group from list of parts, origin - is a first part from list.
   */
  private CloneGroup newCloneGroup(int len, ClonePart... parts) {
    CloneGroup group = new CloneGroup().setCloneUnitLength(len);
    group.setOriginPart(parts[0]);
    for (ClonePart part : parts) {
      group.addPart(part);
    }
    return group;
  }
}
