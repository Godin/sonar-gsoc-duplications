package org.sonar.duplications.api.codeunit;

import java.io.Serializable;
import java.util.List;


/**
 * @author sharif
 */
public class Statement implements Serializable {

  private static final long serialVersionUID = 535333895463882703L;

  /**
   * List of tokens this statement comprises.
   */
  //private Token[] tokens;

  private final int startLine;

  private final int endLine;

  private final String originalContent;

  private final String normalizedContent;

  private final int indexInFile;

  public Statement(int startLine, int endLine, String originalContent,
                   int indexInFile) {
    this(startLine, endLine, originalContent, originalContent, indexInFile);
  }

  public Statement(int startLine, int endLine, String originalContent,
                   String normalizedContent, int indexInFile) {
    this.startLine = startLine;
    this.originalContent = originalContent.intern();
    this.normalizedContent = normalizedContent.intern();
    this.endLine = endLine;
    this.indexInFile = indexInFile;
  }

  public Statement(int startLine, int endLine, List<Token> tokenList, boolean storeTokens,
                   int indexInFile) {
    this(startLine, endLine, createUnnormalizedContent(tokenList),
        createNormalizedContentContent(tokenList), indexInFile);
//		if (storeTokens) {
//			tokens = tokenList.toArray(new Token[] {});
//		}
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
    return originalContent.hashCode() + startLine + endLine + indexInFile;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Statement) {
      Statement other = (Statement) obj;
      return originalContent.equals(other.originalContent)
          && startLine == other.startLine
          && endLine == other.endLine
          && indexInFile == other.indexInFile;
    }
    return false;

  }

  private static String createNormalizedContentContent(List<Token> tokens) {
    StringBuilder builder = new StringBuilder();
    for (Token token : tokens) {
      builder.append(token.getNormalizedContent());
    }
    return builder.toString();
  }

  private static String createUnnormalizedContent(List<Token> tokens) {
    StringBuilder builder = new StringBuilder();
    for (Token token : tokens) {
      builder.append(token.getOriginalContent());
    }
    return builder.toString();
  }

  @Override
  public String toString() {
    return "[" + getStartLine() + "-" + getEndLine()
        + "][index:" + getIndexInFile() + "] [" + getOriginalContent() + "] [" + getNormalizedContent() + "]";
  }

  /*public Token[] getTokens() {
     assertTokensStored();
     return tokens;
   }

   *//** Throws an {@link IllegalStateException}, if tokens have not been stored *//*
	private void assertTokensStored() {
		if (tokens == null) {
			throw new IllegalStateException(
					"In order to access the underlying tokens, Statement must store its tokens. "
							+ "(Set storeTokens flag in constructor)");
		}
	}*/

}