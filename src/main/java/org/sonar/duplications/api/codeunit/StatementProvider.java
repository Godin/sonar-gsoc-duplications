package org.sonar.duplications.api.codeunit;

import org.sonar.duplications.api.DuplicationsException;
import org.sonar.duplications.api.lexer.Lexer;
import org.sonar.duplications.api.lexer.family.StatementExtractor;
import org.sonar.duplications.api.provider.ProviderBase;

import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * @author sharif
 */
public class StatementProvider extends ProviderBase<File, Statement> {

  private static final long serialVersionUID = -3976100935262138933L;

  private int indexInFile = 0;

  private final Lexer tokenizer;

  private StatementExtractor statementExtractor;

  private Iterator<Token> statementIterator;

  private File currentElement;

  public StatementProvider(Lexer tokenizer,
                           StatementExtractor statementExtractor) {
    this.tokenizer = tokenizer;
    this.statementExtractor = statementExtractor;
  }

  @Override
  public void init(File currentElement) throws DuplicationsException {
    this.currentElement = currentElement;
    indexInFile = 0;
  }

  @Override
  protected Statement provideNext() throws DuplicationsException {
    if (statementIterator != null && statementIterator.hasNext()) {
      Token originalStatement = statementIterator.next();
      //for multi-line statement original statement stores only toLine
      int toLine = originalStatement.getLine();
      int fromLine = toLine - originalStatement.getOriginalContent().split("\n").length + 1;
      return new Statement(tokenizer.lex(originalStatement.getOriginalContent()), indexInFile++);
    }

    if (currentElement == null) {
      return null;
    }

    // get list of statement in the current file
    List<Token> statements = statementExtractor
        .extractStatement(currentElement);

    // ignore empty source files
    if (statements.isEmpty()) {
      statementIterator = null;
      // recursive call to get next valid token
      return getNext();
    }

    // set iterator and call recursively
    statementIterator = statements.iterator();

    // done with this file
    currentElement = null;

    return getNext();
  }

}