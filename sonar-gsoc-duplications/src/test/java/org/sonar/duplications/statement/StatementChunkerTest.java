package org.sonar.duplications.statement;

import org.junit.Test;
import org.sonar.duplications.token.Token;
import org.sonar.duplications.token.TokenQueue;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.sonar.duplications.statement.TokenMatcherFactory.anyToken;
import static org.sonar.duplications.statement.TokenMatcherFactory.token;

/**
 * Created by IntelliJ IDEA.
 * User: skydiver
 * Date: 13.07.11
 * Time: 12:04
 */
public class StatementChunkerTest {
  @Test
  public void testStatementIndexInFile() {
    TokenQueue tokenQueue = new TokenQueue();
    tokenQueue.add(new Token("a", 1, 1));
    tokenQueue.add(new Token("x", 2, 1));
    tokenQueue.add(new Token("y", 3, 1));
    tokenQueue.add(new Token("u", 4, 1));
    tokenQueue.add(new Token("b", 5, 1));
    tokenQueue.add(new Token("u", 6, 1));

    // reorder token matchers in comparsion with tokenQueue
    StatementChunker chunker = StatementChunker
        .builder()
        .addChannel(token("y"))
        .addChannel(token("u"))
        .addChannel(token("x"))
        .addChannel(token("a"))
        .addChannel(anyToken())
        .build();

    List<Statement> list = chunker.chunk(tokenQueue);
    int counter = 0;
    for (Statement st : list) {
      assertThat(st.getIndexInFile(), is(counter++));
    }
  }
}
