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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonar.channel.ChannelException;
import org.sonar.duplications.api.codeunit.Token;
import org.sonar.duplications.api.lexer.channel.StatementBuilderChannel.LineRange;

/**
 * The TokenReader class provides some advanced features to read a source code. The most important one is the ability to try consuming the
 * next token in the stream of tokens according to end of statement rule.
 *
 * @author sharif
 *
 */
public class TokenReader extends TokenBuffer {

  private Map<String, Integer> tokenPairMap = new HashMap<String, Integer>();
	
  public TokenReader(TokenStreamReader reader) {
    super(reader, new TokenReaderConfiguration());
  }

  public TokenReader(List<Token> tokenList) {
    super(tokenList, new TokenReaderConfiguration());
  }

  /**
   * Creates a code reader with specific configuration parameters.
   * 
   * @param tokenList
   *          the the list of tokens from which statements are generated
   * @param configuration
   *          the configuration parameters
   */
  public TokenReader(List<Token> tokenList, TokenReaderConfiguration configuration) {
    super(tokenList, configuration);
  }

  /**
   * Read and consume the next token
   * 
   * @param appendable
   *          the read character is appended to appendable
   */
  public final void pop(Appendable appendable) {
    try {
      appendable.append(pop().getNormalizedContent());
    } catch (IOException e) {
      throw new ChannelException(e.getMessage());
    }
  }

  /**
   * Read without consuming the next tokens
   * 
   * @param length
   *          number of tokens to read
   * @return array of tokens
   */
  public final Token[] peek(int length) {
	Token[] result = new Token[length];
    int index = 0;
    Token nextToken = tokenAt(index);
    while (nextToken != Token.EMPTY_TOKEN && index < length) {
      result[index] = nextToken;
      nextToken = tokenAt(++index);
    }
    return result;
  }


/**
   * Read without consuming the next token until a condition is reached (endToken)
   * 
   * @param startsWith
   *          start token of a statement
   * @param endsWith
   *          array of valid end tokens for a statement
   * @param appendable
   *          the read characters is appended to appendable
   */
  
  public final void peekTo(String startsWith, String[] endsWith, Appendable appendable) {
    int currentTokenIndex = 0;
    Token nextToken = tokenAt(currentTokenIndex);
    try {
      while (nextToken != Token.EMPTY_TOKEN) {
		if(isEndToken(nextToken, endsWith, currentTokenIndex)){
			//additional end match, for example: ')' followed by '{', ';' followed by '}' etc 
			appendable.append(nextToken.getNormalizedContent());
			if(currentTokenIndex > 0 && endsWith.length>1){
				if(isEndToken(tokenAt(currentTokenIndex + 1), endsWith, currentTokenIndex)){
					appendable.append(nextToken.getNormalizedContent());
					currentTokenIndex++;
				}
			}
			break;
		}
		nextToken = tokenAt(++currentTokenIndex);
      }
    } catch (IOException e) {
      throw new ChannelException(e.getMessage(), e);
    }
  }

  /**
   * Read and consume the next token according to a given rule. 
   * 
   * @param startsWith
   *          start token of a statement
   * @param endsWith
   *          array of valid end tokens for a statement
   * @param appendable
   *          the consumed characters are appended to this appendable
   * @return number of consumed characters or -1 if one of the two Matchers doesn't match
   */

  public int popTo(String startsWith, String[] endsWith, String symbolRepetatingPairs, boolean combineEndMarker, Appendable appendable,
		  LineRange codeRange) {
	  int currentTokenIndex = 0;
	  tokenPairMap.clear();
	  
	  Token nextToken = tokenAt(currentTokenIndex);
		
	  if (nextToken == Token.EMPTY_TOKEN) //no token available
			return -1;

		if (startsWith != null
				&& !startsWith.equals(nextToken.getNormalizedContent())) //beginning not match
			return -1;
		
		try{
			//some special processing required for Annotation/@ and do syntax 
			boolean doSpecialProcessing = doesEndMarkerRequireSpecialProcessing(
					endsWith, currentTokenIndex);
			
			if(doSpecialProcessing){
				for (String token : endsWith) {
					if (token.equals(StatementBuilderChannel.END_MARKER_AFTER_NEXT_TOKEN_EXCEPT_LEFT_BRACKET_NEXT)
							|| token.equals(StatementBuilderChannel.END_MARKER_AFTER_NEXT_TOKEN)) {
						currentTokenIndex ++;
					} else if (token.equals(StatementBuilderChannel.END_MARKER_BEFORE_NEXT_TOKEN)){
						//nothing to do as currentTokenIndex is the end marker
					}
				}
				
			} else{
				while(nextToken != Token.EMPTY_TOKEN){
					if(symbolRepetatingPairs != null && symbolRepetatingPairs.contains(nextToken.getNormalizedContent())){
						int count = tokenPairMap.get(nextToken.getNormalizedContent()) == null ?
								0: tokenPairMap.get(nextToken.getNormalizedContent());
						tokenPairMap.put(nextToken.getNormalizedContent(), ++count);
					}
					if(isEndToken(nextToken, endsWith, currentTokenIndex, symbolRepetatingPairs)){
						
						//additional end match, for example: ')' followed by '{', ';' followed by '}' etc 
						if(combineEndMarker && currentTokenIndex > 0 && endsWith.length>1){
							if( /*nextToken.equals(")") || nextToken.equals(")")*/ 
									isEndToken(tokenAt(currentTokenIndex + 1), endsWith, currentTokenIndex))
								currentTokenIndex++;
						}
						
						break;
					}
					nextToken = tokenAt(++currentTokenIndex);
				}
			}
	
			//pop the tokens out and build the statement
			for (int i = 0; i <= currentTokenIndex; i++) {
				Token token = pop();
				if(i==0) codeRange.setFromLine(token.getLine());
				if(i==currentTokenIndex) codeRange.setToLine(token.getLine());
		        appendable.append(token.getNormalizedContent());
		    }
		}catch (IndexOutOfBoundsException e) {
			return -1;
		} catch (IOException e) {
		      throw new ChannelException(e.getMessage(), e);
	    }
		return currentTokenIndex;
	}
  
  
	private boolean doesEndMarkerRequireSpecialProcessing(String[] endMarker, int tokenIndex) {
		for (String token : endMarker) {
			if (token.equals(StatementBuilderChannel.END_MARKER_AFTER_NEXT_TOKEN_EXCEPT_LEFT_BRACKET_NEXT)) {
				Token nextToken = tokenAt(tokenIndex + 2); //lookup one token further 
				if (!nextToken.getNormalizedContent().equals("(")) //if its (, do normal processing
					return true;
			} else if (token.equals(StatementBuilderChannel.END_MARKER_BEFORE_NEXT_TOKEN)
					|| token.equals(StatementBuilderChannel.END_MARKER_AFTER_NEXT_TOKEN)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * check whether a token is an end of a statement
	 * @param nextToken
	 * 				token that is being checked as a end token
	 * @param endsWith
	 * 				array of valid end tokens for a statement
	 * @return
	 */
    private boolean isEndToken(Token currentToken, String[] endsWith, int tokenIndex) {
		for (String token : endsWith) {
			if (token.equals(currentToken.getNormalizedContent()))
				return true;
			if(token.equals(StatementBuilderChannel.END_MARKER_BEFORE_LEFT_CURLY_BRACE)){
				Token nextToken = tokenAt(tokenIndex+1);
				if(nextToken.getNormalizedContent().equals("{"))
					return true;
			}
		}
		return false;
	}
  
	/**
	 * check whether a token is an end of a statement
	 * considering the repetition of specific symbol pair
	 * 
	 * @param nextToken
	 * 				token that is being checked as a end token
	 * @param endsWith
	 * 				array of valid end tokens for a statement
	 * @return
	 */
    private boolean isEndToken(Token currentToken, String[] endsWith, int tokenIndex, String symbolPairs) {
    	if(isEndToken(currentToken, endsWith, tokenIndex)){
	    	boolean pairCheckPass = true;
	    	if(!tokenPairMap.isEmpty() && symbolPairs!=null){
				for (int i=0; i<symbolPairs.length(); i=i+2) {
		    		String symbolL = symbolPairs.substring(i, i+1);
		    		String symbolR = symbolPairs.substring(i+1, i+2);
		    		if(!tokenPairMap.containsKey(symbolL) && !tokenPairMap.containsKey(symbolR)){
		    			//pair is not in map
		    			continue;
		    		} else if(tokenPairMap.containsKey(symbolL) && tokenPairMap.containsKey(symbolR)){
		    			//pair is in map
		    			if(tokenPairMap.get(symbolL).intValue() != tokenPairMap.get(symbolR).intValue()){
		    				pairCheckPass = false;
		    				break;
		    			}
		    		} else {
		    			//only a single of the pair is in map
		    			pairCheckPass = false;
	    				break;
		    		}
					
				}
			}
			return pairCheckPass;
    	}else
    		return false;
	}
    
}
