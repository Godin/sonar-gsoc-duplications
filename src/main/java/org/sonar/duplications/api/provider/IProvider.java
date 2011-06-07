package org.sonar.duplications.api.provider;

/**
 * @param <INPUT>
 * @param <OUTPUT>
 * @author sharif
 */
public interface IProvider<INPUT, OUTPUT> {

  public OUTPUT getNext();

  public OUTPUT lookahead(int index);

  public void init(INPUT root);

}