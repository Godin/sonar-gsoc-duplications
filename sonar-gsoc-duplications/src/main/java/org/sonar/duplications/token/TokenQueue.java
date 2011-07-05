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

  public TokenQueue(List<Token> tokenList) {
    tokenQueue = new LinkedList<Token>(tokenList);
  }

  public TokenQueue() {
    tokenQueue = new LinkedList<Token>();
  }

  public Token peek() {
    return tokenQueue.peek();
  }

  public Token poll() {
    return tokenQueue.poll();
  }

  public int size() {
    return tokenQueue.size();
  }

  public void add(Token token) {
    tokenQueue.add(token);
  }

  public boolean isNextTokenValue(String expectedValue) {
    Token nextToken = tokenQueue.peek();
    if (nextToken == null) {
      return false;
    }
    return nextToken.getValue().equals(expectedValue);
  }

  public Iterator<Token> iterator() {
    return tokenQueue.iterator();
  }

  public void pushBack(List<Token> matchedTokenList) {
    tokenQueue.addAll(matchedTokenList);
  }
}
