package org.sonar.duplications.algorithm.filter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.sonar.duplications.algorithm.interval.Interval;
import org.sonar.duplications.algorithm.interval.IntervalTree;
import org.sonar.duplications.index.ClonePart;
import org.sonar.duplications.index.ClonePartContainerBase;

import java.util.List;
import java.util.Map;

public class IntervalTreeCloneFilter implements CloneFilter {

  private final static class PartWrapper<T extends ClonePartContainerBase> {
    public T clone;
    public ClonePart part;

    private PartWrapper(T clone, ClonePart part) {
      this.clone = clone;
      this.part = part;
    }

    public T getClone() {
      return clone;
    }

  }

  private static Map<String, IntervalTree> buildTrees(List<? extends ClonePartContainerBase> clones) {
    Map<String, IntervalTree> trees = Maps.newHashMap();

    //populate interval tree structure
    for (ClonePartContainerBase clone : clones) {
      String originResourceId = clone.getOriginPart().getResourceId();
      List<ClonePart> parts = clone.getCloneParts();
      for (ClonePart part : parts) {
        if (part.getResourceId().equals(originResourceId)) {
          PartWrapper partWrap = new PartWrapper(clone, part);
          IntervalTree tree = trees.get(part.getResourceId());
          if (tree == null) {
            tree = new IntervalTree();
            trees.put(part.getResourceId(), tree);
          }
          int unitStart = part.getUnitStart();
          int unitEnd = part.getUnitStart() + clone.getCloneUnitLength() - 1;

          tree.addInterval(new Interval(unitStart, unitEnd, partWrap));
        }
      }
    }

    return trees;
  }

  public <T extends ClonePartContainerBase> List<T> filter(List<T> clones) {
    List<T> filtered = Lists.newArrayList();
    Map<String, IntervalTree> trees = buildTrees(clones);

    for (T clone : clones) {
      ClonePart originPart = clone.getOriginPart();
      IntervalTree tree = trees.get(originPart.getResourceId());

      int unitStart = originPart.getUnitStart();
      int unitEnd = originPart.getUnitStart() + clone.getCloneUnitLength() - 1;
      List<Interval> intervals = tree.getCoveringIntervals(unitStart, unitEnd);

      boolean covered = false;
      for (Interval<PartWrapper<T>> interval : intervals) {
        T foundClone = interval.getData().getClone();
        if (foundClone.equals(clone)) {
          continue;
        }

        covered |= clone.containsIn(foundClone);
        if (covered) {
          break;
        }
      }

      if (!covered) {
        filtered.add(clone);
      }
    }
    return filtered;
  }

}
