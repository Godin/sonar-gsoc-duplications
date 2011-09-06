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
package org.sonar.duplications.algorithm;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.sonar.duplications.algorithm.AdvancedGroupCloneReporter;
import org.sonar.duplications.algorithm.AdvancedPairCloneReporter;
import org.sonar.duplications.algorithm.CloneReporterAlgorithm;
import org.sonar.duplications.index.CloneIndex;

import java.util.Arrays;
import java.util.Collection;

@RunWith(value = Parameterized.class)
public abstract class BaseCloneReporterTest {

  protected CloneReporterAlgorithmBuilder cloneReporterBuilder;

  @Parameters
  public static Collection<Object[]> data() {
    CloneReporterAlgorithmBuilder builder1 = new CloneReporterAlgorithmBuilder() {
      public CloneReporterAlgorithm build(CloneIndex index) {
        return new AdvancedGroupCloneReporter(index);
      }
    };

    CloneReporterAlgorithmBuilder builder2 = new CloneReporterAlgorithmBuilder() {
      public CloneReporterAlgorithm build(CloneIndex index) {
        return new AdvancedPairCloneReporter(index);
      }
    };
    Object[][] data = new Object[][]{{builder1}, {builder2}};
    return Arrays.asList(data);
  }

  public BaseCloneReporterTest(CloneReporterAlgorithmBuilder builder) {
    this.cloneReporterBuilder = builder;
  }

}
