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
package org.sonar.duplications.api.lexer.family;

import org.sonar.duplications.api.lexer.StatementBuilder;
import org.sonar.duplications.api.lexer.channel.BlackHoleStatementBuilderChannel;
import org.sonar.duplications.api.lexer.channel.StatementBuilderChannel;

public class JavaStatementBuilder {

  private JavaStatementBuilder() {
  }

  public static final StatementBuilder build() {
	StatementBuilder.Builder builder = StatementBuilder.builder()
		.addStextexChannel(new BlackHoleStatementBuilderChannel("import", new String[]{";"}))
		.addStextexChannel(new BlackHoleStatementBuilderChannel("package", new String[]{";"}))
		.addStextexChannel(new StatementBuilderChannel("@", new String[]{StatementBuilderChannel.END_MARKER_AFTER_NEXT_TOKEN_EXCEPT_LEFT_BRACKET_NEXT,")"}, "(){}"))
		.addStextexChannel(new StatementBuilderChannel("do", new String[]{StatementBuilderChannel.END_MARKER_BEFORE_NEXT_TOKEN,"{"}))
		.addStextexChannel(new StatementBuilderChannel("if", new String[]{")"}, "(){}"))
		.addStextexChannel(new StatementBuilderChannel("else", new String[]{")"}, "(){}"))
		.addStextexChannel(new StatementBuilderChannel("for", new String[]{")"}, "(){}"))
		.addStextexChannel(new StatementBuilderChannel("while", new String[]{")",";"}, "(){}", true))
		.addStextexChannel(new StatementBuilderChannel("case", new String[]{":"}))
		.addStextexChannel(new StatementBuilderChannel("default", new String[]{":"}))
		.addStextexChannel(new StatementBuilderChannel(new String[]{";","}",StatementBuilderChannel.END_MARKER_BEFORE_LEFT_CURLY_BRACE,"{"}))
		;
	
    return builder.build();
  }
 }
