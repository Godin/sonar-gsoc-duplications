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
package org.sonar.duplications.block;

import java.util.Arrays;

import org.junit.Test;
import org.sonar.duplications.DuplicationsException;

public class FileBlockGroupTest {

  @Test
  public void shouldAddBlockWithSameResourceId() {
    FileBlockGroup.create("a",
        Arrays.asList(new Block("a", new ByteArray(new byte[] { 1, 2, 3, 4, 5 }), 1, 1, 7)));
  }

  @Test(expected = DuplicationsException.class)
  public void shouldNotAddBlockWithDifferentResourceId() {
    FileBlockGroup.create("a",
        Arrays.asList(new Block("b", new ByteArray(new byte[] { 1, 2, 3, 4, 5 }), 1, 1, 7)));
  }

}
