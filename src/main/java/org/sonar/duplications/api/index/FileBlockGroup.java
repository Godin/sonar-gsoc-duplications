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

import org.sonar.duplications.api.CloneIndexException;
import org.sonar.duplications.api.codeunit.block.Block;

import java.util.*;

public class FileBlockGroup {
  private final String fileResourceId;
  private final SortedSet<Block> fileBlocks;

  private static final class BlockComparator implements Comparator<Block> {

    public int compare(Block o1, Block o2) {
      if (o2.getResourceId().equals(o1.getResourceId())) {
        return o1.getFirstUnitIndex() - o2.getFirstUnitIndex();
      }
      return -1;
    }

    public boolean equals(Object obj) {
      return obj instanceof BlockComparator;
    }
  }

  public FileBlockGroup(String fileResourceId) {
    this.fileResourceId = fileResourceId;
    this.fileBlocks = new TreeSet<Block>(new BlockComparator());
  }

  public void addBlock(Block block) {
    if (!getFileResourceId().equals(block.getResourceId())) {
      throw new CloneIndexException("Block resourceId not equals to FileBlockGroup resourceId");
    }
    fileBlocks.add(block);
  }

  public String getFileResourceId() {
    return fileResourceId;
  }

  public Set<Block> getAllBlocks() {
    return Collections.unmodifiableSortedSet(fileBlocks);
  }
}
