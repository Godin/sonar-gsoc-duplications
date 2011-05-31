package org.sonar.duplications.api.codeunit.statement;

import java.io.File;
import java.util.List;

import org.sonar.duplications.api.codeunit.token.IToken;
import org.sonar.duplications.api.codeunit.token.Token;

/**
 * @author sharif
 *
 */
public class Statement {

	/** List of tokens this statement comprises. */
	private Token[] tokens;

	private final File sourceFile;

	private final int startLine;

	private final int endLine;

	private final String originalContent;

	private final String normalizedContent;
	
	private final int indexInFile ;

	public Statement(File file, int startLine, int endLine, String originalContent,
			 int indexInFile) {
		this(file, startLine,  endLine, originalContent, originalContent, indexInFile);
	}

	public Statement(File file, int startLine, int endLine, String originalContent, 
			String normalizedContent, int indexInFile) {
		this.startLine = startLine;
		this.sourceFile = file;
		this.originalContent = originalContent.intern();
		this.normalizedContent = normalizedContent.intern();
		this.endLine = endLine;
		this.indexInFile = indexInFile;
	}
	
	public Statement(File file, List<IToken> tokenList, boolean storeTokens,
			int indexInFile) {
		this(file, tokenList.get(0).getLine(), tokenList.get(tokenList.size()-1).getLine(), createContent(tokenList),
				createUnnormalizedContent(tokenList),indexInFile);
		if (storeTokens) {
			tokens = tokenList.toArray(new Token[] {});
		}
	}
	
	public File getFile() {
		return sourceFile;
	}

	public int getStartLine() {
		return startLine;
	}

	public int getEndLine() {
		return endLine;
	}

	public int getIndexInFile() {
		return indexInFile;
	}

	public String getOriginalContent() {
		return originalContent;
	}

	public String getNormalizedContent() {
		return normalizedContent;
	}

	@Override
	public int hashCode() {
		return originalContent.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Statement)) {
			return false;
		}
		return originalContent == ((Statement) other).originalContent;
	}

	private static String createContent(List<IToken> tokens) {
		StringBuilder builder = new StringBuilder();
		for (IToken token : tokens) {
			builder.append(token.getNormalizedContent());
		}
		return builder.toString();
	}

	private static String createUnnormalizedContent(List<IToken> tokens) {
		StringBuilder builder = new StringBuilder();
		for (IToken token : tokens) {
			builder.append(token.getOriginalContent());
		}
		return builder.toString();
	}

	@Override
	public String toString() {
		return getOriginalContent() + " [" + getFile() + "(" + getStartLine()
				+ ")][index:" + getIndexInFile() + "]";
	}

	public Token[] getTokens() {
		assertTokensStored();
		return tokens;
	}

	/** Throws an {@link IllegalStateException}, if tokens have not been stored */
	private void assertTokensStored() {
		if (tokens == null) {
			throw new IllegalStateException(
					"In order to access the underlying tokens, Statement must store its tokens. "
							+ "(Set storeTokens flag in constructor)");
		}
	}

}