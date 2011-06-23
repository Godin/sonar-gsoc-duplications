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
package org.sonar.duplications.api.channel;

import java.util.List;

import org.sonar.duplications.api.Statement;


/**
 * @author sharif
 *
 * @param <OUTPUT>
 */
public abstract class Channel2<OUTPUT> {

  /**
   * Tries to consume the token stream at the current reading cursor position 
   * (provided by the {@link org.sonar.duplications.api.lexer.channel.TokenReader}). If
   * the token stream is consumed the method must return true and the OUTPUT object can be fed.
   * 
   * @param tokenReader
   *          the handle on the input token stream
   * @param output
   *          the OUTPUT that can be optionally fed by the Channel
   * @return false if the Channel doesn't want to consume the character stream, true otherwise.
   */
  public abstract boolean consume(TokenQueue tokenQueue, OUTPUT output);

}
