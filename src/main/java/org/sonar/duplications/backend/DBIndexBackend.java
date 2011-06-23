/*
 * Sonar, open source software quality management tool.
 * Written (W) 2011 Andrew Tereskin
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
package org.sonar.duplications.backend;

import org.sonar.duplications.api.Block;
import org.sonar.duplications.index.CloneIndexBackend;

import java.util.Set;
import java.util.SortedSet;

//TODO: only stub now
public class DBIndexBackend implements CloneIndexBackend {

  public Set<String> getAllUniqueResourceId() {
    return null;
  }

  public boolean containsResourceId(String resourceId) {
    return false;
  }

  public SortedSet<Block> getByResourceId(String fileName) {
    return null;
  }

  public Set<Block> getBySequenceHash(String sequenceHash) {
    return null;
  }

  public void insert(Block tuple) {
  }

  public void remove(String fileName) {
  }

  public void remove(Block tuple) {
  }

  public void removeAll() {
  }

  public int size() {
    return 0;
  }
}
