
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


/**
 * Configuration parameters used by a TokenReader to handle some specificities.
 * 
 * @author sharif
 *
 */
public class TokenReaderConfiguration {

  public final static int DEFAULT_BUFFER_CAPACITY = 8000;

  private int bufferCapacity = DEFAULT_BUFFER_CAPACITY;

  /**
   * @return the bufferCapacity
   */
  public int getBufferCapacity() {
    return bufferCapacity;
  }

  /**
   * @param bufferCapacity
   *          the bufferCapacity to set
   */
  public void setBufferCapacity(int bufferCapacity) {
    this.bufferCapacity = bufferCapacity;
  }

  public TokenReaderConfiguration cloneWithoutCodeReaderFilters() {
    TokenReaderConfiguration clone = new TokenReaderConfiguration();
    clone.setBufferCapacity(bufferCapacity);
    return clone;
  }

}
