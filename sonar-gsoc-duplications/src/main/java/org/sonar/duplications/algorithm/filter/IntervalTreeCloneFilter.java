package org.sonar.duplications.algorithm.filter;

import com.google.common.collect.Lists;
import org.sonar.duplications.index.ClonePart;
import org.sonar.duplications.index.ClonePartContainerBase;

import java.util.List;

public class IntervalTreeCloneFilter extends AbstractIntervalTreeCloneFilter {

  private static <T extends ClonePartContainerBase> IntervalTree buildTrees(List<T> clones) {
    IntervalTree originTree = new IntervalTree();

    //populate interval tree structure
    for (T clone : clones) {
      String originResourceId = clone.getOriginPart().getResourceId();
      List<ClonePart> parts = clone.getCloneParts();
      for (ClonePart part : parts) {
        if (part.getResourceId().equals(originResourceId)) {
          PartWrapper partWrap = new PartWrapper(clone, part);
          int unitStart = part.getUnitStart();
          int unitEnd = part.getUnitStart() + clone.getCloneUnitLength() - 1;

          originTree.addInterval(new Interval(unitStart, unitEnd, partWrap));
        }
      }
    }
    return originTree;
  }

  public <T extends ClonePartContainerBase> List<T> filter(List<T> clones) {
    List<T> filtered = Lists.newArrayList();
    IntervalTree tree = buildTrees(clones);

    for (T clone : clones) {
      if (!isCovered(tree, clone)) {
        filtered.add(clone);
      }
    }
    return filtered;
  }

}
