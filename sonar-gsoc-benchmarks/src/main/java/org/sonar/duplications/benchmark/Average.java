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


public final class Average {

  /**
   * Average.
   */
  public final double avg;

  /**
   * Standard deviation.
   */
  public final double stddev;

  private Average(double avg, double stddev) {
    this.avg = avg;
    this.stddev = stddev;
  }

  public static Average from(long[] values) {
    long sum = 0;
    long sumSquares = 0;
    for (long l : values) {
      sum += l;
      sumSquares += l * l;
    }
    double avg = sum / (double) values.length;
    return new Average(avg, Math.sqrt(sumSquares / (double) values.length - avg * avg));
  }

}
