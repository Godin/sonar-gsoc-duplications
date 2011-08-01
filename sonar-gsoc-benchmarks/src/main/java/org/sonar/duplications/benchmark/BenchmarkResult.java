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

public class BenchmarkResult {

  public final String name;
  public final int warmupRounds;
  public final int benchmarkRounds;
  public final Average time;
  public final Average memory;
  public final long warmupTime;
  public final long benchTime;

  BenchmarkResult(String name, int warmupRounds, int benchmarkRounds, long warmupTime, long benchTime, Average time, Average memory) {
    this.name = name;
    this.warmupRounds = warmupRounds;
    this.benchmarkRounds = benchmarkRounds;
    this.warmupTime = warmupTime;
    this.benchTime = benchTime;
    this.time = time;
    this.memory = memory;
  }

  @Override
  public String toString() {
    return String.format(Locale.ENGLISH, "%s [measured %d out of %d rounds] time: %.2f [+-%.2f], memory: %.2f [+-%.2f], total: %.2f, warm: %.2f, bench: %.2f",
        name,
        benchmarkRounds,
        warmupRounds + benchmarkRounds,
        millisecondsToSeconds(time.avg),
        millisecondsToSeconds(time.stddev),
        bytesToMegabytes(memory.avg),
        bytesToMegabytes(memory.stddev),
        millisecondsToSeconds(warmupTime + benchTime),
        millisecondsToSeconds(warmupTime),
        millisecondsToSeconds(benchTime));
  }

  private static double millisecondsToSeconds(double value) {
    return value / 1000.0;
  }

  private static double bytesToMegabytes(double value) {
    return value / 1024 / 1024;
  }

}
