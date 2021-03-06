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

import java.util.List;

import org.sonar.duplications.algorithm.filter.IntervalTreeClonePairFilter;
import org.sonar.duplications.index.CloneGroup;
import org.sonar.duplications.index.CloneIndex;

public class AdvancedPairCloneReporter extends AbstractAdvancedCloneReporter {

  private static final IntervalTreeClonePairFilter INTERVAL_PAIR_FILTER = new IntervalTreeClonePairFilter();

  public AdvancedPairCloneReporter(CloneIndex cloneIndex) {
    this.cloneIndex = cloneIndex;
  }

  public List<CloneGroup> reportClones(FileBlockGroup fileBlockGroup) {
    List<ClonePair> reportedPairs = reportClonePairs(fileBlockGroup);
    List<ClonePair> filtered = INTERVAL_PAIR_FILTER.filter(reportedPairs);
    return groupClonePairs(filtered);
  }

}
