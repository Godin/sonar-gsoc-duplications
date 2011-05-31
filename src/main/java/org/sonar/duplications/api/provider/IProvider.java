package org.sonar.duplications.api.provider;

import org.sonar.duplications.api.sourcecode.ISourceCodeElement;

/**
 * @author sharif
 *
 * @param <Element>
 * @param <Data>
 * @param <X>
 */
public interface IProvider<Element extends ISourceCodeElement, Data, X extends Exception> {

	public Data getNext() throws X;

	public Data lookahead(int index) throws X;

	public void init(Element root) throws X;

}