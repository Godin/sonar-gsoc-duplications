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
package org.sonar.duplications.statement;

import org.sonar.duplications.statement.BlackHoleStatementBuilderChannel;
import org.sonar.duplications.statement.StatementChunker;
import org.sonar.duplications.statement.StatementChannel;

import static org.sonar.duplications.statement.TokenMatcherFactory.*;

public class JavaStatementBuilder {

  private JavaStatementBuilder() {
  }

  public static final StatementChunker build() {
	StatementChunker.Builder builder = StatementChunker.builder()
		.addStextexChannel(new BlackHoleStatementBuilderChannel(from("import"), to(";")))
		.addStextexChannel(new BlackHoleStatementBuilderChannel(from("package"), to(";")))
		.addStextexChannel(new BlackHoleStatementBuilderChannel(nextThisToken("}")))
		.addStextexChannel(new BlackHoleStatementBuilderChannel(nextThisToken("{")))
		.addStextexChannel(new StatementChannel(from("@"), nextAnyToken(1), bridge(MATCH_IS_OPTIONAL, "(", ")")))
		.addStextexChannel(new StatementChannel(from("do")))
		.addStextexChannel(new StatementChannel(from("if"), bridge("(", ")")))
		.addStextexChannel(new StatementChannel(from("else"), nextThisToken("if"), bridge("(", ")"))) //match else if
		.addStextexChannel(new StatementChannel(from("else"))) //match else only
		.addStextexChannel(new StatementChannel(from("for"), bridge("(", ")")))
		.addStextexChannel(new StatementChannel(from("while"), bridge("(", ")"), nextThisToken(MATCH_IS_OPTIONAL,";")))
		.addStextexChannel(new StatementChannel(from("case"), to(":")))
		.addStextexChannel(new StatementChannel(from("default"), to(":")))
		.addStextexChannel(new StatementChannel(to(";","!{","{","}")))  //!TOKEN means before token "TOKEN" i.e., !{ means before token "{"
		;
	
    return builder.build();
  }
 }
