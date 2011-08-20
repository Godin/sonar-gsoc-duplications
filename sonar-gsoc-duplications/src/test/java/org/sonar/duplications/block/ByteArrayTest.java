package org.sonar.duplications.block;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ByteArrayTest {

  @Test
  public void createIntTest() {
    int value = 0x12FF8413;
    ByteArray byteArray = new ByteArray(value);
    assertThat(byteArray.toString(), is(Integer.toHexString(value)));
  }

  @Test
  public void createLongTest() {
    long value = 0x12FF841344567899L;
    ByteArray byteArray = new ByteArray(value);
    assertThat(byteArray.toString(), is(Long.toHexString(value)));
  }

  @Test
  public void createHexStringTest() {
    String value = "12FF841344567899";
    ByteArray byteArray = new ByteArray(value);
    assertThat(byteArray.toString(), is(value.toLowerCase()));
  }
}
