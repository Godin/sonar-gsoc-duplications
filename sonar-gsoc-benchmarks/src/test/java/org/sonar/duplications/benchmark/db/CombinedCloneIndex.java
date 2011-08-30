package org.sonar.duplications.benchmark.db;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.ByteArray;
import org.sonar.duplications.index.AbstractCloneIndex;
import org.sonar.duplications.index.CloneIndex;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class CombinedCloneIndex extends AbstractCloneIndex {

  private final CloneIndex first;
  private final CloneIndex second;

  /**
   * Ids of all resources, which were indexed.
   */
  private final Set<String> resourceIds = Sets.newHashSet();

  public CombinedCloneIndex(CloneIndex first, CloneIndex second) {
    this.first = first;
    this.second = second;
  }

  public Collection<Block> getByResourceId(String resourceId) {
    return first.getByResourceId(resourceId);
  }

  public Collection<Block> getBySequenceHash(ByteArray sequenceHash) {
    List<Block> result = Lists.newArrayList();
    result.addAll(first.getBySequenceHash(sequenceHash));
    for (Block block : second.getBySequenceHash(sequenceHash)) {
      if (!resourceIds.contains(block.getResourceId())) {
        result.add(block);
      }
    }
    return result;
  }

  public void insert(Block block) {
    resourceIds.add(block.getResourceId());
    first.insert(block);
    second.insert(block);
  }

}
