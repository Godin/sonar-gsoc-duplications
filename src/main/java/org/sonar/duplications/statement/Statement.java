package org.sonar.duplications.statement;

import java.io.Serializable;
import java.util.List;

import org.sonar.duplications.token.Token;

/**
 * @author sharif
 */
public class Statement implements Serializable {

  private static final long serialVersionUID = 535333895463882703L;

  /**
   * List of tokens this statement comprises.
   */
  // private Token[] tokens;

  private final int startLine;

  private final int endLine;

  private final String originalContent;

  private final String normalizedContent;

  private final int indexInFile;

  public Statement(int startLine, int endLine, String originalContent, int indexInFile) {
    this(startLine, endLine, originalContent, originalContent, indexInFile);
  }

  public Statement(int startLine, int endLine, String originalContent, String normalizedContent, int indexInFile) {
    this.startLine = startLine;
    this.originalContent = originalContent.intern();
    this.normalizedContent = normalizedContent.intern();
    this.endLine = endLine;
    this.indexInFile = indexInFile;
  }

  public Statement(List<Token> tokenList, int indexInFile) {
    // if (storeTokens) {
    // tokens = tokenList.toArray(new Token[] {});
    // }
    int fromLine = -1;
    int toLine = -1;
    StringBuilder sbNormalizedContent = new StringBuilder();
    StringBuilder sbOriginalContent = new StringBuilder();
    for (int i = 0; i < tokenList.size(); i++) {
      Token token = tokenList.get(i);
      if (i == 0)
        fromLine = token.getLine();
      if (i == tokenList.size() - 1)
        toLine = token.getLine();
      sbNormalizedContent.append(token.getValue());
      sbOriginalContent.append(token.getValue());
    }
    this.startLine = fromLine;
    this.endLine = toLine;
    this.originalContent = sbOriginalContent.toString().intern();
    this.normalizedContent = sbNormalizedContent.toString().intern();
    this.indexInFile = indexInFile;
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
      return originalContent.equals(other.originalContent) && startLine == other.startLine && endLine == other.endLine
          && indexInFile == other.indexInFile;
    }
    return false;

  }

  @Override
  public String toString() {
    return "[" + getStartLine() + "-" + getEndLine() + "][index:" + getIndexInFile() + "] [" + getOriginalContent() + "] ["
        + getNormalizedContent() + "]";
  }
}