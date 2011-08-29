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
package org.sonar.duplications.benchmark;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class SizeOfTest {

  @Test
  public void test32bit() {
    // sizeOf(char[0]) = 3 * REF = 12, which is aligned to 16
    assertThat(SizeOf.sizeOf(new char[0]), is(16L));
    // sizeOf(char[1]) = 3 * REF + sizeOf(char) * 1 = 14, which is aligned to 16
    assertThat(SizeOf.sizeOf(new char[1]), is(16L));
    // sizeOf(char[2]) = 3 * REF + sizeOf(char) * 2 = 16
    assertThat(SizeOf.sizeOf(new char[2]), is(16L));
    // sizeOf(char[3]) = 3 * REF + sizeOf(char) * 3 = 18, which is aligned to 24
    assertThat(SizeOf.sizeOf(new char[3]), is(24L));

    // sizeOf("") = 2 * REF + 3 * sizeOf(int) + sizeOf(char[0]) = 12 + 12 + 16 = 40
    assertThat(SizeOf.sizeOf(new String("")), is(40L));

    // sizeOf("abc") = 2 * REF + 3 * sizeOf(int) + sizeOf(char[2]) = 12 + 12 + 24 = 48
    assertThat(SizeOf.sizeOf(new String("abc")), is(48L));

    // 2 * REF + sizeOf(int) + sizeOf(long) = 20, which is aligned to 24
    assertThat(SizeOf.sizeOf(new PrimitiveContainer()), is(24L));

    // 2 * REF + 4 = 16
    assertThat(SizeOf.sizeOf(new Integer(1)), is(16L));

    // 2 * REF + 8 = 16
    assertThat(SizeOf.sizeOf(new Long(2)), is(16L));

    // 2 * REF + (REF + sizeOf(Integer)) + (REF + sizeOf(Long)) = 48
    assertThat(SizeOf.sizeOf(new ObjectContainer()), is(48L));

    // 3 * REF + sizeOf(byte) * 4 = 12 + 4 = 16
    assertThat(SizeOf.sizeOf(new byte[] { 1, 2, 3, 4 }), is(16L));
  }

  @Test
  public void test64bit() {
    assertThat(SizeOf.sizeOfOn64(new PrimitiveContainer()), is(32L));
    assertThat(SizeOf.sizeOfOn64(new ObjectContainer()), is(80L));
    assertThat(SizeOf.sizeOfOn64(new byte[] { 1, 2, 3, 4 }), is(32L));
  }

  public static class ObjectContainer {
    public Integer x = new Integer(1);
    public Long y = new Long(2);
  }

  public static class PrimitiveContainer {
    public int x = 1;
    public long y = 2;
  }

}
