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
package org.sonar.duplications.benchmark;

import java.util.Locale;

public final class Difference {

  private final double[] diff;

  private Difference(double[] diff) {
    this.diff = diff;
  }

  /**
   * Calculates difference from reference for all values in percentages.
   */
  public static Difference from(double reference, double... values) {
    double[] diff = new double[values.length];
    for (int i = 0; i < values.length; i++) {
      diff[i] = (reference - values[i]) / reference * 100;
    }
    return new Difference(diff);
  }

  private static final String NUMBER_FORMAT = "%4.0f";

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Difference (%): ").append(String.format(Locale.ENGLISH, NUMBER_FORMAT, diff[0]));
    for (int i = 1; i < diff.length; i++) {
      sb.append(", ").append(String.format(Locale.ENGLISH, NUMBER_FORMAT, diff[i]));
    }
    return sb.toString();
  }

}
