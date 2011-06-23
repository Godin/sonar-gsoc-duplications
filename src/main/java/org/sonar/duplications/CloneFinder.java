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
package org.sonar.duplications;

import java.io.File;
import java.util.List;

import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.BlockChunker;
import org.sonar.duplications.index.CloneIndex;
import org.sonar.duplications.statement.StatementChunker;
import org.sonar.duplications.token.TokenChunker;

public class CloneFinder {

  private TokenChunker tokenChunker;
  private StatementChunker stmtChunker;
  private BlockChunker blockChunker;
  private CloneIndex cloneIndex;

  private CloneFinder(Builder builder) {
    this.tokenChunker = builder.tokenChunker;
    this.stmtChunker = builder.stmtChunker;
    this.blockChunker = builder.blockChunker;
    this.cloneIndex = cloneIndex;
  }

  public void register(File sourceFile) {
    List<Block> blocks = blockChunker.chunk(stmtChunker.chunk(tokenChunker.chunk(sourceFile)));
    // TODO
  }

  public static Builder build() {
    return new Builder();
  }

  public static final class Builder {

    private TokenChunker tokenChunker;
    private StatementChunker stmtChunker;
    private BlockChunker blockChunker;
    private CloneIndex cloneIndex;

    public Builder setTokenChunker(TokenChunker tokenChunker) {
      this.tokenChunker = tokenChunker;
      return this;
    }

    public Builder setStatementChunker(StatementChunker stmtChunker) {
      this.stmtChunker = stmtChunker;
      return this;
    }

    public Builder setBlockChunker(BlockChunker blockChunker) {
      this.blockChunker = blockChunker;
      return this;
    }

    public Builder setCloneIndex(CloneIndex cloneIndex) {
      this.cloneIndex = cloneIndex;
      return this;
    }

    public CloneFinder build() {
      return new CloneFinder(this);
    }
  }
}
