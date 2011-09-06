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

import java.io.File;
import java.util.List;

import org.sonar.duplications.algorithm.AdvancedGroupCloneReporter;
import org.sonar.duplications.algorithm.CloneReporterAlgorithm;
import org.sonar.duplications.algorithm.FileBlockGroup;
import org.sonar.duplications.block.Block;
import org.sonar.duplications.index.CloneIndex;
import org.sonar.duplications.index.MemoryCloneIndex;

import com.google.common.collect.Lists;

public class NewCpdBenchmark extends Benchmark {

  private final List<File> files;
  private final int blockSize;

  public NewCpdBenchmark(List<File> files, int blockSize) {
    this.files = files;
    this.blockSize = blockSize;
  }

  @Override
  public void runRound() throws Exception {
    CloneIndex delegate = new MemoryCloneIndex();
    CloneIndex index = TimingProxy.newInstance(delegate);
    CloneReporterAlgorithm reporter = new AdvancedGroupCloneReporter(index);
    singleRun(files, blockSize, index, reporter);
    TimingProxy.getHandlerFor(index).printTimings();
  }

  protected static void singleRun(List<File> files, int blockSize, CloneIndex index, CloneReporterAlgorithm reporter) {
    CloneFinder cf = JavaCloneFinder.build(index, blockSize);
    for (File file : files) {
      cf.register(file);
    }
    for (File file : files) {
      String resourceId = file.getAbsolutePath();
      List<Block> candidateBlockList = Lists.newArrayList(index.getByResourceId(resourceId));
      FileBlockGroup fileBlockGroup = FileBlockGroup.create(resourceId, candidateBlockList);
      reporter.reportClones(fileBlockGroup);
    }
  }

}