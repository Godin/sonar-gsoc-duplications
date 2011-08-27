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
package org.sonar.duplications.block;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.sonar.duplications.statement.Statement;

import com.google.common.collect.Lists;

/**
 * Any implementation of {@link BlockChunker} should pass these test scenarios.
 */
public abstract class BlockChunkerTestCase {

  /**
   * Factory method.
   */
  protected abstract BlockChunker createChunkerWithBlockSize(int blockSize);

  /**
   * Given:
   * <pre>
   * String[][] data = {
   *   {"a", "a"},
   *   {"a", "a"},
   *   {"a"},
   *   {"a", "a"},
   *   {"a", "a"}
   * };
   *
   * Statements (where L - literal, C - comma): "LCL", "C", "LCL", "C", "L", "C", "LCL", "C", "LCL"
   * Block size is 5.
   * First block: "LCL", "C", "LCL", "C", "L"
   * Last block: "L", "C", "LCL", "C", "LCL"
   * </pre>
   * Expected: different hashes for first and last blocks
   */
  @Test
  public void testSameChars() {
    List<Statement> statements = statementsFromStrings("LCL", "C", "LCL", "C", "L", "C", "LCL", "C", "LCL");
    BlockChunker chunker = createChunkerWithBlockSize(5);
    List<Block> blocks = chunker.chunk("resource", statements);
    String hash1 = blocks.get(0).getBlockHash().toString();
    String hash2 = blocks.get(blocks.size() - 1).getBlockHash().toString();
    assertFalse(hash1.equals(hash2));
  }

  /**
   * Given: 5 statements, block size is 3
   * Expected: 4 blocks
   */
  @Test
  public void testSize() {
    List<Statement> statements = statementsFromStrings("1", "2", "3", "4", "5", "6");
    BlockChunker chunker = createChunkerWithBlockSize(3);
    List<Block> blocks = chunker.chunk("resource", statements);
    assertThat(blocks.size(), is(4));
  }

  @Test
  public void testHashes() {
    List<Statement> statements = statementsFromStrings("1", "2", "1", "2");
    BlockChunker chunker = createChunkerWithBlockSize(2);
    List<Block> blocks = chunker.chunk("resource", statements);
    assertThat(blocks.get(0).getBlockHash(), equalTo(blocks.get(2).getBlockHash()));
    assertThat(blocks.get(0).getBlockHash(), not(equalTo(blocks.get(1).getBlockHash())));
  }

  /**
   * Given: 0 statements
   * Expected: 0 blocks
   * TODO Godin: in fact we can even require {@link Collections#EMPTY_LIST}
   */
  @Test
  public void shouldNotBuildBlocksWhenNoStatements() {
    List<Statement> statements = Collections.emptyList();
    BlockChunker blockChunker = createChunkerWithBlockSize(2);
    List<Block> blocks = blockChunker.chunk("resource", statements);
    assertThat(blocks.size(), is(0));
  }

  /**
   * Given: 1 statement, block size is 2
   * Expected: 0 blocks
   * TODO Godin: in fact we can even require {@link Collections#EMPTY_LIST}
   */
  @Test
  public void shouldNotBuildBlocksWhenNotEnoughStatements() {
    List<Statement> statements = statementsFromStrings("statement");
    BlockChunker blockChunker = createChunkerWithBlockSize(2);
    List<Block> blocks = blockChunker.chunk("resource", statements);
    assertThat(blocks.size(), is(0));
  }

  private static List<Statement> statementsFromStrings(String... values) {
    List<Statement> result = Lists.newArrayList();
    for (String value : values) {
      result.add(new Statement(0, 0, value));
    }
    return result;
  }

}
