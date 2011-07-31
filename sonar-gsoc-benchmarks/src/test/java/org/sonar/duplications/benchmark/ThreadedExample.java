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
import java.util.Locale;

public class ThreadedExample {

  private static final String PROJECT = "activemq-core-5.5.0";
  private static final int BLOCK_SIZE = 13;
  private static final int BENCHMARK_ROUNDS = 10;
  private static final int WARMUP_ROUNDS = 3;

  public static void main(String[] args) {
    List<File> files = Utils.getProjectFiles(PROJECT);
    System.out.println(PROJECT + ", " + files.size() + " files to analyse");

    Benchmark nonThreaded = new NewCpdBenchmark(files, BLOCK_SIZE) {
      @Override
      public String getName() {
        return "new CPD non-threaded";
      }
    };
    double nonThreadedAvgTime = nonThreaded.runBenchmark(BENCHMARK_ROUNDS, WARMUP_ROUNDS);

    int availableProcessors = Runtime.getRuntime().availableProcessors();
    System.out.println("Available processors: " + availableProcessors);
    for (int threadsCount = 1; threadsCount <= availableProcessors; threadsCount++) {
      Benchmark threaded = new ThreadedNewCpdBenchmark(files, BLOCK_SIZE, threadsCount);
      double threadedAvgTime = threaded.runBenchmark(BENCHMARK_ROUNDS, WARMUP_ROUNDS);
      double diff = (nonThreadedAvgTime - threadedAvgTime) / nonThreadedAvgTime * 100.0;
      System.out.println(String.format(Locale.ENGLISH, "Difference %.0f%%", diff));
    }
  }

}
