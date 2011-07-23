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

import org.sonar.duplications.statement.StatementChunker;

import static org.sonar.duplications.statement.TokenMatcherFactory.*;

public class JavaStatementBuilder {

  private JavaStatementBuilder() {
  }

  public static final StatementChunker build() {
    StatementChunker.Builder builder = StatementChunker
        .builder()
        .addBlackHoleChannel(from("import"), to(";"))
        .addBlackHoleChannel(from("package"), to(";"))
        .addBlackHoleChannel(token("}"))
        .addBlackHoleChannel(token("{"))
        .addChannel(from("@"), anyToken(), opt(bridge("(", ")")))
        .addChannel(from("do"))
        .addChannel(from("if"), bridge("(", ")"))
        .addChannel(from("else"), token("if"), bridge("(", ")"))
        .addChannel(from("else"))
        .addChannel(from("for"), bridge("(", ")"))
        .addChannel(from("while"), bridge("(", ")"), opt(token(";")))
        .addChannel(from("case"), to(":"))
        .addChannel(from("default"), to(":"))
        .addChannel(to(";", "{", "}"), forgiveLastToken());

    return builder.build();
  }
}
