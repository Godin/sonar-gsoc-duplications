package org.sonar.duplications.benchmark.db;

import java.util.Collection;

import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.ByteArray;
import org.sonar.duplications.index.CloneIndex;

public interface BatchIndex extends CloneIndex {

  void init();

  void insert(Block block);

  void prepareCache(String resourceId);

  Collection<Block> getBySequenceHash(ByteArray sequenceHash);

  Collection<Block> getByResourceId(String resourceId);

}
