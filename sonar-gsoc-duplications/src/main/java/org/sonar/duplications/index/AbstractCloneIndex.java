package org.sonar.duplications.index;

import java.util.Collection;

import org.sonar.duplications.block.Block;

public abstract class AbstractCloneIndex implements CloneIndex {

  public Collection<String> getAllUniqueResourceId() {
    throw new UnsupportedOperationException();
  }

  public boolean containsResourceId(String resourceId) {
    throw new UnsupportedOperationException();
  }

  public void remove(String resourceId) {
    throw new UnsupportedOperationException();
  }

  public void remove(Block block) {
    throw new UnsupportedOperationException();
  }

  public void removeAll() {
    throw new UnsupportedOperationException();
  }

  public int size() {
    throw new UnsupportedOperationException();
  }

}
