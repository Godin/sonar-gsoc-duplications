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
package org.sonar.duplications.benchmark.db;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.ByteArray;
import org.sonar.duplications.index.AbstractCloneIndex;
import org.sonar.duplications.index.CloneIndex;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class CombinedCloneIndex extends AbstractCloneIndex {

  private final CloneIndex first;
  private final CloneIndex second;

  /**
   * Ids of all resources, which were indexed.
   */
  private final Set<String> resourceIds = Sets.newHashSet();

  public CombinedCloneIndex(CloneIndex first, CloneIndex second) {
    this.first = first;
    this.second = second;
  }

  public Collection<Block> getByResourceId(String resourceId) {
    return first.getByResourceId(resourceId);
  }

  public Collection<Block> getBySequenceHash(ByteArray sequenceHash) {
    List<Block> result = Lists.newArrayList();
    result.addAll(first.getBySequenceHash(sequenceHash));
    for (Block block : second.getBySequenceHash(sequenceHash)) {
      if (!resourceIds.contains(block.getResourceId())) {
        result.add(block);
      }
    }
    return result;
  }

  public void insert(Block block) {
    resourceIds.add(block.getResourceId());
    first.insert(block);
    second.insert(block);
  }

}
