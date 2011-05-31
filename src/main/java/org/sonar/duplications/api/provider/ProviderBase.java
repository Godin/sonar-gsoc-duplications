package org.sonar.duplications.api.provider;


import java.io.Serializable;
import java.util.LinkedList;

import org.sonar.duplications.api.sourcecode.ISourceCodeElement;

/**
 * @author sharif
 *
 * @param <Element>
 * @param <Data>
 * @param <X>
 */
public abstract class ProviderBase<Element extends ISourceCodeElement, Data, X extends Exception>
		implements IProvider<Element, Data, X>, Serializable {

	/**
	 * This list stores data that have been accessed by
	 * {@link #lookahead(int)} but have not yet been retrieved using
	 * {@link #getNext()}
	 */
	private final LinkedList<Data> lookaheadBuffer = new LinkedList<Data>();


	/**
	 * Template method that allows deriving classes to perform their
	 * initialization
	 */
	public abstract void init(Element root) throws X;

	/**
	 * Returns an item ahead of the current position, without actually
	 * retrieving it. The first item to be looked ahead at has index 1.
	 */
	public Data lookahead(int index) throws X {
		while (index > lookaheadBuffer.size()) {
			Data data = provideNext();
			if (data == null) {
				return null;
			}
			lookaheadBuffer.add(data);
		}

		return lookaheadBuffer.get(index - 1);
	}

	public Data getNext() throws X {
		if (lookaheadBuffer.size() > 0) {
			return lookaheadBuffer.poll();
		}
		return provideNext();
	}

	/** Template method that providers implement to yield elements */
	protected abstract Data provideNext() throws X;

}