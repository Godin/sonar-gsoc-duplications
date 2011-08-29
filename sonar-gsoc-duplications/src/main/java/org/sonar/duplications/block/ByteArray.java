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
package org.sonar.duplications.block;

import java.util.Arrays;

public class ByteArray {

  private final byte[] bytes;

  /**
   * Cache for hash code.
   * FIXME Godin: this class not really immutable, because of method {@link #array()}, and cache for hash code seems very strange for mutable object.
   */
  private int hash;

  public ByteArray(String hexString) {
    int len = hexString.length();
    this.bytes = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
          + Character.digit(hexString.charAt(i + 1), 16));
    }
  }

  public ByteArray(byte[] bytes) {
    this.bytes = bytes;
  }

  public ByteArray(long value) {
    this.bytes = new byte[]{
        (byte) (value >>> 56),
        (byte) (value >>> 48),
        (byte) (value >>> 40),
        (byte) (value >>> 32),
        (byte) (value >>> 24),
        (byte) (value >>> 16),
        (byte) (value >>> 8),
        (byte) value};
  }

  public ByteArray(int value) {
    this.bytes = new byte[]{
        (byte) (value >>> 24),
        (byte) (value >>> 16),
        (byte) (value >>> 8),
        (byte) value};
  }

  public byte[] array() {
    return bytes;
  }

  private static final String HEXES = "0123456789abcdef";

  private String getHex(byte[] raw) {
    if (raw == null) {
      return null;
    }
    StringBuilder hex = new StringBuilder(2 * raw.length);
    for (byte b : raw) {
      hex.append(HEXES.charAt((b & 0xF0) >> 4))
          .append(HEXES.charAt((b & 0x0F)));
    }
    return hex.toString();
  }

  @Override
  public String toString() {
    return getHex(bytes);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ByteArray byteArray = (ByteArray) o;
    if (!Arrays.equals(bytes, byteArray.bytes)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int h = hash;
    int len = bytes.length;
    if (h == 0 && len > 0) {
      h = Arrays.hashCode(bytes);
      hash = h;
    }
    return h;
  }
}
