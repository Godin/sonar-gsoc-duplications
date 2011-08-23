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
