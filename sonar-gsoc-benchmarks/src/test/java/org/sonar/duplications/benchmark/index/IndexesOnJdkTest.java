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
package org.sonar.duplications.benchmark.index;

import static org.hamcrest.Matchers.greaterThan;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.sonar.duplications.benchmark.Utils;

@Ignore("Not relevant for the moment")
public class IndexesOnJdkTest extends AbstractIndexesTestCase {

  @BeforeClass
  public static void before() {
    BENCHMARK_ROUNDS = 2;
    WARMUP_ROUNDS = 1;

    files = Utils.filesFromJdk16();
    Assume.assumeThat(files.size(), greaterThan(0));
  }

}
