package org.sonar.duplications.api.codeunit.statement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.sonar.duplications.api.DuplicationsException;
import org.sonar.duplications.api.codeunit.token.IToken;
import org.sonar.duplications.api.codeunit.token.TokenProvider;
import org.sonar.duplications.api.provider.ProviderBase;
import org.sonar.duplications.api.sourcecode.ISourceCodeElement;

/**
 * @author sharif
 *
 */
public class StatementProvider extends 
	ProviderBase<ISourceCodeElement, Statement, DuplicationsException> implements Serializable {

	private final TokenProvider tokenProvider;

	/** The statement oracle used to detect statement boundaries. */
	private IStatementOracle statementOracle;

	private ISourceCodeElement currentElement;
	
	private final boolean storeTokens;

	private int indexInFile;

	private IToken lastToken;

	public StatementProvider(TokenProvider tokenProvider) {
		this(tokenProvider, false);
	}

	public StatementProvider(TokenProvider tokenProvider,
			boolean storeTokens) {
		this.tokenProvider = tokenProvider;
		this.storeTokens = storeTokens;
	}

	@Override
	public void init(ISourceCodeElement currentElement) throws DuplicationsException {
		tokenProvider.init(currentElement);
		statementOracle = currentElement.getLanguage().getStatementOracle();
		this.currentElement = currentElement;
	}

	@Override
	protected Statement provideNext() throws DuplicationsException {
		List<IToken> statementTokens = new ArrayList<IToken>();

		// determine first token in statement
		IToken firstToken = findStartOfStatement();
		if (firstToken == null) {
			return null;
		}

		statementTokens.add(firstToken);

		// determine rest of statement tokens
		IToken nextToken = tokenProvider.lookahead(1);
		while (inSameStatement(firstToken, nextToken)) {
			statementTokens.add(getNextToken());
			nextToken = tokenProvider.lookahead(1);
		}

		return new Statement(currentElement.getFile(), statementTokens, storeTokens, indexInFile++);
	}

	/**
	 * Retrieves the next token from the tokenProvider
	 */
	private IToken getNextToken() throws DuplicationsException {
		IToken nextToken = tokenProvider.getNext();
		/*if (lastToken != null) {
			indexInFile = 0;
		}*/
		lastToken = nextToken;
		return nextToken;
	}


	/**
	 * Finds the next token that starts a statement
	 */
	private IToken findStartOfStatement() throws DuplicationsException {
		IToken firstToken = getNextToken();
		while (firstToken != null
				&& statementOracle.isEndOfStatementToken(firstToken.getNormalizedContent())) {
			firstToken = getNextToken();
		}
		return firstToken;
	}

	/** Checks whether two tokens are part of the same statement */
	private boolean inSameStatement(IToken firstUnit, IToken nextUnit) {
		return nextUnit != null
				&& !statementOracle.isEndOfStatementToken(nextUnit.getNormalizedContent());
	}
}