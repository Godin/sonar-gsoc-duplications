package org.sonar.duplications.algorithm.filter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.sonar.duplications.algorithm.interval.Interval;
import org.sonar.duplications.algorithm.interval.IntervalTree;
import org.sonar.duplications.index.ClonePair;
import org.sonar.duplications.index.ClonePart;
import org.sonar.duplications.index.ClonePartContainerBase;

import java.util.List;
import java.util.Map;

public class IntervalTreeClonePairFilter implements CloneFilter {

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

  private static <T extends ClonePartContainerBase> Map<String, IntervalTree> buildTrees(List<T> clones) {
    Map<String, IntervalTree> trees = Maps.newHashMap();

    //populate interval tree structure
    for (T clone : clones) {
      ClonePair clonePair = (ClonePair) clone;
      String originResourceId = clonePair.getOriginPart().getResourceId();
      String otherResourceId = clonePair.getAnotherPart().getResourceId();
      IntervalTree tree = trees.get(otherResourceId);
      if (tree == null) {
        tree = new IntervalTree();
        trees.put(otherResourceId, tree);
      }
      List<ClonePart> parts = clone.getCloneParts();
      for (ClonePart part : parts) {
        if (part.getResourceId().equals(originResourceId)) {
          PartWrapper partWrap = new PartWrapper(clone, part);
          int unitStart = part.getUnitStart();
          int unitEnd = part.getUnitStart() + clone.getCloneUnitLength() - 1;

          tree.addInterval(new Interval(unitStart, unitEnd, partWrap));
        }
      }
    }

    return trees;
  }

  public <T extends ClonePartContainerBase> List<T> filter(List<T> clones) {
    if (clones.isEmpty()) {
      return clones;
    }
    if (!(clones.get(0) instanceof ClonePair)) {
      return clones;
    }
    List<T> filtered = Lists.newArrayList();
    Map<String, IntervalTree> trees = buildTrees(clones);

    for (T clone : clones) {
      ClonePair clonePair = (ClonePair) clone;
      ClonePart originPart = clonePair.getOriginPart();
      ClonePart otherPart = clonePair.getAnotherPart();
      IntervalTree tree = trees.get(otherPart.getResourceId());

      int unitStart = originPart.getUnitStart();
      int unitEnd = originPart.getUnitStart() + clone.getCloneUnitLength() - 1;

      List<Interval> intervals = tree.getCoveringIntervals(unitStart, unitEnd);

      boolean covered = false;
      for (Interval<PartWrapper<T>> interval : intervals) {
        T foundClone = interval.getData().getClone();
        if (foundClone.equals(clone)) {
          continue;
        }
        if (clone.containsIn(foundClone)) {
          covered = true;
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
