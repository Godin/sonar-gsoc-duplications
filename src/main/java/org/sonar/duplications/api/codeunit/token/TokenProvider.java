package org.sonar.duplications.api.codeunit.token;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.sonar.duplications.api.DuplicationsException;
import org.sonar.duplications.api.lexer.ELanguage;
import org.sonar.duplications.api.lexer.Lexer;
import org.sonar.duplications.api.lexer.LexterFactory;
import org.sonar.duplications.api.provider.ProviderBase;
import org.sonar.duplications.api.sourcecode.ISourceCodeElement;

/**
 * @author sharif
 *
 */
public class TokenProvider extends ProviderBase<ISourceCodeElement, IToken, DuplicationsException> implements Serializable {

//	private final ISourceCodeElementProvider<ISourceCodeElement> sourceCodeElementProvider;

	private Iterator<Token> tokenIterator;

	private ISourceCodeElement currentElement;
	
	private final Lexer lexer;
	
	public TokenProvider(/*ISourceCodeElementProvider<ISourceCodeElement> sourceCodeElementProvider,*/ Lexer lexer) {
		//this.sourceCodeElementProvider = sourceCodeElementProvider;
		this.lexer = lexer;
	}

	public TokenProvider(/*ISourceCodeElementProvider<ISourceCodeElement> sourceCodeElementProvider, */ELanguage language) {
		//this.sourceCodeElementProvider = sourceCodeElementProvider;
		this.lexer = LexterFactory.getLexer(language);
	}

	@Override
	public void init(ISourceCodeElement currentElement){
		//sourceCodeElementProvider.init(root);
		this.currentElement  = currentElement;
	}
	
	@Override
	protected IToken provideNext() throws DuplicationsException {

		// token iterator is initialized and has more tokens
		if (tokenIterator != null && tokenIterator.hasNext()) {
			return tokenIterator.next();
		}

		// move to next file: this feature has been moved to block level
		//ISourceCodeElement currentElement = sourceCodeElementProvider.getNext();

		// if there are no more files, we're done
		if (currentElement == null) {
			return null;
		}

		// get list of tokens in the current file
		List<Token> tokens = readTokensFrom(currentElement);

		// ignore empty source files
		if (tokens.isEmpty()) {
			tokenIterator = null;
			// recursive call to get next valid token
			return getNext();
		}

		// set iterator and call recursively
		tokenIterator = tokens.iterator();
		
		//done with this file
		currentElement = null;
		
		return getNext();
	}

	/** Read all tokens from the an {@link ISourceCodeElement} into a list. */
	private List<Token> readTokensFrom(ISourceCodeElement element) {
		if (element.getFile() == null) {
			return Collections.emptyList();
		}

		List<Token> tokens = new ArrayList<Token>();
		try {
			tokens = lexer.lex(element.getFile());
		} catch (Exception e) {
			throw new DuplicationsException("Could not read tokens from element '" + element.getFile()
					+ "':" + e.getMessage());
		}

		return tokens;
	}

}