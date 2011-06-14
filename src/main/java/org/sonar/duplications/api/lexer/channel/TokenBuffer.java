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
package org.sonar.duplications.api.lexer.channel;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.sonar.channel.ChannelException;
import org.sonar.duplications.api.codeunit.Token;

/**
 * The CodeBuffer class provides all the basic features required to manipulate a token stream. Those features are :
 * <ul>
 * <li>Read and consume next token : pop()</li>
 * <li>Retrieve last consumed token : lastToken()</li>
 * <li>Read without consuming next token : peek()</li>
 * <li>Read without consuming token at the specified index after the cursor</li>
 * <li>Position of the pending cursor : line and column</li>
 * </ul>
 *
 * @author sharif
 *
 */
public class TokenBuffer {

  private TokenStreamReader tokenReader;
	
  private Token lastToken = Token.EMPTY_TOKEN;
  private Cursor cursor;
  private int bufferCapacity;
  private Token[] buffer;
  private int bufferPosition = 0;
  private int bufferSize = 0;

  private boolean recordingMode = false;
  private StringBuilder recordedTokens = new StringBuilder();

  protected TokenBuffer(TokenStreamReader tokenReader, TokenReaderConfiguration configuration) {
    lastToken = Token.EMPTY_TOKEN;
    cursor = new Cursor();
    bufferCapacity = configuration.getBufferCapacity();
    buffer = new Token[bufferCapacity];
    this.tokenReader = tokenReader;
    fillBuffer();
  }

  public TokenBuffer(List<Token> tokenList, TokenReaderConfiguration configuration) {
	  this(new TokenStreamReader(tokenList), configuration);
  }
  
  /**
   * Read and consume the next token
   * 
   * @return the next token or -1 if the end of the stream is reached
   */
  public final Token pop() {
    if (bufferPosition == bufferSize) {
      fillBuffer();
    }
    if (bufferSize == 0) {
      return Token.EMPTY_TOKEN;
    }
    Token token = buffer[bufferPosition++];
    updateCursorPosition(token);
    if (recordingMode) {
    	recordedTokens.append(token.getNormalizedContent());
    }
    lastToken = token;
    return token;
  }

  private void updateCursorPosition(Token token) {
      cursor.line = token.getLine();
      cursor.column = token.getColumn();
  }

  private int fillBuffer() {
    try {
      int offset = bufferSize - bufferPosition;
      if (offset != 0) {
        System.arraycopy(buffer, bufferPosition, buffer, 0, bufferSize - bufferPosition);
      }
      bufferPosition = 0;
      int numberOfChars = tokenReader.readToken(buffer, offset, bufferCapacity - offset);
      if (numberOfChars == -1) {
        numberOfChars = 0;
      }
      bufferSize = numberOfChars + offset;
      return offset;
    } catch (IOException e) {
      throw new ChannelException(e.getMessage(), e);
    }
  }

  /**
   * Looks at the last consumed token
   * 
   * @return the last token or -1 if the no token has been yet consumed
   */
  public final Token lastToken() {
    return lastToken;
  }

  /**
   * Looks at the next token without consuming it
   * 
   * @return the next token or -1 if the end of the stream has been reached
   */
  public final Token peek() {
    return tokenAt(0);
  }

  /**
   * Pushes a token sequence onto the top of this CodeBuffer. This tokens will be then the first to be read.
   * 
   * @param chars
   *          the token sequences to push into the CodeBuffer
   */
  public void push(List<Token> tokenList) {
    int length = tokenList.size();
    if (bufferPosition >= length) {
      for (int index = 0; index < length; index++) {
        buffer[bufferPosition + index - length] = tokenList.get(index);
      }
      bufferPosition -= length;
    } else {
      Token[] extendedBuffer = new Token[buffer.length - bufferPosition + length];
      for (int index = 0; index < length; index++) {
        extendedBuffer[index] = tokenList.get(index);
      }
      System.arraycopy(buffer, bufferPosition, extendedBuffer, length, bufferSize - bufferPosition);
      buffer = extendedBuffer;
      bufferSize = bufferSize + length - bufferPosition;
      bufferPosition = 0;
    }
  }

  /**
   * Close the stream
   */
  public final void close() {
    IOUtils.closeQuietly(tokenReader);
  }

  /**
   * @return the current line of the cursor
   */
  public final int getLinePosition() {
    return cursor.line;
  }

  public final Cursor getCursor() {
    return cursor;
  }

  /**
   * @return the current column of the cursor
   */
  public final int getColumnPosition() {
    return cursor.column;
  }

  /**
   * Overrides the current column position
   */
  public final TokenBuffer setColumnPosition(int cp) {
    this.cursor.column = cp;
    return this;
  }

  /**
   * Overrides the current line position
   */
  public final void setLinePosition(int lp) {
    this.cursor.line = lp;
  }

  public final void startRecording() {
    recordingMode = true;
  }

  public final CharSequence stopRecording() {
    recordingMode = false;
    CharSequence result = recordedTokens;
    recordedTokens = new StringBuilder();
    return result;
  }

  /**
   * Returns the token at the specified index after the cursor without consuming it
   * 
   * @param index
   *          the index of the token to be returned
   * @return the desired token
   * @see java.lang.CharSequence#charAt(int)
   */
  public final Token tokenAt(int index) {
    if (bufferPosition + index > bufferSize - 1) {
      fillBuffer();
    }
    if (bufferPosition + index > bufferSize - 1) {
      return Token.EMPTY_TOKEN;
    }
    return buffer[bufferPosition + index];
  }

 
  public final int length() {
    return (bufferSize == bufferCapacity ? Integer.MAX_VALUE : bufferSize);
  }

  public final CharSequence subSequence(int start, int end) {
    throw new UnsupportedOperationException();
  }

  @Override
  public final String toString() {
    StringBuilder result = new StringBuilder();
    result.append("CodeReader(");
    result.append("line:").append(cursor.line);
    result.append("|column:").append(cursor.column);
    result.append("|cursor value:'").append(peek().getNormalizedContent()).append("'");
    result.append(")");
    return result.toString();
  }

  public final class Cursor implements Cloneable {

    private int line = 1;
    private int column = 0;

    public int getLine() {
      return line;
    }

    public int getColumn() {
      return column;
    }

    public Cursor clone() {
      Cursor clone = new Cursor();
      clone.column = column;
      clone.line = line;
      return clone;
    }
  }
}