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
package org.sonar.duplications.block;

import com.google.common.collect.Sets;
import org.sonar.duplications.DuplicationsException;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.SortedSet;

public final class FileBlockGroup {

  private final String resourceId;
  private final SortedSet<Block> fileBlocks;

  private FileBlockGroup(Builder builder) {
    this.resourceId = builder.resourceId;
    this.fileBlocks = builder.fileBlocks;
  }

  public static FileBlockGroup create(String resourceId, Collection<Block> blocks) {
    return new Builder()
        .setResurceId(resourceId)
        .addBlocks(blocks)
        .build();
  }

  public static final class Builder {

    private static final Comparator<Block> BLOCK_COMPARATOR = new Comparator<Block>() {
      public int compare(Block o1, Block o2) {
        return o1.getIndexInFile() - o2.getIndexInFile();
      }
    };

    private SortedSet<Block> fileBlocks = Sets.newTreeSet(BLOCK_COMPARATOR);
    private String resourceId;

    public Builder setResurceId(String resourceId) {
      this.resourceId = resourceId;
      return this;
    }

    public Builder addBlock(Block block) {
      fileBlocks.add(block);
      return this;
    }

    public Builder addBlocks(Collection<Block> blocks) {
      fileBlocks.addAll(blocks);
      return this;
    }

    public FileBlockGroup build() {
      for (Block block : fileBlocks) {
        if (!block.getResourceId().equals(resourceId)) {
          throw new DuplicationsException("Block resourceId not equals to FileBlockGroup resourceId");
        }
      }
      return new FileBlockGroup(this);
    }
  }

  public String getResourceId() {
    return resourceId;
  }

  public SortedSet<Block> getBlockList() {
    return Collections.unmodifiableSortedSet(fileBlocks);
  }
}
