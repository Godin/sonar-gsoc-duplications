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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.BlockChunker;
import org.sonar.duplications.block.BlockChunkerTestCase;
import org.sonar.duplications.statement.Statement;

public class DigestHashBlockChunkerTest extends BlockChunkerTestCase {

  @Override
  protected BlockChunker createChunkerWithBlockSize(int blockSize) {
    return new DigestHashBlockChunker("MD5", blockSize);
  }

  /**
   * Hash values must always be the same (without dependency on JDK).
   */
  @Test
  public void shouldCalculateHashes() {
    List<Statement> statements = createStatementsFromStrings("aaaaaa", "bbbbbb", "cccccc", "dddddd", "eeeeee");
    BlockChunker blockChunker = createChunkerWithBlockSize(3);
    List<Block> blocks = blockChunker.chunk("resource", statements);
    assertThat(blocks.get(0).getBlockHash().toString(), is("4bc1aaf5af66c5e249cff2fbfd3e0b3a"));
    assertThat(blocks.get(1).getBlockHash().toString(), is("4b6d547c4e0205f9f4128617994dfc03"));
    assertThat(blocks.get(2).getBlockHash().toString(), is("368fc86c8cbb6d233f8f3f5baa0093d6"));
  }

}
