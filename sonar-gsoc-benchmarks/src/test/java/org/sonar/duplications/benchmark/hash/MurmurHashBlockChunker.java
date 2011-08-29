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

import org.sonar.duplications.block.ByteArray;
import org.sonar.duplications.statement.Statement;

import java.util.List;

public class MurmurHashBlockChunker extends AbstractHashBlockChunker {

  public MurmurHashBlockChunker(int blockSize) {
    super(blockSize);
  }

  protected ByteArray buildBlockHash(List<Statement> statementList) {
    int totalLen = 0;
    for (Statement statement : statementList) {
      totalLen += statement.getValue().getBytes().length;
    }
    byte[] bytes = new byte[totalLen];
    int current = 0;
    for (Statement statement : statementList) {
      byte[] stmtBytes = statement.getValue().getBytes();
      int length = stmtBytes.length;
      System.arraycopy(stmtBytes, 0, bytes, current, length);
      current += length;
    }
    int messageDigest = MurmurHash2.hash(bytes, 0x1234ABCD);
    return new ByteArray(messageDigest);
  }
}
