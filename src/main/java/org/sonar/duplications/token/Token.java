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

public class Token {

  private final int line;
  private final int column;
  private final String value;

  public Token(String value, int line, int column) {
    this.value = value;
    this.column = column;
    this.line = line;
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof Token) {
      Token anotherToken = (Token) object;
      return anotherToken.line == line && anotherToken.column == column && anotherToken.value.equals(value);
    }
    return false;
  }

  public int getLine() {
    return line;
  }

  public int getColumn() {
    return column;
  }

  public String getValue() {
    return value;
  }

  @Override
  public int hashCode() {
    return value.hashCode() + line + column;
  }

  @Override
  public String toString() {
    return "'" + value + "'[" + line + "," + column + "]";
  }
}
