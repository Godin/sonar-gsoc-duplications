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
package org.sonar.duplications.benchmark;

import org.sonar.duplications.algorithm.AdvancedGroupCloneReporter;
import org.sonar.duplications.algorithm.CloneReporterAlgorithm;
import org.sonar.duplications.block.BlockChunker;
import org.sonar.duplications.index.CloneIndex;
import org.sonar.duplications.java.JavaStatementBuilder;
import org.sonar.duplications.java.JavaTokenProducer;

/**
 * @deprecated not very flexible and used only in benchmarks
 */
@Deprecated
public final class JavaCloneFinder {

  private JavaCloneFinder() {
  }

  public static CloneFinder build(CloneIndex cloneIndex) {
    return JavaCloneFinder.build(cloneIndex, 5);
  }

  public static CloneFinder build(CloneIndex cloneIndex, int blockSize) {
    return JavaCloneFinder.build(cloneIndex, blockSize, new AdvancedGroupCloneReporter(cloneIndex));
  }

  public static CloneFinder build(CloneIndex cloneIndex, int blockSize, CloneReporterAlgorithm cloneReporter) {
    CloneFinder.Builder builder = CloneFinder.build()
        .setTokenChunker(JavaTokenProducer.build())
        .setStatementChunker(JavaStatementBuilder.build())
        .setBlockChunker(new BlockChunker(blockSize))
        .setCloneIndex(cloneIndex)
        .setCloneReporter(cloneReporter);
    return builder.build();
  }
}
