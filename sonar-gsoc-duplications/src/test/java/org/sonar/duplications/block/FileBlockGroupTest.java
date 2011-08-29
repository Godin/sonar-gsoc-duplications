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

import org.junit.Before;
import org.junit.Test;
import org.sonar.duplications.DuplicationsException;
import org.sonar.duplications.DuplicationsTestUtil;
import org.sonar.duplications.java.JavaStatementBuilder;
import org.sonar.duplications.java.JavaTokenProducer;
import org.sonar.duplications.statement.StatementChunker;
import org.sonar.duplications.token.TokenChunker;

import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FileBlockGroupTest {

  private File testFile = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest/CPDFile1.java");
  private FileBlockGroup fci;

  @Before
  public void pubicInitTest() {
    init(new FileBlockGroup.Builder().setResurceId(testFile.getPath()), testFile);
  }

  @Test
  public void shouldGroupBlock() {
    assertThat(fci.getBlockList().size(), is(12));
  }

  @Test
  public void shouldAddBlockWithSameResourceId() {
    FileBlockGroup file = new FileBlockGroup.Builder()
        .setResurceId("a")
        .addBlock(new Block("a", new ByteArray(new byte[]{1, 2, 3, 4, 5}), 1, 1, 7))
        .build();
  }

  @Test(expected = DuplicationsException.class)
  public void shouldNotAddBlockWithDifferentResourceId() {
    FileBlockGroup file = new FileBlockGroup.Builder()
        .setResurceId("a")
        .addBlock(new Block("b", new ByteArray(new byte[]{1, 2, 3, 4, 5}), 1, 1, 7))
        .build();
  }

  public void init(FileBlockGroup.Builder builder, File file) {
    try {
      TokenChunker lexer = JavaTokenProducer.build();
      StatementChunker statementBuilder = JavaStatementBuilder.build();
      BlockChunker blockBuilder = new BlockChunker(5);
      for (Block block : blockBuilder.chunk(file.getPath(), statementBuilder.chunk(lexer.chunk(file)))) {
        builder.addBlock(block);
      }
      fci = builder.build();
    } catch (Exception e) {
      throw new DuplicationsException("Error in initialization", e);
    }
  }
}
