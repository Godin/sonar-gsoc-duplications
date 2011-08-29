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

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

public final class GCSnapshot {
  private static List<GarbageCollectorMXBean> garbageBeans = ManagementFactory.getGarbageCollectorMXBeans();

  private long[] gcInvocations = new long[garbageBeans.size()];
  private long[] gcTimes = new long[garbageBeans.size()];

  public GCSnapshot() {
    for (int i = 0; i < gcInvocations.length; i++) {
      gcInvocations[i] = garbageBeans.get(i).getCollectionCount();
      gcTimes[i] = garbageBeans.get(i).getCollectionTime();
    }
  }

  public long accumulatedInvocations() {
    long sum = 0;
    int i = 0;
    for (GarbageCollectorMXBean bean : garbageBeans) {
      sum += bean.getCollectionCount() - gcInvocations[i++];
    }
    return sum;
  }

  public long accumulatedTime() {
    long sum = 0;
    int i = 0;
    for (GarbageCollectorMXBean bean : garbageBeans) {
      sum += bean.getCollectionTime() - gcTimes[i++];
    }
    return sum;
  }

  @Override
  public String toString() {
    return "GC invocations: " + accumulatedInvocations() + " time: " + accumulatedTime();
  }

}
