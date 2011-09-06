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

/**
 * See <a href="http://java.sun.com/docs/books/jls/third_edition/html/lexical.html">The Java Language Specification, Third Edition: Lexical Structure</a>
 * 
 * <p>
 * We decided to use dollar sign as a prefix for normalization, even if it can be a part of an identifier,
 * because according to Java Language Specification it supposed to be used only in mechanically generated source code.
 * Thus probability to find it within a normal code should be low.
 * </p>
 */
public final class JavaTokenProducer {

  private JavaTokenProducer() {
  }

  private static final String NORMALIZED_CHARACTER_LITERAL = "$CHARS";
  private static final String NORMALIZED_NUMERIC_LITERAL = "$NUMBER";

  private static final String EXP = "([Ee][+-]?+[0-9]++)";
  private static final String BINARY_EXP = "([Pp][+-]?+[0-9]++)";

  private static final String FLOAT_SUFFIX = "[fFdD]";
  private static final String INT_SUFFIX = "[lL]";

  public static TokenChunker build() {
    return TokenChunker.builder()
        // White Space
        .ignore("\\s")
        // Comments
        .ignore("//[^\\n\\r]*+")
        .ignore("/\\*[\\s\\S]*?\\*/")
        // String Literals
        .token("\"([^\"\\\\]*+(\\\\[\\s\\S])?+)*+\"", NORMALIZED_CHARACTER_LITERAL)
        // Character Literals
        .token("'([^'\\n\\\\]*+(\\\\.)?+)*+'", NORMALIZED_CHARACTER_LITERAL)
        // Identifiers, Keywords, Boolean Literals, The Null Literal
        .token("\\p{javaJavaIdentifierStart}++\\p{javaJavaIdentifierPart}*+")
        // Floating-Point Literals
        .token("[0-9]++\\.([0-9]++)?+" + EXP + "?+" + FLOAT_SUFFIX + "?+", NORMALIZED_NUMERIC_LITERAL)
        .token("\\.[0-9]++" + EXP + "?+" + FLOAT_SUFFIX + "?+", NORMALIZED_NUMERIC_LITERAL)
        .token("[0-9]++" + EXP + FLOAT_SUFFIX + "?+", NORMALIZED_NUMERIC_LITERAL)
        .token("0[xX][0-9a-fA-F]++\\.[0-9a-fA-F]*+" + BINARY_EXP + "?+" + FLOAT_SUFFIX + "?+", NORMALIZED_NUMERIC_LITERAL)
        .token("0[xX][0-9a-fA-F]++" + BINARY_EXP + FLOAT_SUFFIX + "?+", NORMALIZED_NUMERIC_LITERAL)
        // Integer Literals
        .token("0[xX][0-9a-fA-F]++" + INT_SUFFIX + "?+", NORMALIZED_NUMERIC_LITERAL)
        .token("[0-9]++" + INT_SUFFIX + "?+", NORMALIZED_NUMERIC_LITERAL)
        // Any other character
        .token(".")
        .build();
  }

}
