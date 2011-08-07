package org.sonar.duplications.algorithm.filter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.sonar.duplications.index.CloneGroup;
import org.sonar.duplications.index.ClonePart;
import org.sonar.duplications.interval.Interval;
import org.sonar.duplications.interval.IntervalTree;

import java.util.List;
import java.util.Map;

public class IntervalTreeCloneGroupFilter implements CloneGroupFilter {

  private final static class PartWrapper {
    public CloneGroup clone;
    public ClonePart part;

    private PartWrapper(CloneGroup clone, ClonePart part) {
      this.clone = clone;
      this.part = part;
    }

    public CloneGroup getClone() {
      return clone;
    }

    public ClonePart getPart() {
      return part;
    }
  }


  private static Map<String, IntervalTree> buildTrees(List<CloneGroup> clones) {
    Map<String, IntervalTree> trees = Maps.newHashMap();

    //populate interval tree structure
    for (CloneGroup clone : clones) {
      for (ClonePart part : clone.getCloneParts()) {
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

    return trees;
  }

  public List<CloneGroup> filter(List<CloneGroup> clones) {
    List<CloneGroup> filtered = Lists.newArrayList();
    Map<String, IntervalTree> trees = buildTrees(clones);

    for (CloneGroup clone : clones) {
      ClonePart originPart = clone.getOriginPart();
      IntervalTree tree = trees.get(originPart.getResourceId());

      int unitStart = originPart.getUnitStart();
      int unitEnd = originPart.getUnitStart() + clone.getCloneUnitLength() - 1;
      List<Interval> intervals = tree.getCoveringIntervals(unitStart, unitEnd);

      boolean covered = false;
      for (Interval<PartWrapper> interval : intervals) {
        CloneGroup foundClone = interval.getData().getClone();
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
