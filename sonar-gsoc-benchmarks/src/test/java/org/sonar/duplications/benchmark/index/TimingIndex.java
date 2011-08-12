package org.sonar.duplications.benchmark.index;

import java.util.Collection;
import java.util.Locale;

import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.ByteArray;
import org.sonar.duplications.index.AbstractCloneIndex;
import org.sonar.duplications.index.CloneIndex;

public class TimingIndex extends AbstractCloneIndex {

  private long timeGetByResourceId;
  private long timeGetByHash;
  private long timeInsert;

  private CloneIndex index;

  public TimingIndex(CloneIndex index) {
    this.index = index;
  }

  public Collection<Block> getByResourceId(String resourceId) {
    long time = System.currentTimeMillis();
    Collection<Block> result = index.getByResourceId(resourceId);
    timeGetByResourceId += System.currentTimeMillis() - time;
    return result;
  }

  public Collection<Block> getBySequenceHash(ByteArray sequenceHash) {
    long time = System.currentTimeMillis();
    Collection<Block> result = index.getBySequenceHash(sequenceHash);
    timeGetByHash += System.currentTimeMillis() - time;
    return result;
  }

  public void insert(Block block) {
    long time = System.currentTimeMillis();
    index.insert(block);
    timeInsert += System.currentTimeMillis() - time;
  }

  public void print() {
    System.out.println(String.format(Locale.ENGLISH, "insert: %.2f getByResourceId: %.2f getByHash: %.2f",
        timeInsert / 1000.0,
        timeGetByResourceId / 1000.0,
        timeGetByHash / 1000.0));
  }

}
