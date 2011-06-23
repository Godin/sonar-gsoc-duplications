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

import java.util.ArrayList;
import java.util.List;

import org.sonar.duplications.api.Statement;
import org.sonar.duplications.api.Token;

/**
 * channel that just consumes tokens but does not provide any output
 *  
 * @author sharif
 *
 */
public class BlackHoleStatementBuilderChannel extends StatementBuilderChannel {

	public BlackHoleStatementBuilderChannel(TokenMatcher ... tokenMatchers) {
		super(tokenMatchers);
	}

	@Override
	public boolean consume(TokenQueue tokenQueue, List<Statement> output) {
		if(tokenMatchers != null){
			List<Token> matchedTokenList = new ArrayList<Token>();
			for(TokenMatcher tokenMatcher : tokenMatchers){
				if(!tokenMatcher.matchToken(tokenQueue, matchedTokenList)){
					//match unsuccessful, restore the consumed tokens by previous successful matchers
					tokenQueue.restore(matchedTokenList);
					return false;
				}
			}

			//all matchers were successful, so just ignore the output
			return true;
		}
		return false;
	}
}
