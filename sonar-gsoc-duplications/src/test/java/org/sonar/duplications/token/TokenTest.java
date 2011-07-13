package org.sonar.duplications.token;

import org.junit.Test;

import static org.junit.Assert.assertThat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class TokenTest {

  @Test
  public void shouldBeEqual() {
    Token firstToken = new Token("MyValue", 1, 3);
    Token secondToken = new Token("MyValue", 1, 3);

    assertThat(firstToken, is(secondToken));
  }

  @Test
  public void shouldNotBeEqual() {
    Token firstToken = new Token("MyValue", 1, 3);
    Token secondToken = new Token("MySecondValue", 1, 3);
    Token thirdToken = new Token("MyValue", 3, 3);
    
    assertThat(firstToken, not(is(secondToken)));
    assertThat(firstToken, not(is(thirdToken)));
  }

}
