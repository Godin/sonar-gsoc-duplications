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
package org.sonar.duplications.benchmark.perf;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.sonar.duplications.benchmark.Benchmark;
import org.sonar.duplications.benchmark.NewCpdBenchmark;
import org.sonar.duplications.benchmark.OldCpdBenchmark;

import com.google.common.collect.Lists;

public class AbstractCompare {

  private static final int WARMUP_ROUNDS = 5;
  private static final int BENCHMARK_ROUNDS = 20;
  private static final int BLOCK_SIZE = 13;

  public static void compare(String project) {
    compare(project, BENCHMARK_ROUNDS);
  }

  public static void compare(String project, int benchmarkRounds) {
    System.out.println();
    List<File> files = getProjectFiles(project);
    System.out.println(project + ", " + files.size() + " files to analyse");
    OldCpdBenchmark oldCpd = new OldCpdBenchmark(files);
    NewCpdBenchmark newCpd = new NewCpdBenchmark(files, BLOCK_SIZE);
    compare(oldCpd, newCpd, benchmarkRounds, WARMUP_ROUNDS);
    System.out.println("Old CPD matches: " + oldCpd.getCount());
    System.out.println();
  }

  public static void compare(Benchmark first, Benchmark second, int benchmarkRounds, int warmupRounds) {
    Benchmark.cleanupMemory();
    double firstAvg = first.runBenchmark(benchmarkRounds, warmupRounds);
    Benchmark.cleanupMemory();
    double secondAvg = second.runBenchmark(benchmarkRounds, warmupRounds);
    Benchmark.cleanupMemory();
    double diff = (firstAvg - secondAvg) / firstAvg * 100.0;
    System.out.println(String.format(Locale.ENGLISH, "Difference %.0f%%", diff));
  }

  public static List<File> getProjectFiles(String project) {
    File dir = new File("target/test-projects/" + project);
    List<File> files = Lists.newArrayList();
    files.addAll(FileUtils.listFiles(dir, new String[] { "java" }, true));
    return files;
  }

}
