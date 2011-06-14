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

import org.sonar.duplications.api.DuplicationsException;
import org.sonar.duplications.api.codeunit.Statement;

/**
 * channel that consumes tokens if a statement can be build using those tokens as per given rule
 * the statement is added to the output
 *
 * @author sharif
 *
 */
public class StatementBuilderChannel extends Channel2<List<Statement>> {

	/**
	 * These are some special end markers to handle special type of statement
	 * 1. Annotation parameterized/non-parameterized
	 * 2. do in do-while with/without { }
	 * 
	 */
	public static final String END_MARKER_BEFORE_LEFT_CURLY_BRACE = "end_marker_before_{"; 
	public static final String END_MARKER_BEFORE_NEXT_TOKEN = "end_marker_before_next_token"; 
	public static final String END_MARKER_AFTER_NEXT_TOKEN = "end_marker_after_next_token";
	public static final String END_MARKER_AFTER_NEXT_TOKEN_EXCEPT_LEFT_BRACKET_NEXT = "end_marker_after_next_token_except_(_NEXT";
	
	protected final StringBuilder tmpBuilder = new StringBuilder();
	
	private static int indexInFile = 0;
	
	/**
	 * stores the range of lines a statement spans over in physical file
	 */
	protected LineRange lineRange = new LineRange();
	
	/**
	 * if true
	 * multiple valid end marker tokens will be combined at the end of statement
	 * for example "while( condition );" in do-while block where ')' and ';' are both used as end marker
	 */
	protected boolean combineEndMarker = false;
	
	/**
	 * start token of an statement
	 */
	protected String startsWith;	
	
	/**
	 * tokens eligible for an end of a statement
	 */
	protected String[] endsWith;	
	
	/**
	 * some of the end of statement marker token might be appear before actual end marker position
	 * as a token pair for example, consider this code fragment:
	 * 
	 * if( getCount() > 0 ) return true; else return false;
	 * 
	 * we are considering 3 statements out of it as follows:
	 * 
	 * if( getCount() > 0 )
	 * return true;
	 * else return false;
	 * 
	 * the end marker of the 1st statement is ) but it appears once inside the statement before 
	 * end of the statement is reached. 
	 * 
	 * so this field will contain which of the tokens in the statement need to check for
	 * pairwise appearance before deciding for an end of statement   
	 * 
	 */
	protected String symbolRepetatingPairs; 
	
	public StatementBuilderChannel(String[] endsWith) {
		this(null, endsWith, null);
	}

	public StatementBuilderChannel(String startsWith, String[] endsWith) {
		this(startsWith, endsWith, null);
	}

	public StatementBuilderChannel(String[] endsWith,
			String symbolRepetatingPairs) {
		this(null, endsWith, symbolRepetatingPairs);
	}
	public StatementBuilderChannel(String startsWith, String[] endsWith,
			String symbolRepetatingPairs) {
		this(startsWith, endsWith, symbolRepetatingPairs, false);
	}
	
	public StatementBuilderChannel(String startsWith, String[] endsWith,
			String symbolRepetatingPairs, boolean combineEndMarker) {
		this.startsWith = startsWith;
		this.endsWith = endsWith;
		if(symbolRepetatingPairs != null && symbolRepetatingPairs.length()%2 != 0) 
			throw new DuplicationsException("Incvaid input \""+ symbolRepetatingPairs +"\": need to provide symbols in pair");
		this.symbolRepetatingPairs = symbolRepetatingPairs;
		this.combineEndMarker = combineEndMarker;
	}

	@Override
	public boolean consume(TokenReader tokenReader, List<Statement> output) {
		if (tokenReader.popTo(startsWith, endsWith, symbolRepetatingPairs, combineEndMarker, tmpBuilder, lineRange) > -1) {
			String statementValue = tmpBuilder.toString();
			output.add(new Statement(lineRange.getFromLine(), lineRange.getToLine(), statementValue, indexInFile++));
			tmpBuilder.delete(0, tmpBuilder.length());
			lineRange.reset();
			return true;
		}
		return false;
	}
	
	public static final class LineRange {
		private Integer toLine;
		private Integer fromLine;

		public LineRange() {
			reset();
		}

		public void reset() {
			this.toLine = -1;
			this.fromLine = -1;
		}

		public Integer getToLine() {
			return toLine;
		}

		public Integer getFromLine() {
			return fromLine;
		}

		public void setToLine(Integer toLine) {
			this.toLine = toLine;
		}

		public void setFromLine(Integer fromLine) {
			this.fromLine = fromLine;
		}
	}
}
