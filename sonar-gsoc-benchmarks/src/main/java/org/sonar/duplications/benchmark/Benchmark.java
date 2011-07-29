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

import java.util.Locale;

public abstract class Benchmark {

  public final double runBenchmark(int benchmarkRounds, int warmupRounds) {
    return runBenchmark(benchmarkRounds, warmupRounds, true);
  }

  public final double runBenchmark(int benchmarkRounds, int warmupRounds, boolean callgc) {
    long warmupTime = System.currentTimeMillis();

    // warmup rounds
    for (int i = 0; i < warmupRounds; i++) {
      if (callgc) {
        cleanupMemory();
      }
      internalRunRound();
    }

    long benchTime = System.currentTimeMillis();
    warmupTime = benchTime - warmupTime;

    // benchmark rounds
    long[] roundTime = new long[benchmarkRounds];
    for (int i = 0; i < benchmarkRounds; i++) {
      if (callgc) {
        cleanupMemory();
      }
      long time = System.currentTimeMillis();
      internalRunRound();
      time = System.currentTimeMillis() - time;
      roundTime[i] = time;
    }
    benchTime = System.currentTimeMillis() - benchTime;

    long totalTime = warmupTime + benchTime;
    int totalRounds = warmupRounds + benchmarkRounds;
    Average averageRoundTime = Average.from(roundTime);
    System.out.println(String.format(Locale.ENGLISH,
        "%s [measured %d out of %d rounds] round: %.2f [+-%.2f], total: %.2f, warm: %.2f, bench: %.2f",
        getName(),
        benchmarkRounds,
        totalRounds,
        millisecondsToSeconds(averageRoundTime.avg),
        millisecondsToSeconds(averageRoundTime.stddev),
        millisecondsToSeconds(totalTime),
        millisecondsToSeconds(warmupTime),
        millisecondsToSeconds(benchTime)));
    return averageRoundTime.avg;
  }

  private static double millisecondsToSeconds(double time) {
    return time / 1000.0;
  }

  public static void cleanupMemory() {
    System.gc();
    System.gc();
    Thread.yield();
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
