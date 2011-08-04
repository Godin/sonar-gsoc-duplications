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

import java.util.List;

import org.sonar.duplications.block.Block;

import com.google.common.collect.Lists;

/**
 * Straightforward implementation made by Sharif Uddin.
 */
class NaiveBlocksGroup extends BlocksGroup {

  protected NaiveBlocksGroup() {
    super();
  }

  @Override
  protected boolean subsumedBy(BlocksGroup group1, BlocksGroup group2, int indexCorrection) {
    boolean result = true;
    for (Block block1 : group1.blocks) {
      // search for a block2 to cover block1
      boolean partialResult = false;
      for (Block block2 : group2.blocks) {
        if (block1.getResourceId().equals(block2.getResourceId()) && (block1.getIndexInFile() - indexCorrection) == block2.getIndexInFile()) {
          // block1 is covered by block2
          partialResult = true;
          break;
        }
      }
      if (!partialResult) {
        // block1 is not covered by any block2
        result = false;
        break;
      }
    }
    return result;
  }

  @Override
  protected BlocksGroup intersect(BlocksGroup group1, BlocksGroup group2) {
    NaiveBlocksGroup result = new NaiveBlocksGroup();
    for (Block block1 : group1.blocks) {
      for (Block block2 : group2.blocks) {
        if (block1.getResourceId().equals(block2.getResourceId()) && block1.getIndexInFile() + 1 == block2.getIndexInFile()) {
          result.blocks.add(block2);
          break;
        }
      }
    }
    return result;
  }

  @Override
  protected List<Block[]> pairs(BlocksGroup beginGroup, BlocksGroup endGroup, int cloneLength) {
    List<Block[]> result = Lists.newArrayList();
    for (Block beginBlock : beginGroup.blocks) {
      for (Block endBlock : endGroup.blocks) {
        if ((beginBlock.getResourceId().equals(endBlock.getResourceId())) && (beginBlock.getIndexInFile() + cloneLength - 1 == endBlock.getIndexInFile())) {
          result.add(new Block[] { beginBlock, endBlock });
          break;
        }
      }
    }
    return result;
  }

}
