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
package org.sonar.duplications.api;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.sonar.duplications.api.channel.Channel2;
import org.sonar.duplications.api.channel.StatementBuilderChannelDisptacher;
import org.sonar.duplications.api.channel.TokenQueue;

/**
 * This class is similar to lexer but takes list of token as input and provide
 * list of statement as output
 * 
 * @author sharif
 * 
 */
public final class StatementBuilder {
	private final StatementBuilderChannelDisptacher<List<Statement>> channelDispatcher;

	public static Builder builder() {
		return new Builder();
	}

	private StatementBuilder(Builder builder) {
		this.channelDispatcher = builder.getChannelDispatcher();
	}

	public List<Statement> build(List<Token> tokens) {
		TokenQueue tokenQueue = new TokenQueue(tokens);
		List<Statement> statements = new ArrayList<Statement>();
		try {
			channelDispatcher.consume(tokenQueue, statements);
			return statements;
		} catch (Exception e) {
			throw new DuplicationsException("Unable to build statement from token : "+ tokenQueue.peek(), e);
		}
	}

	public static final class Builder {

		private List<Channel2> channels = new ArrayList<Channel2>();
		private Charset charset = Charset.defaultCharset();

		private Builder() {
		}

		public StatementBuilder build() {
			return new StatementBuilder(this);
		}

		public Builder addChannel(Channel2<List<Statement>> channel) {
			channels.add(channel);
			return this;
		}

		public Builder addStextexChannel(Channel2<List<Statement>> channel) {
			channels.add(channel);
			return this;
		}

		private StatementBuilderChannelDisptacher<List<Statement>> getChannelDispatcher() {
			return new StatementBuilderChannelDisptacher<List<Statement>>(
					channels);
		}

		public Builder setCharset(Charset charset) {
			this.charset = charset;
			return this;
		}

	}

}
