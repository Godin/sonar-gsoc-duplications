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

import org.sonar.duplications.CloneFinder;
import org.sonar.duplications.index.MemoryCloneIndex;
import org.sonar.duplications.java.JavaCloneFinder;

public class PopulateIndexBenchmark extends Benchmark {
  private final List<File> files;

  public PopulateIndexBenchmark(List<File> files) {
    this.files = files;
  }

  @Override
  public void runRound() throws Exception {
    MemoryCloneIndex mci = new MemoryCloneIndex();
    CloneFinder cf = JavaCloneFinder.build(mci, 13);
    for (File file : files) {
      cf.register(file);
    }
  }
}
