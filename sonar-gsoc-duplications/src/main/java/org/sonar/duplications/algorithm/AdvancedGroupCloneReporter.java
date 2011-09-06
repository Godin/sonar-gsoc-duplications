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

import org.sonar.duplications.algorithm.filter.IntervalTreeCloneFilter;
import org.sonar.duplications.index.CloneGroup;
import org.sonar.duplications.index.CloneIndex;

public class AdvancedGroupCloneReporter extends AbstractAdvancedCloneReporter {

  private static final IntervalTreeCloneFilter INTERVAL_FILTER = new IntervalTreeCloneFilter();

  public AdvancedGroupCloneReporter(CloneIndex cloneIndex) {
    this.cloneIndex = cloneIndex;
  }

  public List<CloneGroup> reportClones(FileBlockGroup fileBlockGroup) {
    List<ClonePair> reportedPairs = reportClonePairs(fileBlockGroup);
    List<CloneGroup> clones = groupClonePairs(reportedPairs);
    clones = INTERVAL_FILTER.filter(clones);
    return clones;
  }

}
