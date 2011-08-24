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
    assertThat("clones", result.clonesCount, is(811));
  }

  @Override
  public void struts() {
    super.struts();
    assertThat("clones", result.clonesCount, is(1098));
  }

  @Override
  public void strutsel() {
    super.strutsel();
    assertThat("clones", result.clonesCount, is(923));
  }

  @Override
  public void openejb() {
    super.openejb();
    assertThat("clones", result.clonesCount, is(619));
  }

  @Override
  public void easybeans() {
    super.easybeans();
    assertThat("clones", result.clonesCount, is(31));
  }

  @Override
  public void commonsCollections() {
    super.commonsCollections();
    assertThat("clones", result.clonesCount, is(78));
  }

  @Override
  public void jboss() {
    super.jboss();
    assertThat("clones", result.clonesCount, is(633));
  }

  @Override
  public void neo4j() {
    super.neo4j();
    assertThat("clones", result.clonesCount, is(41));
  }

  @Override
  public void jackrabbit() {
    super.jackrabbit();
    assertThat("clones", result.clonesCount, is(149));
  }

  @Override
  public void struts2() {
    super.struts2();
    assertThat("clones", result.clonesCount, is(93));
  }

  @Override
  public void empire() {
    super.empire();
    assertThat("clones", result.clonesCount, is(377));
  }

  @Override
  public void tomcat() {
    super.tomcat();
    assertThat("clones", result.clonesCount, is(49));
  }

  @Override
  protected List<CloneGroup> analyse(CloneIndex index, String resourceId) {
    List<Block> fileBlocks = Lists.newArrayList(index.getByResourceId(resourceId));
    return OriginalCloneDetectionAlgorithm.detect(index, fileBlocks);
  }

}
