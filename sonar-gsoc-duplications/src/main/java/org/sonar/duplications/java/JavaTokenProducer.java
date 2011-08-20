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
package org.sonar.duplications.java;

import org.sonar.duplications.token.TokenChunker;

public final class JavaTokenProducer {

  private JavaTokenProducer() {
  }

  public static TokenChunker build() {
    TokenChunker.Builder builder = TokenChunker.builder()
        .addBlackHoleChannel("\\s")
        .addBlackHoleChannel("//[^\\n\\r]*+")
        .addBlackHoleChannel("/\\*[\\s\\S]*?\\*/")
        .addChannel("\".*?\"", "LITERAL")
        .addChannel("\'.*?\'", "LITERAL")
        .addChannel("[a-zA-Z_]++[0-9]*+[a-zA-Z_]*+")
        .addChannel("[0-9]*\\.[0-9]+([eE][-+]?[0-9]+)?", "DECIMAL")
        .addChannel("[0-9]++", "INTEGER")
        .addChannel(".");
    return builder.build();
  }
}
