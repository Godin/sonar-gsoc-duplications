/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2011 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.duplications.token;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.sonar.channel.ChannelException;
import org.sonar.duplications.api.Token;

/**
 * class that maintains a queue of tokens, supports methods pop: returns head token and remove it from queue peek: returns head token
 * without remove it from queue lookahead: returns a token from the queue with specified index starting from the head without removing any
 * token
 * 
 * @author sharif
 * 
 */
public class TokenQueue implements Iterable<Token> {

  private Queue<Token> tokenQueue;
  private final LinkedList<Token> lookaheadBuffer = new LinkedList<Token>();
  private Token lastPoppedToken;

  public TokenQueue(List<Token> tokenList) {
    tokenQueue = new LinkedList<Token>(tokenList);
    lastPoppedToken = null;
  }

  public TokenQueue() {
    tokenQueue = new LinkedList<Token>();
    lastPoppedToken = null;
  }

  /**
   * Returns the next token without consume it.
   */
  public Token peek() {
    return lookAhead(1);
  }

  /**
   * Returns a token ahead of the current position, without consume it The first item to be looked ahead at has index 1.
   */
  public Token lookAhead(int index) {
    if (index < 1)
      throw new ChannelException("Index must be greater than 0");
    while (index > lookaheadBuffer.size()) {
      Token data = tokenQueue.poll();
      if (data == null) {
        return Token.EMPTY_TOKEN;
      }
      lookaheadBuffer.add(data);
    }
    return lookaheadBuffer.get(index - 1);
  }

  /**
   * consumes and returns the next token
   * 
   */
  public Token pop() {
    if (lookaheadBuffer.size() > 0) {
      return lastPoppedToken = lookaheadBuffer.poll();
    }
    if (tokenQueue.size() > 0) {
      return lastPoppedToken = tokenQueue.poll();
    }
    return Token.EMPTY_TOKEN;
  }

  /**
   * returns the last popped token
   * 
   * @return
   */
  public Token getLastPoppedToken() {
    return lastPoppedToken;
  }

  /**
   * restore consumed token
   * 
   * as a successful token matcher consumes token from token queue, this is required when a statement is build partially by some successful
   * token matchers but eventually failed to build in full because of failure of some other token matchers
   * 
   * 
   * @param matchedTokenList
   */
  public void restore(List<Token> matchedTokenList) {
    if (matchedTokenList != null && !matchedTokenList.isEmpty())
      lookaheadBuffer.addAll(0, matchedTokenList);
  }

  public void clear() {
    lookaheadBuffer.clear();
    tokenQueue.clear();
    lastPoppedToken = null;
  }

  public int size() {
    return lookaheadBuffer.size() + tokenQueue.size();
  }

  public void add(Token token) {
    tokenQueue.add(token);
  }

  public Iterator<Token> iterator() {
    return tokenQueue.iterator();
  }

}
