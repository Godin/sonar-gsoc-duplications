/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2011 SonarSource
 * Written (W) 2011 Andrew Tereskin
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
package org.sonar.duplications.algorithm;


import com.google.common.collect.Maps;

import java.util.Map;

public class StatsCollector {

  private Map<String, Long> workingTimes;
  private Map<String, Long> startTimes;
  private Map<String, Double> statNumbers;

  private String name;

  public StatsCollector(String name) {
    this.workingTimes = Maps.newHashMap();
    this.startTimes = Maps.newHashMap();
    this.statNumbers = Maps.newHashMap();
    this.name = name;
  }

  public void startTime(String key) {
    startTimes.put(key, System.currentTimeMillis());
  }

  public void stopTime(String key) {
    long startTime = startTimes.get(key);
    long prevTime = 0;
    if (workingTimes.containsKey(key)) {
      prevTime = workingTimes.get(key);
    }
    prevTime += System.currentTimeMillis() - startTime;
    workingTimes.put(key, prevTime);
  }

  public void addNumber(String key, double value) {
    double prev = 0;
    if (statNumbers.containsKey(key)) {
      prev = statNumbers.get(key);
    }
    statNumbers.put(key, prev + value);
  }

  public void printTimeStatistics() {
    System.out.println("---- Time statistics for " + name);
    long total = 0;
    for (String key : workingTimes.keySet()) {
      total += workingTimes.get(key);
    }
    for (String key : workingTimes.keySet()) {
      long time = workingTimes.get(key);
      double percentage = 100.0 * time / total;
      double seconds = time / 1000.0;
      System.out.println("Working time for '" + key + "': " + seconds + " - " + percentage + "%");
    }
  }

  public void printNumberStatistics() {
    System.out.println("---- Number statistics for " + name);

    for (String key : statNumbers.keySet()) {
      double val = statNumbers.get(key);
      System.out.println("Number statistics for '" + key + "': " + val);
    }
  }

  public void printAllStatistics() {

    printTimeStatistics();

    printNumberStatistics();
  }

}
