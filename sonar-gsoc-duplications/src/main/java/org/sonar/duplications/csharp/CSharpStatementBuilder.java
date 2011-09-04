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
package org.sonar.duplications.csharp;

import org.sonar.duplications.statement.StatementChunker;

import static org.sonar.duplications.statement.TokenMatcherFactory.*;

public final class CSharpStatementBuilder {

  private CSharpStatementBuilder() {
  }

  public static StatementChunker build() {
    return StatementChunker.builder()
        .statement(from("using"), bridge("(", ")"))
        .statement(from("enum"), to(";"))
        .ignore(from("using"), to(";"))
        .ignore(from("namespace"), to("{"))
        .ignore(from("#"), anyToken(), bridge("(", ")"))
        .ignore(from("#"), token("else"))
        .ignore(from("#"), token("endif"))
        .ignore(from("#"), anyToken(), anyToken())
        .ignore(token("}"))
        .ignore(token("{"))
        .statement(bridge("[", "]"))
        .statement(from("do"))
        .statement(from("if"), bridge("(", ")"))
        .statement(from("else"), token("if"), bridge("(", ")"))
        .statement(from("else"))
        .statement(from("for"), bridge("(", ")"))
        .statement(from("foreach"), bridge("(", ")"))
        .statement(from("while"), bridge("(", ")"), opt(token(";")))
        .statement(from("case"), to(":"))
        .statement(from("get"), opt(bridge("{", "}")), opt(token(";")))
        .statement(from("set"), opt(bridge("{", "}")), opt(token(";")))
        .statement(from("default"), to(":"))
        .statement(to(";", "{", "}"), forgetLastToken())
        .build();
  }

}
