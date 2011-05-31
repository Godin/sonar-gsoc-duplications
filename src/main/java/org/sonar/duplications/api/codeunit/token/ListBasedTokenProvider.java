package org.sonar.duplications.api.codeunit.token;


import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.sonar.duplications.api.DuplicationsException;
import org.sonar.duplications.api.provider.ProviderBase;
import org.sonar.duplications.api.sourcecode.ISourceCodeElement;


/**
 * @author sharif
 *
 */
public class ListBasedTokenProvider extends ProviderBase<ISourceCodeElement, IToken, DuplicationsException> implements
		Serializable {
	/** Iterator that keeps track of position in tokens list */
	private Iterator<IToken> tokenIterator;

	/** Constructor */
	public ListBasedTokenProvider(List<IToken> tokens) {
		tokenIterator = tokens.iterator();
	}

	/** {@inheritDoc} */
	@Override
	public void init(ISourceCodeElement root) {
		// Do nothing
	}

	/** {@inheritDoc} */
	@Override
	protected IToken provideNext() {
		if (tokenIterator != null && tokenIterator.hasNext()) {
			return tokenIterator.next();
		}

		tokenIterator = null;
		return null;
	}
}