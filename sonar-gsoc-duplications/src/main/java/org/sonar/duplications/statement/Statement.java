package org.sonar.duplications.statement;

import org.sonar.duplications.DuplicationsException;
import org.sonar.duplications.token.Token;

import java.util.List;

/**
 * @author sharif
 */
public class Statement {

  private final int startLine;

  private final int endLine;

  private final String value;

  private int indexInFile;

  public Statement(List<Token> tokenList) {
    if (tokenList == null || tokenList.size() == 0) {
      throw new DuplicationsException("A statement can't be initialized with an empty list of token");
    }
    int fromLine = tokenList.get(0).getLine();
    int toLine = tokenList.get(tokenList.size() - 1).getLine();
    StringBuilder tmpValue = new StringBuilder();
    for (int i = 0; i < tokenList.size(); i++) {
      tmpValue.append(tokenList.get(i).getValue());
    }
    this.startLine = fromLine;
    this.endLine = toLine;
    this.value = tmpValue.toString();
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

  public void setIndexInFile(int indexInFile) {
    this.indexInFile = indexInFile;
  }

  public String getValue() {
    return value;
  }

  @Override
  public int hashCode() {
    return value.hashCode() + startLine + endLine + indexInFile;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Statement) {
      Statement other = (Statement) obj;
      return indexInFile == other.indexInFile && value.equals(other.value);
    }
    return false;

  }

  @Override
  public String toString() {
    return "[" + getStartLine() + "-" + getEndLine() + "][index:" + getIndexInFile() + "] [" + getValue() + "]";
  }
}