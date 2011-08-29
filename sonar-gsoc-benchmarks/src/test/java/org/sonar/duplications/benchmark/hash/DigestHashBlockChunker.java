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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.sonar.duplications.DuplicationsException;
import org.sonar.duplications.block.ByteArray;
import org.sonar.duplications.statement.Statement;

public class DigestHashBlockChunker extends AbstractHashBlockChunker {

  /**
   * TODO Godin: separation of statements required, but I'm not sure that we use a good value for this
   */
  private static final byte SEPARATOR = 0;

  private final MessageDigest digest;

  public static enum Algorithm {
    MD5, SHA;
  }

  public DigestHashBlockChunker(Algorithm algorithm, int blockSize) {
    this(algorithm.toString(), blockSize);
  }

  public DigestHashBlockChunker(String algorithm, int blockSize) {
    super(blockSize);
    try {
      this.digest = MessageDigest.getInstance(algorithm);
    } catch (NoSuchAlgorithmException e) {
      throw new DuplicationsException("Unable to create a digest generator", e);
    }
  }

  @Override
  protected ByteArray buildBlockHash(List<Statement> statements) {
    digest.reset();
    for (Statement statement : statements) {
      digest.update(statement.getValue().getBytes());
      digest.update(SEPARATOR);
    }
    return new ByteArray(digest.digest());
  }

}
