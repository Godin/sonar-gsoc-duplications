/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2011 SonarSource
 * Written (W) 2011 Andrew Tereskin
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

import org.sonar.duplications.algorithm.filter.CloneFilter;
import org.sonar.duplications.algorithm.filter.IntervalTreeCloneFilter;
import org.sonar.duplications.block.FileBlockGroup;
import org.sonar.duplications.index.CloneGroup;
import org.sonar.duplications.index.CloneIndex;
import org.sonar.duplications.index.ClonePair;

import java.util.List;

public class AdvancedGroupCloneReporter extends AbstractAdvancedCloneReporter {

  public static final CloneFilter INTERVAL_FILTER = new IntervalTreeCloneFilter();

  public static final String ALGORITHM_KEY = "main algorithm";
  public static final String INIT_KEY = "initialization";
  public static final String FILTER_KEY = "filter covered";
  public static final String GROUPS_KEY = "report clones";

  public AdvancedGroupCloneReporter(CloneIndex cloneIndex) {
    this.cloneIndex = cloneIndex;
    this.statsCollector = new StatsCollector("AdvancedGrouped");
  }

  public List<CloneGroup> reportClones(FileBlockGroup fileBlockGroup) {

    List<ClonePair> reportedPairs = reportClonePairs(fileBlockGroup);

    statsCollector.startTime(GROUPS_KEY);
    List<CloneGroup> clones = groupClonePairs(reportedPairs);
    statsCollector.addNumber("reported clones", clones.size());
    statsCollector.stopTime(GROUPS_KEY);

    int sizeBefore = clones.size();
    statsCollector.startTime(FILTER_KEY);
    clones = INTERVAL_FILTER.filter(clones);
    statsCollector.stopTime(FILTER_KEY);

    statsCollector.addNumber("removed covered", sizeBefore - clones.size());
    statsCollector.addNumber("total clone groups", clones.size());

    return clones;
  }

}
