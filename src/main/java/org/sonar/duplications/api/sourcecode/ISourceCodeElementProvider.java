package org.sonar.duplications.api.sourcecode;

import org.sonar.duplications.api.DuplicationsException;
import org.sonar.duplications.api.provider.IProvider;
import org.sonar.duplications.api.sourcecode.ISourceCodeElement;

/**
 * @author sharif
 *
 * @param <E>
 */
public interface ISourceCodeElementProvider<E extends ISourceCodeElement> extends
		IProvider<E, E, DuplicationsException> {
	// Nothing to do
}