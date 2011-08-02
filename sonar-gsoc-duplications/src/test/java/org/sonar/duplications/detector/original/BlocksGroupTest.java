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
package org.sonar.duplications.detector.original;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;
import org.sonar.duplications.block.Block;
import org.sonar.duplications.detector.original.BlocksGroup;

public class BlocksGroupTest {

  /**
   * {@link BlocksGroup} uses only resourceId and index from block, thus we can simplify testing.
   */
  private static Block newBlock(String resourceId, int indexInFile) {
    return new Block(resourceId, null, indexInFile, indexInFile, indexInFile);
  }

  @Test
  public void shouldReturnSize() {
    BlocksGroup group = new BlocksGroup(Arrays.asList(newBlock("a", 1), newBlock("b", 2)));
    assertThat(group.size(), is(2));
  }

  @Test
  public void shouldCreateEmptyGroup() {
    assertThat(BlocksGroup.empty().size(), is(0));
  }

  @Test
  public void testSubsumedBy() {
    BlocksGroup group1 = new BlocksGroup(Arrays.asList(newBlock("a", 1), newBlock("b", 2)));
    BlocksGroup group2 = new BlocksGroup(Arrays.asList(newBlock("a", 2), newBlock("b", 3), newBlock("c", 4)));
    assertThat(group2.subsumedBy(group1), is(false));
  }

  @Test
  public void testSubsumedBy2() {
    BlocksGroup group1 = new BlocksGroup(Arrays.asList(newBlock("a", 1), newBlock("b", 2)));
    BlocksGroup group2 = new BlocksGroup(Arrays.asList(newBlock("a", 2), newBlock("b", 3)));
    assertThat(group2.subsumedBy(group1), is(true));
  }

  @Test
  public void testIntersect() {
    BlocksGroup group1 = new BlocksGroup(Arrays.asList(newBlock("a", 1), newBlock("b", 2)));
    BlocksGroup group2 = new BlocksGroup(Arrays.asList(newBlock("a", 2), newBlock("b", 3)));
    BlocksGroup intersection = group1.intersect(group2);
    assertThat(intersection.size(), is(2));
  }

  /**
   * Results for this test taken from results of work of {@link NaiveBlocksGroup naive implementation}.
   */
  @Test
  public void testSubsumedBy3() {
    // ['a'[2|2-7]:3, 'b'[0|0-5]:3] subsumedBy ['a'[1|1-6]:2] false
    assertThat(new BlocksGroup(Arrays.asList(newBlock("a", 2), newBlock("b", 0)))
        .subsumedBy(new BlocksGroup(Arrays.asList(newBlock("a", 1)))),
        is(false));

    // ['a'[3|3-8]:4, 'b'[1|1-6]:4] subsumedBy ['a'[1|1-6]:2] false
    assertThat(new BlocksGroup(Arrays.asList(newBlock("a", 3), newBlock("b", 1)))
        .subsumedBy(new BlocksGroup(Arrays.asList(newBlock("a", 1)))),
        is(false));

    // ['a'[4|4-9]:5, 'b'[2|2-7]:5] subsumedBy ['a'[1|1-6]:2] false
    assertThat(new BlocksGroup(Arrays.asList(newBlock("a", 4), newBlock("b", 2)))
        .subsumedBy(new BlocksGroup(Arrays.asList(newBlock("a", 1)))),
        is(false));

    // ['a'[5|5-10]:6, 'b'[3|3-8]:6] subsumedBy ['a'[1|1-6]:2] false
    assertThat(new BlocksGroup(Arrays.asList(newBlock("a", 5), newBlock("b", 3)))
        .subsumedBy(new BlocksGroup(Arrays.asList(newBlock("a", 1)))), is(false));

    // ['a'[3|3-8]:4, 'b'[1|1-6]:4] subsumedBy ['a'[2|2-7]:3, 'b'[0|0-5]:3] true
    assertThat(new BlocksGroup(Arrays.asList(newBlock("a", 3), newBlock("b", 1)))
        .subsumedBy(new BlocksGroup(Arrays.asList(newBlock("a", 2), newBlock("b", 0)))),
        is(true));

    // ['a'[4|4-9]:5, 'b'[2|2-7]:5, 'c'[0|0-5]:5] subsumedBy ['a'[3|3-8]:4, 'b'[1|1-6]:4] false
    assertThat(new BlocksGroup(Arrays.asList(newBlock("a", 4), newBlock("b", 2), newBlock("c", 0)))
        .subsumedBy(new BlocksGroup(Arrays.asList(newBlock("a", 3), newBlock("b", 1)))),
        is(false));

    // ['a'[5|5-10]:6, 'b'[3|3-8]:6, 'c'[1|1-6]:6] subsumedBy ['a'[3|3-8]:4, 'b'[1|1-6]:4] false
    assertThat(new BlocksGroup(Arrays.asList(newBlock("a", 5), newBlock("b", 3), newBlock("c", 1)))
        .subsumedBy(new BlocksGroup(Arrays.asList(newBlock("a", 3), newBlock("b", 1)))),
        is(false));

    // ['a'[6|6-11]:7, 'c'[2|2-7]:7] subsumedBy ['a'[3|3-8]:4, 'b'[1|1-6]:4] false
    assertThat(new BlocksGroup(Arrays.asList(newBlock("a", 6), newBlock("c", 2)))
        .subsumedBy(new BlocksGroup(Arrays.asList(newBlock("a", 3), newBlock("b", 1)))),
        is(false));

    // ['a'[5|5-10]:6, 'b'[3|3-8]:6, 'c'[1|1-6]:6] subsumedBy ['a'[4|4-9]:5, 'b'[2|2-7]:5, 'c'[0|0-5]:5] true
    assertThat(new BlocksGroup(Arrays.asList(newBlock("a", 5), newBlock("b", 3), newBlock("c", 1)))
        .subsumedBy(new BlocksGroup(Arrays.asList(newBlock("a", 4), newBlock("b", 2), newBlock("c", 0)))),
        is(true));

    // ['a'[6|6-11]:7, 'c'[2|2-7]:7] subsumedBy ['a'[5|5-10]:6, 'b'[3|3-8]:6, 'c'[1|1-6]:6] true
    assertThat(new BlocksGroup(Arrays.asList(newBlock("a", 6), newBlock("c", 2)))
        .subsumedBy(new BlocksGroup(Arrays.asList(newBlock("a", 5), newBlock("b", 3), newBlock("c", 1)))),
        is(true));
  }

  /**
   * Results for this test taken from results of work of {@link NaiveBlocksGroup naive implementation}.
   */
  @Test
  public void testIntersect2() {
    // ['a'[2|2-7]:3, 'b'[0|0-5]:3]
    // intersect ['a'[3|3-8]:4, 'b'[1|1-6]:4]
    // as ['a'[3|3-8]:4, 'b'[1|1-6]:4]
    assertThat(new BlocksGroup(Arrays.asList(newBlock("a", 2), newBlock("b", 0)))
        .intersect(new BlocksGroup(Arrays.asList(newBlock("a", 3), newBlock("b", 1))))
        .size(), is(2));

    // ['a'[3|3-8]:4, 'b'[1|1-6]:4]
    // intersect ['a'[4|4-9]:5, 'b'[2|2-7]:5, 'c'[0|0-5]:5]
    // as ['a'[4|4-9]:5, 'b'[2|2-7]:5]
    assertThat(new BlocksGroup(Arrays.asList(newBlock("a", 3), newBlock("b", 1)))
        .intersect(new BlocksGroup(Arrays.asList(newBlock("a", 4), newBlock("b", 2), newBlock("c", 0))))
        .size(), is(2));

    // ['a'[4|4-9]:5, 'b'[2|2-7]:5]
    // intersect ['a'[5|5-10]:6, 'b'[3|3-8]:6, 'c'[1|1-6]:6]
    // as ['a'[5|5-10]:6, 'b'[3|3-8]:6]
    assertThat(new BlocksGroup(Arrays.asList(newBlock("a", 4), newBlock("b", 2)))
        .intersect(new BlocksGroup(Arrays.asList(newBlock("a", 5), newBlock("b", 3), newBlock("c", 1))))
        .size(), is(2));

    // ['a'[5|5-10]:6, 'b'[3|3-8]:6]
    // intersect ['a'[6|6-11]:7, 'c'[2|2-7]:7]
    // as ['a'[6|6-11]:7]
    assertThat(new BlocksGroup(Arrays.asList(newBlock("a", 5), newBlock("b", 3)))
        .intersect(new BlocksGroup(Arrays.asList(newBlock("a", 6), newBlock("c", 2))))
        .size(), is(1));

    // ['a'[4|4-9]:5, 'b'[2|2-7]:5, 'c'[0|0-5]:5]
    // intersect ['a'[5|5-10]:6, 'b'[3|3-8]:6, 'c'[1|1-6]:6]
    // as ['a'[5|5-10]:6, 'b'[3|3-8]:6, 'c'[1|1-6]:6]
    assertThat(new BlocksGroup(Arrays.asList(newBlock("a", 4), newBlock("b", 2), newBlock("c", 0)))
        .intersect(new BlocksGroup(Arrays.asList(newBlock("a", 5), newBlock("b", 3), newBlock("c", 1))))
        .size(), is(3));

    // ['a'[5|5-10]:6, 'b'[3|3-8]:6, 'c'[1|1-6]:6]
    // intersect ['a'[6|6-11]:7, 'c'[2|2-7]:7]
    // as ['a'[6|6-11]:7, 'c'[2|2-7]:7]
    assertThat(new BlocksGroup(Arrays.asList(newBlock("a", 5), newBlock("b", 3), newBlock("c", 1)))
        .intersect(new BlocksGroup(Arrays.asList(newBlock("a", 6), newBlock("c", 2))))
        .size(), is(2));

    // ['a'[6|6-11]:7, 'c'[2|2-7]:7]
    // intersect ['a'[7|7-12]:8]
    // as ['a'[7|7-12]:8]
    assertThat(new BlocksGroup(Arrays.asList(newBlock("a", 6), newBlock("c", 7)))
        .intersect(new BlocksGroup(Arrays.asList(newBlock("a", 7))))
        .size(), is(1));
  }

}
