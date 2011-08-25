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
package org.sonar.duplications.benchmark.it;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.sonar.duplications.algorithm.AdvancedGroupCloneReporter;
import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.FileBlockGroup;
import org.sonar.duplications.detector.original.OriginalCloneDetectionAlgorithm;
import org.sonar.duplications.index.CloneGroup;
import org.sonar.duplications.index.CloneIndex;

import com.google.common.collect.Lists;

/**
 * TODO Godin: explain difference in amount of clones between {@link AdvancedGroupCloneReporter} and {@link OriginalCloneDetectionAlgorithm}.
 */
public class AdvancedGroupCloneReporterTest extends ResultsTestCase {

  @Override
  public void activemq() {
    super.activemq();
    assertDuplicatedBlocks(882);
    assertThat("clones", result.clonesCount, is(818));
    assertThat("parts", result.partsCount, is(3730));
  }

  @Override
  public void struts() {
    super.struts();
    assertDuplicatedBlocks(1014);
    assertThat("clones", result.clonesCount, is(949));
    assertThat("parts", result.partsCount, is(3592));
  }

  @Override
  public void strutsel() {
    super.strutsel();
    assertDuplicatedBlocks(809);
    assertThat("clones", result.clonesCount, is(770));
    assertThat("parts", result.partsCount, is(2385));
  }

  @Override
  public void openejb() {
    super.openejb();
    assertDuplicatedBlocks(618);
    assertThat("clones", result.clonesCount, is(616));
    assertThat("parts", result.partsCount, is(6372));
  }

  @Override
  public void easybeans() {
    super.easybeans();
    assertDuplicatedBlocks(32);
    assertThat("clones", result.clonesCount, is(30));
    assertThat("parts", result.partsCount, is(62));
  }

  @Override
  public void commonsCollections() {
    super.commonsCollections();
    assertDuplicatedBlocks(80);
    assertThat("clones", result.clonesCount, is(77));
    assertThat("parts", result.partsCount, is(206));
  }

  @Override
  public void jboss() {
    super.jboss();
    assertDuplicatedBlocks(1810);
    assertThat("clones", result.clonesCount, is(996));
    assertThat("parts", result.partsCount, is(3672));
  }

  @Override
  public void neo4j() {
    super.neo4j();
    assertDuplicatedBlocks(60);
    assertThat("clones", result.clonesCount, is(44));
    assertThat("parts", result.partsCount, is(90));
  }

  @Override
  public void jackrabbit() {
    super.jackrabbit();
    assertDuplicatedBlocks(171);
    assertThat("clones", result.clonesCount, is(120));
    assertThat("parts", result.partsCount, is(295));
  }

  @Override
  public void struts2() {
    super.struts2();
    assertDuplicatedBlocks(245);
    assertThat("clones", result.clonesCount, is(113));
    assertThat("parts", result.partsCount, is(255));
  }

  @Override
  public void empire() {
    super.empire();
    assertDuplicatedBlocks(291);
    assertThat("clones", result.clonesCount, is(290));
    assertThat("parts", result.partsCount, is(693));
  }

  @Override
  public void tomcat() {
    super.tomcat();
    assertDuplicatedBlocks(104);
    assertThat("clones", result.clonesCount, is(54));
    assertThat("parts", result.partsCount, is(112));
  }

  @Override
  public void jdk() {
    super.jdk();
    assertDuplicatedBlocks(23754);
    assertThat("clones", result.clonesCount, is(21156));
    assertThat("parts", result.partsCount, is(212201));
  }

  @Override
  protected List<CloneGroup> analyse(CloneIndex index, String resourceId) {
    AdvancedGroupCloneReporter reporter = new AdvancedGroupCloneReporter(index);
    List<Block> fileBlocks = Lists.newArrayList(index.getByResourceId(resourceId));
    return reporter.reportClones(FileBlockGroup.create(resourceId, fileBlocks));
  }

}
