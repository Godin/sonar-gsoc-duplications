package org.sonar.duplications.java;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.sonar.channel.ChannelException;
import org.sonar.duplications.statement.TokenMatcherFactory;
import org.sonar.duplications.token.Token;
import org.sonar.duplications.token.TokenQueue;

public class TokenMatcherTest {
	
	@Test
	public void shouldMatchFromToken(){
		List<Token> output = new ArrayList<Token>();
		List<Token> input = new ArrayList<Token>();
		Token t1 = new Token("@", 1, 2);
		Token t2 = new Token("Entity", 1, 2);
		input.add(t1);
		input.add(t2);
		TokenQueue tokenQueue = new TokenQueue(input);	
		
		boolean match = TokenMatcherFactory.from("@").matchToken(tokenQueue, output);
		Assert.assertTrue(match);
		assertThat(output.size(), is(1));
		assertThat(output, hasItems(t1));
	}
	
	@Test
	public void shouldNotMatchFromToken(){
		List<Token> output = new ArrayList<Token>();
		List<Token> input = new ArrayList<Token>();
		Token t1 = new Token("@", 1, 2);
		Token t2 = new Token("Entity", 1, 2);
		input.add(t1);
		input.add(t2);
		TokenQueue tokenQueue = new TokenQueue(input);	
		
		boolean match = TokenMatcherFactory.from("Entity").matchToken(tokenQueue, output);
		Assert.assertFalse(match); //starts with different token
		assertThat(output.size(), is(0));
	}
	
	@Test
	public void shouldMatchEndToken(){
		List<Token> output = new ArrayList<Token>();
		List<Token> input = new ArrayList<Token>();
		Token t1 = new Token("int", 1, 2);
		Token t2 = new Token("count", 1, 2);
		Token t3 = new Token(";", 1, 2);
		Token t4 = new Token("public", 1, 2);
		Token t5 = new Token("void", 1, 2);
		input.add(t1);
		input.add(t2);
		input.add(t3);
		input.add(t4);
		input.add(t5);
		
		TokenQueue tokenQueue = new TokenQueue(input);	
		
		boolean match = TokenMatcherFactory.to(";").matchToken(tokenQueue, output);
		Assert.assertTrue(match);
		assertThat(output.size(), is(3));
		assertThat(output, hasItems(t1,t2,t3));
	}
	
	@Test
	public void shouldMatchBeforeEndToken(){
		List<Token> output = new ArrayList<Token>();
		List<Token> input = new ArrayList<Token>();
		Token t1 = new Token("int", 1, 2);
		Token t2 = new Token("count", 1, 2);
		Token t3 = new Token(";", 1, 2);
		Token t4 = new Token("public", 1, 2);
		Token t5 = new Token("void", 1, 2);
		input.add(t1);
		input.add(t2);
		input.add(t3);
		input.add(t4);
		input.add(t5);
		
		TokenQueue tokenQueue = new TokenQueue(input);	
		
		boolean match = TokenMatcherFactory.to("!;").matchToken(tokenQueue, output);
		Assert.assertTrue(match); //match before ";"
		assertThat(output.size(), is(2));
		assertThat(output, hasItems(t1,t2));
	}
	
	@Test
	public void shouldNotMatchEndToken(){
		List<Token> output = new ArrayList<Token>();
		List<Token> input = new ArrayList<Token>();
		Token t1 = new Token("int", 1, 2);
		Token t2 = new Token("count", 1, 2);
		Token t3 = new Token("=", 1, 2);
		Token t4 = new Token("0", 1, 2);
		Token t5 = new Token("}", 1, 2);
		input.add(t1);
		input.add(t2);
		input.add(t3);
		input.add(t4);
		input.add(t5);
		
		TokenQueue tokenQueue = new TokenQueue(input);	
		
		boolean match = TokenMatcherFactory.to(";").matchToken(tokenQueue, output);
		Assert.assertFalse(match); //there was not token as ";"
		assertThat(output.size(), is(0));
	}
	
	@Test
	public void shouldMatchBridge(){
		List<Token> output = new ArrayList<Token>();
		List<Token> input = new ArrayList<Token>();
		Token t1 = new Token("(", 1, 2);
		Token t2 = new Token("getCount", 1, 2);
		Token t3 = new Token("(", 1, 2);
		Token t4 = new Token(")", 1, 2);
		Token t5 = new Token("+", 1, 2);
		Token t6 = new Token("NUMBER", 1, 2);
		Token t7 = new Token("*", 1, 2);
		Token t8 = new Token("NUMBER", 1, 2);
		Token t9 = new Token(")", 1, 2);
		
		input.add(t1);
		input.add(t2);
		input.add(t3);
		input.add(t4);
		input.add(t5);
		input.add(t6);
		input.add(t7);
		input.add(t8);
		input.add(t9);
		
		TokenQueue tokenQueue = new TokenQueue(input);	
		
		boolean match = TokenMatcherFactory.bridge("(",")").matchToken(tokenQueue, output);
		Assert.assertTrue(match);
		assertThat(output.size(), is(9));
		
		input.remove(t9);
		tokenQueue = new TokenQueue(input);	
		output.clear();
		
		match = TokenMatcherFactory.bridge(TokenMatcherFactory.MATCH_IS_OPTIONAL, "(",")").matchToken(tokenQueue, output);
		Assert.assertTrue(match); //since the match was optional, returns true with o token in output
		assertThat(output.size(), is(0));
	}
	
	@Test
	public void shouldNotMatchBridge(){
		List<Token> output = new ArrayList<Token>();
		List<Token> input = new ArrayList<Token>();
		Token t1 = new Token("(", 1, 2);
		Token t2 = new Token("getCount", 1, 2);
		Token t3 = new Token("(", 1, 2);
		Token t4 = new Token(")", 1, 2);
		Token t5 = new Token("+", 1, 2);
		Token t6 = new Token("NUMBER", 1, 2);
		Token t7 = new Token("*", 1, 2);
		Token t8 = new Token("NUMBER", 1, 2);
		//Token t9 = new Token(")", 1, 2);
		
		input.add(t1);
		input.add(t2);
		input.add(t3);
		input.add(t4);
		input.add(t5);
		input.add(t6);
		input.add(t7);
		input.add(t8);
		//input.add(t9);
		
		TokenQueue tokenQueue = new TokenQueue(input);	
		
		boolean match = TokenMatcherFactory.bridge("(",")").matchToken(tokenQueue, output);
		Assert.assertFalse(match); //last closing token ")" missing
		assertThat(output.size(), is(0));
	}
	
	
	@Test
	public void shouldMatchAnyToken(){
		List<Token> output = new ArrayList<Token>();
		List<Token> input = new ArrayList<Token>();
		Token t1 = new Token("int", 1, 2);
		Token t2 = new Token("count", 1, 2);
		Token t3 = new Token("=", 1, 2);
		Token t4 = new Token("0", 1, 2);
		Token t5 = new Token(";", 1, 2);
		
		input.add(t1);
		input.add(t2);
		input.add(t3);
		input.add(t4);
		input.add(t5);
		
		TokenQueue tokenQueue = new TokenQueue(input);	
		
		boolean match = TokenMatcherFactory.nextAnyToken(1).matchToken(tokenQueue, output);
		Assert.assertTrue(match);
		assertThat(output.size(), is(1));
		assertThat(output,  hasItems(t1));
		
		tokenQueue = new TokenQueue(input);
		output.clear();
		
		match = TokenMatcherFactory.nextAnyToken(3).matchToken(tokenQueue, output);
		Assert.assertTrue(match);
		assertThat(output.size(), is(3));
		assertThat(output, hasItems(t1,t2,t3));
	}
	
	@Test
	public void shouldNotMatchAnyToken(){
		List<Token> output = new ArrayList<Token>();
		List<Token> input = new ArrayList<Token>();
		Token t1 = new Token("int", 1, 2);
		Token t2 = new Token("count", 1, 2);
		Token t3 = new Token("=", 1, 2);
		Token t4 = new Token("0", 1, 2);
		Token t5 = new Token(";", 1, 2);
		
		input.add(t1);
		input.add(t2);
		input.add(t3);
		input.add(t4);
		input.add(t5);
		
		TokenQueue tokenQueue = new TokenQueue(input);	
		
		boolean match = TokenMatcherFactory.nextAnyToken(6).matchToken(tokenQueue, output);
		Assert.assertFalse(match); //index is greater than queue size
		assertThat(output.size(), is(0));
	}
	
	@Test (expected = ChannelException.class)
	public void shouldNotMatchAnyTokenAndThrowException(){
		List<Token> output = new ArrayList<Token>();
		List<Token> input = new ArrayList<Token>();
		Token t1 = new Token("int", 1, 2);
		Token t2 = new Token("count", 1, 2);
		Token t3 = new Token("=", 1, 2);
		Token t4 = new Token("0", 1, 2);
		Token t5 = new Token(";", 1, 2);
		
		input.add(t1);
		input.add(t2);
		input.add(t3);
		input.add(t4);
		input.add(t5);
		
		TokenQueue tokenQueue = new TokenQueue(input);	
		
		boolean match = TokenMatcherFactory.nextAnyToken(-1).matchToken(tokenQueue, output);
		//index must be greater than zero
	}
	
	@Test
	public void shouldMatchThisToken(){
		List<Token> output = new ArrayList<Token>();
		List<Token> input = new ArrayList<Token>();
		Token t1 = new Token("int", 1, 2);
		Token t2 = new Token("count", 1, 2);
		Token t3 = new Token("=", 1, 2);
		Token t4 = new Token("0", 1, 2);
		Token t5 = new Token(";", 1, 2);
		
		input.add(t1);
		input.add(t2);
		input.add(t3);
		input.add(t4);
		input.add(t5);
		
		TokenQueue tokenQueue = new TokenQueue(input);	
		
		boolean match = TokenMatcherFactory.nextThisToken("int").matchToken(tokenQueue, output);
		Assert.assertTrue(match);
		assertThat(output.size(), is(1));
		assertThat(output,  hasItems(t1));
		
		tokenQueue = new TokenQueue(input);
		output.clear();
		
		match = TokenMatcherFactory.nextThisToken(TokenMatcherFactory.MATCH_IS_OPTIONAL,";").matchToken(tokenQueue, output);
		Assert.assertTrue(match); //since match was optional
		assertThat(output.size(), is(0));
	}
	
	@Test
	public void shouldNotMatchThisToken(){
		List<Token> output = new ArrayList<Token>();
		List<Token> input = new ArrayList<Token>();
		Token t1 = new Token("int", 1, 2);
		Token t2 = new Token("count", 1, 2);
		Token t3 = new Token("=", 1, 2);
		Token t4 = new Token("0", 1, 2);
		Token t5 = new Token(";", 1, 2);
		
		input.add(t1);
		input.add(t2);
		input.add(t3);
		input.add(t4);
		input.add(t5);
		
		TokenQueue tokenQueue = new TokenQueue(input);	
		
		boolean match = TokenMatcherFactory.nextThisToken(";").matchToken(tokenQueue, output);
		Assert.assertFalse(match); //since match was not optional
		assertThat(output.size(), is(0));
	}
	
}
