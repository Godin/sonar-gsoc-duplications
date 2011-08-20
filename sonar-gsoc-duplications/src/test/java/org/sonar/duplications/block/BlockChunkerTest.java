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

import org.junit.Test;
import org.sonar.duplications.statement.Statement;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class BlockChunkerTest {

  @Test
  public void shouldBuildBlocksFromStatements() {
    BlockChunker blockChunker = new BlockChunker(2);
    List<Statement> statements = Arrays.asList(
        new Statement(1, 1, "package example;"),
        new Statement(2, 2, "class Example {"),
        new Statement(3, 3, "}"));

    List<Block> blocks = blockChunker.chunk("foo", statements);

    assertThat(blocks.size(), is(2));

    assertThat(blocks.get(0).getIndexInFile(), is(0));
    assertThat(blocks.get(0).getFirstLineNumber(), is(1));
    assertThat(blocks.get(0).getLastLineNumber(), is(2));

    assertThat(blocks.get(1).getIndexInFile(), is(1));
    assertThat(blocks.get(1).getFirstLineNumber(), is(2));
    assertThat(blocks.get(1).getLastLineNumber(), is(3));

    assertThat(blocks.get(0).getBlockHash(), not(equalTo(blocks.get(1).getBlockHash())));
  }

  @Test
  public void shouldCalculateHashes() {
    BlockChunker blockChunker = new BlockChunker(2);
    List<Statement> statements = Arrays.asList(
        new Statement(1, 1, "if (a)"),
        new Statement(2, 2, "doWork();"),
        new Statement(3, 3, "if (a)"),
        new Statement(4, 4, "doWork();"),
        new Statement(5, 5, "doWork();"));

    List<Block> blocks = blockChunker.chunk("foo", statements);
    assertThat(blocks.size(), is(4));
    // for (Block block : blocks) {
    // System.out.println(block.getBlockHash());
    // }
    assertThat("same value as for block 2", blocks.get(0).getBlockHash(), equalTo(blocks.get(2).getBlockHash()));
    assertThat("same value as for block 2", blocks.get(0).getBlockHash(), is(new ByteArray("fffffff715d0c4b1")));
    assertThat(blocks.get(1).getBlockHash(), is(new ByteArray("fffffff6750ec0af")));
    assertThat("same value as for block 0", blocks.get(2).getBlockHash(), is(new ByteArray("fffffff715d0c4b1")));
    assertThat(blocks.get(3).getBlockHash(), is(new ByteArray("fffffff66fb2f3c0")));
  }

  @Test
  public void shouldNotBuildBlocksWhenNoStatements() {
    BlockChunker blockChunker = new BlockChunker(2);
    List<Statement> statements = Collections.emptyList();

    List<Block> blocks = blockChunker.chunk("foo", statements);

    assertThat(blocks.size(), is(0));
  }

  @Test
  public void shouldNotBuildBlocksWhenNotEnoughStatements() {
    BlockChunker blockChunker = new BlockChunker(2);
    List<Statement> statements = Arrays.asList(new Statement(1, 1, "package example;"));

    List<Block> blocks = blockChunker.chunk("foo", statements);

    assertThat(blocks.size(), is(0));
  }

}
