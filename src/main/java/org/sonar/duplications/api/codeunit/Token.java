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
package org.sonar.duplications.api.codeunit;

public class Token {

	private final int line;
	private final int column;
	private final String normalizedContent;
	private final String originalContent;

	public Token(String originalContent, int line, int column) {
		this.normalizedContent = originalContent;
		this.originalContent = originalContent;
		this.column = column;
		this.line = line;
	}

	public Token(String normalizedContent, String originalContent, int line,
			int column) {
		this.normalizedContent = normalizedContent;
		this.originalContent = originalContent;
		this.column = column;
		this.line = line;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Token) {
			Token anotherToken = (Token) object;
			return anotherToken.normalizedContent.equals(normalizedContent)
					&& anotherToken.line == line
					&& anotherToken.column == column;
		}
		return false;
	}

	public int getLine() {
		return line;
	}

	public int getColumn() {
		return column;
	}

	public String getNormalizedContent() {
		return normalizedContent;
	}

	public String getOriginalContent() {
		return originalContent;
	}

	@Override
	public int hashCode() {
		return normalizedContent.hashCode() + line + column;
	}

	@Override
	public String toString() {
		return "'" + normalizedContent + "'[" + line + "," + column + "]";
	}
}
