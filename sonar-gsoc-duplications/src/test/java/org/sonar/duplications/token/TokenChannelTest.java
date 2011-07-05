package org.sonar.duplications.token;

import org.junit.Test;
import org.sonar.channel.CodeReader;

import static org.junit.Assert.assertThat;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

public class TokenChannelTest {

  @Test
  public void shouldConsume() {
    TokenChannel channel = new TokenChannel("ABC");
    TokenQueue output = new TokenQueue();
    assertThat(channel.consume(new CodeReader("ABCD"), output), is(true));

    assertThat(output, hasItem(new Token("ABC", 1, 0)));
  }

  @Test
  public void shouldNotConsume() {
    TokenChannel channel = new TokenChannel("ABC");
    TokenQueue output = new TokenQueue();
    assertThat(channel.consume(new CodeReader("123"), output), is(false));

    assertThat(output.size(), is(0));
  }

}
