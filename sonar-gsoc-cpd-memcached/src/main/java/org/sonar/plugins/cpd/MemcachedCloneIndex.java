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
package org.sonar.plugins.cpd;

import net.spy.memcached.MemcachedClient;
import org.sonar.duplications.block.Block;
import org.sonar.duplications.index.CloneIndex;

import java.util.Collection;

public class MemcachedCloneIndex implements CloneIndex {

  private final String cloneGroup;

  private final MemcachedClient client;

  public MemcachedCloneIndex(String cloneGroup, MemcachedClient client) {
    this.cloneGroup = cloneGroup;
    this.client = client;
  }

  public Collection<String> getAllUniqueResourceId() {
    return null;
  }

  public boolean containsResourceId(String resourceId) {
    Object obj = client.get("resource-id" + resourceId);
    return obj != null;
  }

  public Collection<Block> getByResourceId(String resourceId) {
    return null;
  }

  public Collection<Block> getBySequenceHash(String sequenceHash) {
    return null;
  }

  public void insert(Block block) {
  }

  public void remove(String resourceId) {
  }

  public void remove(Block block) {
  }

  public void removeAll() {
  }

  public int size() {
    return 0;
  }
}
