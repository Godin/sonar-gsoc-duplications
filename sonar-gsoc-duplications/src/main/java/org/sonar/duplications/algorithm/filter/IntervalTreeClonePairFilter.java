package org.sonar.duplications.algorithm.filter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.sonar.duplications.index.ClonePair;
import org.sonar.duplications.index.ClonePart;
import org.sonar.duplications.index.ClonePartContainerBase;

import java.util.List;
import java.util.Map;

public class IntervalTreeClonePairFilter extends AbstractIntervalTreeCloneFilter {

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
      IntervalTree tree = trees.get(clonePair.getAnotherPart().getResourceId());

      if (!isCovered(tree, clone)) {
        filtered.add(clone);
      }
    }
    return filtered;
  }

}
