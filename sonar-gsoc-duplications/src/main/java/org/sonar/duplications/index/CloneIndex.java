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
package org.sonar.duplications.index;

import java.util.Collection;

import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.ByteArray;

public interface CloneIndex {

  /**
   * @deprecated Godin: I don't think that we need such method, moreover currently it used only from tests
   */
  @Deprecated
  Collection<String> getAllUniqueResourceId();

  /**
   * @deprecated Godin: I don't think that we need such method
   */
  @Deprecated
  boolean containsResourceId(String resourceId);

  /**
   * Method performs search in index db. Parameter is full filename inside
   * project exactly the same as it was saved in db.
   * If file is moved inside project it should be removed and then added again.
   * blocks in SortedSet are sorted by position in file
   *
   * @param resourceId - unique resourceId like file name
   * @return list of <tt>Block</tt> from index and empty list if nothing found
   */
  Collection<Block> getByResourceId(String resourceId);

  /**
   * Search by sequence hash.
   *
   * @param sequenceHash - hash of statement sequence
   * @return set of <tt>Block</tt> from index and empty list if nothing found
   */
  Collection<Block> getBySequenceHash(ByteArray sequenceHash);

  void insert(Block block);

  /**
   * Remove all <tt>Block</tt> from index with <tt>resourceId</tt>
   *
   * @param resourceId full path of file in project with filename
   */
  void remove(String resourceId);

  /**
   * Remove all elements from index that are equals to <tt>block</tt>
   *
   * @param block block to be removed from index
   * @deprecated Godin: I don't think that we need such method, moreover currently it unused
   */
  @Deprecated
  void remove(Block block);

  /**
   * Empty hash index - remove all tuples
   */
  void removeAll();

  /**
   * Total number of blocks in index
   *
   * @return size of index
   */
  int size();
}
