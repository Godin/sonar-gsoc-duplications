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

public abstract class Benchmark {

  public final BenchmarkResult runBenchmark(int benchmarkRounds, int warmupRounds) {
    return runBenchmark(benchmarkRounds, warmupRounds, true);
  }

  public final BenchmarkResult runBenchmark(int benchmarkRounds, int warmupRounds, boolean callgc) {
    long warmupTime = System.currentTimeMillis();

    // warmup rounds
    for (int i = 0; i < warmupRounds; i++) {
      if (callgc) {
        MemoryUtils.cleanup();
      }
      internalRunRound();
    }

    long benchTime = System.currentTimeMillis();
    warmupTime = benchTime - warmupTime;

    // benchmark rounds
    long[] peakMemory = new long[benchmarkRounds];
    long[] roundTime = new long[benchmarkRounds];
    for (int i = 0; i < benchmarkRounds; i++) {
      if (callgc) {
        MemoryUtils.cleanup();
      }
      MemoryUtils.resetPeakUsage();
      long time = System.currentTimeMillis();
      internalRunRound();
      time = System.currentTimeMillis() - time;
      roundTime[i] = time;
      peakMemory[i] = MemoryUtils.getPeakUsage();
    }
    benchTime = System.currentTimeMillis() - benchTime;

    BenchmarkResult result = new BenchmarkResult(
        getName(),
        warmupRounds,
        benchmarkRounds,
        warmupTime,
        benchTime,
        Average.from(roundTime),
        Average.from(peakMemory));
    System.out.println(result);
    return result;
  }

  private void internalRunRound() {
    try {
      runRound();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public String getName() {
    return getClass().getSimpleName();
  }

  public abstract void runRound() throws Exception;

}
