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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.BlockChunker;
import org.sonar.duplications.block.ByteArray;
import org.sonar.duplications.statement.Statement;

import com.google.common.collect.Lists;

public abstract class AbstractHashBlockChunker extends BlockChunker {

  public AbstractHashBlockChunker(int blockSize) {
    super(blockSize);
  }

  @Override
  public List<Block> chunk(String resourceId, List<Statement> statements) {
    if (statements.size() < getBlockSize()) {
      return Collections.emptyList();
    }

    LinkedList<Statement> statementsForBlock = new LinkedList<Statement>();
    List<Block> blocks = Lists.newArrayListWithCapacity(statements.size() - getBlockSize() + 1);

    for (Statement stmt : statements) {
      statementsForBlock.add(stmt);
      if (statementsForBlock.size() == getBlockSize()) {
        Statement firstStatement = statementsForBlock.getFirst();
        Statement lastStatement = statementsForBlock.getLast();
        blocks.add(new Block(resourceId,
            buildBlockHash(statementsForBlock),
            blocks.size(),
            firstStatement.getStartLine(),
            lastStatement.getEndLine()));
        statementsForBlock.removeFirst();
      }
    }

    return blocks;
  }

  protected abstract ByteArray buildBlockHash(List<Statement> statements);

}
