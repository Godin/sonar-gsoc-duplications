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

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * See article <a href="http://www.informit.com/guides/content.aspx?g=java&seqNum=249">"Memory Monitoring with Java SE 5"</a>.
 */
public final class MemoryUtils {

  private static final List<MemoryPoolMXBean> HEAP_POOLS = Lists.newArrayList();

  static {
    for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
      if (pool.getType() == MemoryType.HEAP) {
        HEAP_POOLS.add(pool);
      }
    }
  }

  private MemoryUtils() {
  }

  /**
   * @return returns peak of used heap memory in bytes, which has been achieved since the start of JVM or since reset of peak.
   * @see #resetPeakUsage()
   */
  public static long getPeakUsage() {
    long sum = 0;
    for (MemoryPoolMXBean pool : HEAP_POOLS) {
      sum += pool.getPeakUsage().getUsed();
    }
    return sum;
  }

  public static void resetPeakUsage() {
    for (MemoryPoolMXBean pool : HEAP_POOLS) {
      pool.resetPeakUsage();
    }
  }

  public static void cleanup() {
    System.gc();
    System.gc();
    Thread.yield();
  }

}
