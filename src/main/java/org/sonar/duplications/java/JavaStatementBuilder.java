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

import org.sonar.duplications.api.StatementBuilder;
import org.sonar.duplications.api.channel.BlackHoleStatementBuilderChannel;
import org.sonar.duplications.api.channel.StatementBuilderChannel;

import static org.sonar.duplications.api.matcher.TokenMatcherFactory.*;

public class JavaStatementBuilder {

  private JavaStatementBuilder() {
  }

  public static final StatementBuilder build() {
	StatementBuilder.Builder builder = StatementBuilder.builder()
		.addStextexChannel(new BlackHoleStatementBuilderChannel(from("import"), to(";")))
		.addStextexChannel(new BlackHoleStatementBuilderChannel(from("package"), to(";")))
		.addStextexChannel(new BlackHoleStatementBuilderChannel(nextThisToken("}")))
		.addStextexChannel(new BlackHoleStatementBuilderChannel(nextThisToken("{")))
		.addStextexChannel(new StatementBuilderChannel(from("@"), nextAnyToken(1), bridge(MATCH_IS_OPTIONAL, "(", ")")))
		.addStextexChannel(new StatementBuilderChannel(from("do")))
		.addStextexChannel(new StatementBuilderChannel(from("if"), bridge("(", ")")))
		.addStextexChannel(new StatementBuilderChannel(from("else"), nextThisToken("if"), bridge("(", ")"))) //match else if
		.addStextexChannel(new StatementBuilderChannel(from("else"))) //match else only
		.addStextexChannel(new StatementBuilderChannel(from("for"), bridge("(", ")")))
		.addStextexChannel(new StatementBuilderChannel(from("while"), bridge("(", ")"), nextThisToken(MATCH_IS_OPTIONAL,";")))
		.addStextexChannel(new StatementBuilderChannel(from("case"), to(":")))
		.addStextexChannel(new StatementBuilderChannel(from("default"), to(":")))
		.addStextexChannel(new StatementBuilderChannel(to(";","!{","{","}")))  //!TOKEN means before token "TOKEN" i.e., !{ means before token "{"
		;
	
    return builder.build();
  }
 }
