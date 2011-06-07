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
package org.sonar.duplications.api.index;

import org.sonar.duplications.api.codeunit.Block;

import java.util.Set;
import java.util.SortedSet;

public interface CloneIndexBackend {

  public Set<String> getAllUniqueResourceId();

  public boolean containsResourceId(String resourceId);

  /**
   * Method performs search in index db. Parameter is full filename inside
   * project exactly the same as it was saved in db.
   * If file is moved inside project it should be removed and then added again.
   *
   * @param resourceId - unique resourceId like file name
   * @return list of <tt>Block</tt> from index and empty list if nothing found
   */
  public SortedSet<Block> getByResourceId(String resourceId);

  /**
   * Search by sequence hash.
   *
   * @param sequenceHash - hash of statement sequence
   * @return set of <tt>Block</tt> from index and empty list if nothing found
   */
  public Set<Block> getBySequenceHash(byte[] sequenceHash);

  public void insert(Block tuple);

  /**
   * Remove all <tt>Block</tt> from index with <tt>fileName</tt>
   *
   * @param fileName - full path of file in project with filename
   */
  public void remove(String fileName);

  /**
   * Remove all elements from index that are equals to <tt>tuple</tt>
   *
   * @param tuple - tuple to be removed from index
   */
  public void remove(Block tuple);

  /**
   * Empty hash index - remove all tuples
   */
  public void removeAll();

  /**
   * Total number of tuples in index
   *
   * @return size of index
   */
  public int size();
}
