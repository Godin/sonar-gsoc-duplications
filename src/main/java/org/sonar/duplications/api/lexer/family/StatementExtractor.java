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

import java.io.File;
import java.util.List;

import org.sonar.duplications.api.codeunit.Token;
import org.sonar.duplications.api.lexer.Lexer;
import org.sonar.duplications.api.lexer.channel.BlackHoleLexerChannel;
import org.sonar.duplications.api.lexer.channel.LexerChannel;


public class StatementExtractor {

  private Lexer lexer;
  
  private StatementExtractor() {
	  init();
  }

  public List<Token> extractStatement(File sourceFile){
	  return lexer.lex(sourceFile);
  }
  
  public void init() {
    Lexer.Builder builder = Lexer.builder()
    	.addChannel(new BlackHoleLexerChannel("import.*?;")) //import
        .addChannel(new BlackHoleLexerChannel("package.*?;")) //package
        .addChannel(new BlackHoleLexerChannel("^\\s\\s*")) //leading whitespace
//        .addChannel(new BlackHoleLexerChannel("\\s*\\}")) //single }
//        .addChannel(new BlackHoleLexerChannel("\\s*\\{")) //single {
        .addChannel(new LexerChannel("\\@\\w+[\\s&&^\\n]*(\\s*\\(.*?\\))?")) //annotation
        .addChannel(new LexerChannel("if\\s*\\(.*?(\\(.*\\).*?)*\\)(\\s*\\{)?")) //if w/wo {
        .addChannel(new LexerChannel("for\\s*\\(.*?;\\s*.*?;\\s*.*?\\)(\\s*\\{)?")) //for w/wo {
        .addChannel(new LexerChannel("for\\s*\\(.*?:.*?\\)(\\s*\\{)?")) //compact for w/wo {
        .addChannel(new LexerChannel("while\\s*\\(.*?(\\(.*\\).*?)*\\)(\\s*[\\{\\;])?")) //while w/wo {
        .addChannel(new LexerChannel("do[\\s&&^\\n]*(\\s*\\{)?")) //do w/wo {
        .addChannel(new LexerChannel("(.*?\\s*)*?[;{}]"));
    lexer = builder.build();
  }

	public static StatementExtractor getInstance() {
		return new StatementExtractor();
	}
 }

