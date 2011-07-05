package org.sonar.duplications.statement.matcher;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sonar.duplications.token.Token;
import org.sonar.duplications.token.TokenQueue;

/**
 * match upto any of the specified token
 * 
 * @author sharif
 * 
 */
public class UptoTokenMatcher extends TokenMatcher {

  private Set<String> uptoMatchTokens = new HashSet<String>();

  public UptoTokenMatcher(String[] uptoMatchTokens) {
    for (String uptoMatchToken : uptoMatchTokens) {
      this.uptoMatchTokens.add(uptoMatchToken);
    }
  }

  @Override
  public boolean matchToken(TokenQueue tokenQueue, List<Token> pendingStatement) {
    do {
      Token token = tokenQueue.poll();

      pendingStatement.add(token);
      if (uptoMatchTokens.contains(token.getValue())) {
        return true;
      }
    } while (tokenQueue.peek() != null);
    return false;
  }
}
