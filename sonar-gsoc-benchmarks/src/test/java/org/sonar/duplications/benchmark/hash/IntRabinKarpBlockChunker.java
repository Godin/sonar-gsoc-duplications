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
package org.sonar.duplications.benchmark.hash;

import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.BlockChunker;
import org.sonar.duplications.block.ByteArray;
import org.sonar.duplications.statement.Statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: skydiver
 * Date: 14.08.11
 * Time: 22:52
 */
public class IntRabinKarpBlockChunker extends BlockChunker {

  private static final int PRIME_BASE = 31;
  private int power;

  private int blockSize;

  public IntRabinKarpBlockChunker(int blockSize) {
    super(blockSize);

    this.blockSize = blockSize;
    power = 1;
    for (int i = 0; i < blockSize; i++) {
      power = power * PRIME_BASE;
    }
  }

  public List<Block> chunk(String resourceId, List<Statement> statements) {
    if (statements.size() < blockSize) {
      return Collections.emptyList();
    }
    List<Block> blockList = new ArrayList<Block>();
    int hash = 0;
    for (int i = 0; i < statements.size(); i++) {
      // add current statement to hash
      Statement current = statements.get(i);
      hash = hash * PRIME_BASE + current.getValue().hashCode();
      // remove first statement from hash, if needed
      int j = i - blockSize + 1;
      if (j > 0) {
        hash -= power * statements.get(j - 1).getValue().hashCode();
      }
      // create block
      if (j >= 0) {
        Statement first = statements.get(j);
        blockList.add(new Block(resourceId, new ByteArray(hash), j, first.getStartLine(), current.getEndLine()));
      }
    }
    return blockList;
  }

}
