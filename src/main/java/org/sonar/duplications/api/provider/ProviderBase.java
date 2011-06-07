package org.sonar.duplications.api.provider;


import java.io.Serializable;
import java.util.LinkedList;


/**
 * @param <INPUT>
 * @param <OUTPUT>
 * @author sharif
 */
public abstract class ProviderBase<INPUT, OUTPUT> implements IProvider<INPUT, OUTPUT>, Serializable {

  private static final long serialVersionUID = 7122845590366986913L;
  /**
   * This list stores data that have been accessed by
   * {@link #lookahead(int)} but have not yet been retrieved using
   * {@link #getNext()}
   */
  private final LinkedList<OUTPUT> lookaheadBuffer = new LinkedList<OUTPUT>();


  /**
   * Template method that allows deriving classes to perform their
   * initialization
   */
  public abstract void init(INPUT root);

  /**
   * Returns an item ahead of the current position, without actually
   * retrieving it. The first item to be looked ahead at has index 1.
   */
  public OUTPUT lookahead(int index) {
    while (index > lookaheadBuffer.size()) {
      OUTPUT data = provideNext();
      if (data == null) {
        return null;
      }
      lookaheadBuffer.add(data);
    }

    return lookaheadBuffer.get(index - 1);
  }

  public OUTPUT getNext() {
    if (lookaheadBuffer.size() > 0) {
      return lookaheadBuffer.poll();
    }
    return provideNext();
  }

  /**
   * Template method that providers implement to yield elements
   *
   * @return output element
   */
  protected abstract OUTPUT provideNext();

}