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

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.pmd.cpd.TokenEntry;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.sonar.duplications.cpd.Match;
import org.sonar.duplications.index.Clone;
import org.sonar.duplications.index.ClonePart;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

public class CompareResultsTest {

  private static final String[] PROJECTS = {
      "commons-collections-3.2",
      "struts-el-1.2.9",
      "activemq-core-5.5.0",
      "empire-db-2.1.0-incubating",
      "openejb-jee-3.1.4",
      "easybeans-core-1.2.1",
      "neo4j-kernel-1.4",
      "struts-1.3.9",
      "jboss-as-server-6.0.0.Final",
      "jackrabbit-jcr-tests-2.2.7",
  };

  private int MIN_BLOCK_SIZE = 12;
  private int MAX_BLOCK_SIZE = 14;

  @Test
  public void test() {
    // calculate and report
    System.out.println(", " + StringUtils.join(PROJECTS, ','));
    Double[][] results = new Double[MAX_BLOCK_SIZE + 1][PROJECTS.length];

    int[] oldCpdForProject = new int[PROJECTS.length];
    for (int prj = 0; prj < PROJECTS.length; prj++) {
      oldCpdForProject[prj] = runOldCpd(Utils.getProjectFiles(PROJECTS[prj]));
    }

    for (int blockSize = MIN_BLOCK_SIZE; blockSize <= MAX_BLOCK_SIZE; blockSize++) {
      for (int prj = 0; prj < PROJECTS.length; prj++) {
        int oldValue = oldCpdForProject[prj];
        int newValue = runNewCpd(Utils.getProjectFiles(PROJECTS[prj]), blockSize);
        results[blockSize][prj] = (oldValue - newValue) / (double) oldValue * 100;
      }
      System.out.println(blockSize + ", " + StringUtils.join(results[blockSize], ','));
    }
    // analyse
    int optimalBlockSize = 0;
    double minAvg = Double.MAX_VALUE;
    for (int blockSize = MIN_BLOCK_SIZE; blockSize <= MAX_BLOCK_SIZE; blockSize++) {
      double sum = 0;
      for (double d : results[blockSize]) {
        double l = Math.abs(d);
        sum += l;
      }
      double avg = sum / PROJECTS.length;
      if (avg < minAvg) {
        minAvg = avg;
        optimalBlockSize = blockSize;
      }
    }
    System.out.println("blockSize=" + optimalBlockSize);
    // Lock results
    for (int prj = 0; prj < PROJECTS.length - 2; prj++) { // excluding jackrabbit-jcr-tests and jboss-as-server
      assertThat(results[optimalBlockSize][prj], closeTo(0, 10));
    }
    assertThat(minAvg, closeTo(6, 2));
    assertThat(optimalBlockSize, is(13));
  }

  private int runOldCpd(List<File> files) {
    SetMultimap<String, Integer> duplicatedLines = HashMultimap.create();
    Iterator<Match> matches = OldCpdBenchmark.singleRun(files);
    while (matches.hasNext()) {
      Match match = matches.next();
      for (TokenEntry mark : match.getMarkSet()) {
        String srcId = mark.getTokenSrcID();
        for (int i = 0; i < match.getLineCount(); i++) {
          int line = mark.getBeginLine() + i;
          duplicatedLines.put(srcId, line);
        }
      }
    }
    return duplicatedLines.entries().size();
  }

  private int runNewCpd(List<File> files, int blockSize) {
    SetMultimap<String, Integer> duplicatedLines = HashMultimap.create();
    List<Clone> clones = NewCpdBenchmark.singleRun(files, blockSize);
    for (Clone clone : clones) {
      for (ClonePart clonePart : clone.getCloneParts()) {
        String resourceId = clonePart.getResourceId();
        for (int line = clonePart.getLineStart(); line <= clonePart.getLineEnd(); line++) {
          duplicatedLines.put(resourceId, line);
        }
      }
    }
    return duplicatedLines.entries().size();
  }

}
