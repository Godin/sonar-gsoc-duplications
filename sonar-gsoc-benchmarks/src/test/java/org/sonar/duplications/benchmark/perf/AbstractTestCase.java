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

import org.junit.*;
import org.sonar.duplications.algorithm.AdvancedCloneReporter;
import org.sonar.duplications.algorithm.CloneReporterAlgorithm;
import org.sonar.duplications.algorithm.PairedAdvancedCloneReporter;
import org.sonar.duplications.benchmark.*;
import org.sonar.duplications.index.CloneIndex;
import org.sonar.duplications.index.MemoryCloneIndex;

import java.io.File;
import java.util.List;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;

public class AbstractTestCase {

  protected static int BLOCK_SIZE = 13;
  protected static int WARMUP_ROUNDS = 3;
  protected static int BENCHMARK_ROUNDS = 10;

  protected static List<File> files;
  protected static BenchmarksDiff results = new BenchmarksDiff();

  protected BenchmarkResult run(Benchmark benchmark) {
    return benchmark.runBenchmark(BENCHMARK_ROUNDS, WARMUP_ROUNDS);
  }

  @Before
  public void setUp() {
    MemoryUtils.cleanup();
  }

  @After
  public void tearDown() {
    MemoryUtils.cleanup();
  }

  @Test
  public void oldCpd() {
    OldCpdBenchmark oldCpd = new OldCpdBenchmark(files);
    results.setReference(run(oldCpd));
    System.out.println("Old CPD matches: " + oldCpd.getCount());
  }

  @Test
  public void newCpdAdvanced() {
    CloneIndex index = new MemoryCloneIndex();
    CloneReporterAlgorithm reporter = new AdvancedCloneReporter(index);
    results.add(run(new NewCpdBenchmark(files, BLOCK_SIZE, index, reporter)));
  }

  @Test
  public void newCpdPaired() {
    CloneIndex index = new MemoryCloneIndex();
    CloneReporterAlgorithm reporter = new PairedAdvancedCloneReporter(index);
    results.add(run(new NewCpdBenchmark(files, BLOCK_SIZE, index, reporter)));
  }

  @Test
  public void originalAlgorithm() {
    results.add(run(new OriginalAlgorithmBenchmark(files, BLOCK_SIZE)));
  }

  @Test
  public void newCpdWithTwoThreads() {
    int availableProcessors = Runtime.getRuntime().availableProcessors();
    Assume.assumeThat(availableProcessors, greaterThanOrEqualTo(2));
    results.add(run(new ThreadedNewCpdBenchmark(files, BLOCK_SIZE, 2)));
  }

  @Test
  public void newCpdWithFourThreads() {
    int availableProcessors = Runtime.getRuntime().availableProcessors();
    Assume.assumeThat(availableProcessors, greaterThanOrEqualTo(4));
    results.add(run(new ThreadedNewCpdBenchmark(files, BLOCK_SIZE, 4)));
  }

  @AfterClass
  public static void after() {
    results.print();
  }

}
