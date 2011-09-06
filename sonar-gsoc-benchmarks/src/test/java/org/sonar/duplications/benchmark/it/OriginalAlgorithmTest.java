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
import org.sonar.duplications.detector.original.OriginalCloneDetectionAlgorithm;
import org.sonar.duplications.index.CloneGroup;
import org.sonar.duplications.index.CloneIndex;

import com.google.common.collect.Lists;

/**
 * TODO Godin: explain difference in amount of clones between {@link AdvancedGroupCloneReporter} and {@link OriginalCloneDetectionAlgorithm}.
 */
public class OriginalAlgorithmTest extends ResultsTestCase {

  @Override
  public void activemq() {
    super.activemq();
    assertDuplicatedBlocks(827);
    assertThat("clones", result.clonesCount, is(788));
    assertThat("parts", result.partsCount, is(4595));
  }

  @Override
  public void struts() {
    super.struts();
    assertDuplicatedBlocks(1168);
    assertThat("clones", result.clonesCount, is(1101));
    assertThat("parts", result.partsCount, is(8688));
  }

  @Override
  public void strutsel() {
    super.strutsel();
    assertDuplicatedBlocks(962);
    assertThat("clones", result.clonesCount, is(923));
    assertThat("parts", result.partsCount, is(7452));
  }

  @Override
  public void openejb() {
    super.openejb();
    assertDuplicatedBlocks(621);
    assertThat("clones", result.clonesCount, is(619));
    assertThat("parts", result.partsCount, is(13523));
  }

  @Override
  public void easybeans() {
    super.easybeans();
    assertDuplicatedBlocks(33);
    assertThat("clones", result.clonesCount, is(31));
    assertThat("parts", result.partsCount, is(71));
  }

  @Override
  public void commonsCollections() {
    super.commonsCollections();
    assertDuplicatedBlocks(78);
    assertThat("clones", result.clonesCount, is(75));
    assertThat("parts", result.partsCount, is(222));
  }

  @Override
  public void jboss() {
    super.jboss();
    assertDuplicatedBlocks(567);
    assertThat("clones", result.clonesCount, is(323));
    assertThat("parts", result.partsCount, is(1134));
  }

  @Override
  public void neo4j() {
    super.neo4j();
    assertDuplicatedBlocks(59);
    assertThat("clones", result.clonesCount, is(42));
    assertThat("parts", result.partsCount, is(89));
  }

  @Override
  public void jackrabbit() {
    super.jackrabbit();
    assertDuplicatedBlocks(154);
    assertThat("clones", result.clonesCount, is(115));
    assertThat("parts", result.partsCount, is(311));
  }

  @Override
  public void struts2() {
    super.struts2();
    assertDuplicatedBlocks(202);
    assertThat("clones", result.clonesCount, is(83));
    assertThat("parts", result.partsCount, is(212));
  }

  @Override
  public void empire() {
    super.empire();
    assertDuplicatedBlocks(364);
    assertThat("clones", result.clonesCount, is(363));
    assertThat("parts", result.partsCount, is(1750));
  }

  @Override
  public void tomcat() {
    super.tomcat();
    assertDuplicatedBlocks(108);
    assertThat("clones", result.clonesCount, is(49));
    assertThat("parts", result.partsCount, is(116));
  }

  @Override
  public void jdk() {
    super.jdk();
    assertDuplicatedBlocks(68945);
    assertThat("clones", result.clonesCount, is(6704));
    assertThat("parts", result.partsCount, is(3487094));
  }

  @Override
  protected List<CloneGroup> analyse(CloneIndex index, String resourceId) {
    List<Block> fileBlocks = Lists.newArrayList(index.getByResourceId(resourceId));
    return OriginalCloneDetectionAlgorithm.detect(index, fileBlocks);
  }

}
