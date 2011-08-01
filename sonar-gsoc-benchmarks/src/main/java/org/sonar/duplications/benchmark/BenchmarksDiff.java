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

import java.util.List;

import com.google.common.collect.Lists;

public class BenchmarksDiff {

  private BenchmarkResult reference;
  private final List<BenchmarkResult> results = Lists.newArrayList();
  
  public BenchmarksDiff() {
  }

  public BenchmarksDiff(BenchmarkResult reference) {
    this.reference = reference;
  }

  public void setReference(BenchmarkResult reference) {
    this.reference = reference;
  }

  public void add(BenchmarkResult result) {
    results.add(result);
  }

  public void print() {
    if (reference == null) {
      return;
    }
    double[] time = new double[results.size()];
    double[] memory = new double[results.size()];
    for (int i = 0; i < results.size(); i++) {
      BenchmarkResult result = results.get(i);
      time[i] = result.time.avg;
      memory[i] = result.memory.avg;
    }
    System.out.println("Time   " + Difference.from(reference.time.avg, time));
    System.out.println("Memory " + Difference.from(reference.memory.avg, memory));
  }
  
}
