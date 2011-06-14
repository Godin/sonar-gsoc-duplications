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
package org.sonar.duplications.api.lexer.channel;

import java.util.List;
import org.sonar.duplications.api.codeunit.Statement;

/**
 * channel that just consumes tokens but does not provide any output
 *  
 * @author sharif
 *
 */
public class BlackHoleStatementBuilderChannel extends StatementBuilderChannel {

	public BlackHoleStatementBuilderChannel(String startsWith, String[] endsWith) {
		super(startsWith, endsWith);
	}

	public BlackHoleStatementBuilderChannel(String startsWith, String[] endsWith, 
			String symbolRepetatingPairs) {
		super(startsWith, endsWith, symbolRepetatingPairs);
	}
	
	@Override
	public boolean consume(TokenReader tokenReader, List<Statement> output) {
		if (tokenReader.popTo(startsWith, endsWith, symbolRepetatingPairs, combineEndMarker, tmpBuilder, lineRange) > -1) {
			tmpBuilder.delete(0, tmpBuilder.length());
			return true;
		}
		return false;
	}
}
