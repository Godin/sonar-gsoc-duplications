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

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.sonar.duplications.benchmark.Utils;
import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.BlockChunker;
import org.sonar.duplications.index.CloneGroup;
import org.sonar.duplications.index.CloneIndex;
import org.sonar.duplications.index.ClonePart;
import org.sonar.duplications.index.MemoryCloneIndex;
import org.sonar.duplications.java.JavaStatementBuilder;
import org.sonar.duplications.java.JavaTokenProducer;
import org.sonar.duplications.statement.Statement;
import org.sonar.duplications.statement.StatementChunker;
import org.sonar.duplications.token.TokenChunker;
import org.sonar.duplications.token.TokenQueue;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

/**
 * This is a regression test, which verifies results of algorithm on real projects.
 * Causes of failure of this test include:
 * <ul>
 * <li>changes in algorithm</li>
 * <li>changes in chunkers (token, statement, block)</li>
 * <li>changes in index</li>
 * </ul>
 * 
 * Note that difference between number of files and number of resources from index
 * comes from the fact that not all files have enough blocks.
 * Thus those files wouldn't be analysed.
 * TODO Godin: Examine those files and maybe tune size of block, because in some cases difference looks significant.
 * Article recommends value 5, however Freddy thinks that value might be different for different languages.
 */
public abstract class ResultsTestCase {

  private static final int BLOCK_SIZE = 13;

  private CloneIndex index;

  protected Result result;

  @Before
  public void setUp() {
    index = new MemoryCloneIndex();
  }

  @Test
  public void activemq() {
    List<File> files = Utils.getProjectFiles("activemq-core-5.5.0");
    // note that amount of files is 1354
    int filesWithoutBlocks = populateIndex(files);
    int resourcesInIndex = index.getAllUniqueResourceId().size();
    assertThat("files without blocks", filesWithoutBlocks, is(275));
    assertThat("resources in index", resourcesInIndex, is(1079));
    assertThat(filesWithoutBlocks + resourcesInIndex, is(files.size()));
    analyse(index, files);
    assertThat("resources with duplications", result.duplicatedLines.keySet().size(), is(490));
    assertThat("duplicated lines", result.duplicatedLines.entries().size(), is(47605));
  }

  @Test
  public void struts() {
    List<File> files = Utils.getProjectFiles("struts-1.3.9");
    // note that amount of files is 525
    int filesWithoutBlocks = populateIndex(files);
    int resourcesInIndex = index.getAllUniqueResourceId().size();
    assertThat("files without blocks", filesWithoutBlocks, is(113));
    assertThat("resources in index", resourcesInIndex, is(412));
    assertThat(filesWithoutBlocks + resourcesInIndex, is(files.size()));
    analyse(index, files);
    assertThat("resources with duplications", result.duplicatedLines.keySet().size(), is(137));
    assertThat("duplicated lines", result.duplicatedLines.entries().size(), is(25039));
  }

  @Test
  public void strutsel() {
    List<File> files = Utils.getProjectFiles("struts-el-1.2.9");
    // note that amount of files is 106
    int filesWithoutBlocks = populateIndex(files);
    int resourcesInIndex = index.getAllUniqueResourceId().size();
    assertThat("files without blocks", filesWithoutBlocks, is(5));
    assertThat("resources in index", resourcesInIndex, is(101));
    assertThat(filesWithoutBlocks + resourcesInIndex, is(files.size()));
    analyse(index, files);
    assertThat("resources with duplications", result.duplicatedLines.keySet().size(), is(66));
    assertThat("duplicated lines", result.duplicatedLines.entries().size(), is(13489));
  }

  @Test
  public void openejb() {
    List<File> files = Utils.getProjectFiles("openejb-jee-3.1.4");
    // note that amount of files is 900
    int filesWithoutBlocks = populateIndex(files);
    int resourcesInIndex = index.getAllUniqueResourceId().size();
    assertThat("files without blocks", filesWithoutBlocks, is(291));
    assertThat("resources in index", resourcesInIndex, is(609));
    assertThat(filesWithoutBlocks + resourcesInIndex, is(files.size()));
    analyse(index, files);
    assertThat("resources with duplications", result.duplicatedLines.keySet().size(), is(182));
    assertThat("duplicated lines", result.duplicatedLines.entries().size(), is(23081));
  }

  @Test
  public void easybeans() {
    List<File> files = Utils.getProjectFiles("easybeans-core-1.2.1");
    // note that amount of files is 188
    int filesWithoutBlocks = populateIndex(files);
    int resourcesInIndex = index.getAllUniqueResourceId().size();
    assertThat("files without blocks", filesWithoutBlocks, is(57));
    assertThat("resources in index", resourcesInIndex, is(131));
    assertThat(filesWithoutBlocks + resourcesInIndex, is(files.size()));
    analyse(index, files);
    assertThat("resources with duplications", result.duplicatedLines.keySet().size(), is(17));
    assertThat("duplicated lines", result.duplicatedLines.entries().size(), is(960));
  }

  @Test
  public void commonsCollections() {
    List<File> files = Utils.getProjectFiles("commons-collections-3.2");
    // note that amount of files is 273
    int filesWithoutBlocks = populateIndex(files);
    int resourcesInIndex = index.getAllUniqueResourceId().size();
    assertThat("files without blocks", filesWithoutBlocks, is(70));
    assertThat("resources in index", resourcesInIndex, is(203));
    assertThat(filesWithoutBlocks + resourcesInIndex, is(files.size()));
    analyse(index, files);
    assertThat("resources with duplications", result.duplicatedLines.keySet().size(), is(43));
    assertThat("duplicated lines", result.duplicatedLines.entries().size(), is(4272));
  }

  @Test
  public void jboss() {
    List<File> files = Utils.getProjectFiles("jboss-as-server-6.0.0.Final");
    // note that amount of files is 722
    int filesWithoutBlocks = populateIndex(files);
    int resourcesInIndex = index.getAllUniqueResourceId().size();
    assertThat("files without blocks", filesWithoutBlocks, is(253));
    assertThat("resources in index", resourcesInIndex, is(469));
    assertThat(filesWithoutBlocks + resourcesInIndex, is(files.size()));
    analyse(index, files);
    assertThat("resources with duplications", result.duplicatedLines.keySet().size(), is(80));
    assertThat("duplicated lines", result.duplicatedLines.entries().size(), is(23656));
  }

  @Test
  public void neo4j() {
    List<File> files = Utils.getProjectFiles("neo4j-kernel-1.4");
    // note that amount of files is 317
    int filesWithoutBlocks = populateIndex(files);
    int resourcesInIndex = index.getAllUniqueResourceId().size();
    assertThat("files without blocks", filesWithoutBlocks, is(125));
    assertThat("resources in index", resourcesInIndex, is(192));
    assertThat(filesWithoutBlocks + resourcesInIndex, is(files.size()));
    analyse(index, files);
    assertThat("resources with duplications", result.duplicatedLines.keySet().size(), is(15));
    assertThat("duplicated lines", result.duplicatedLines.entries().size(), is(2143));
  }

  @Test
  public void jackrabbit() {
    List<File> files = Utils.getProjectFiles("jackrabbit-jcr-tests-2.2.7");
    // note that amount of files is 312
    int filesWithoutBlocks = populateIndex(files);
    int resourcesInIndex = index.getAllUniqueResourceId().size();
    assertThat("files without blocks", filesWithoutBlocks, is(29));
    assertThat("resources in index", resourcesInIndex, is(283));
    assertThat(filesWithoutBlocks + resourcesInIndex, is(files.size()));
    analyse(index, files);
    assertThat("resources with duplications", result.duplicatedLines.keySet().size(), is(56));
    assertThat("duplicated lines", result.duplicatedLines.entries().size(), is(8373));
  }

  @Test
  public void struts2() {
    List<File> files = Utils.getProjectFiles("struts2-embeddedjsp-plugin-2.2.3");
    // note that amount of files is 177
    int filesWithoutBlocks = populateIndex(files);
    int resourcesInIndex = index.getAllUniqueResourceId().size();
    assertThat("files without blocks", filesWithoutBlocks, is(43));
    assertThat("resources in index", resourcesInIndex, is(134));
    assertThat(filesWithoutBlocks + resourcesInIndex, is(files.size()));
    analyse(index, files);
    assertThat("resources with duplications", result.duplicatedLines.keySet().size(), is(25));
    assertThat("duplicated lines", result.duplicatedLines.entries().size(), is(3098));
  }

  @Test
  public void empire() {
    List<File> files = Utils.getProjectFiles("empire-db-2.1.0-incubating");
    // note that amount of files is 82
    int filesWithoutBlocks = populateIndex(files);
    int resourcesInIndex = index.getAllUniqueResourceId().size();
    assertThat("files without blocks", filesWithoutBlocks, is(12));
    assertThat("resources in index", resourcesInIndex, is(70));
    assertThat(filesWithoutBlocks + resourcesInIndex, is(files.size()));
    analyse(index, files);
    assertThat("resources with duplications", result.duplicatedLines.keySet().size(), is(9));
    assertThat("duplicated lines", result.duplicatedLines.entries().size(), is(4194));
  }

  @Test
  public void tomcat() {
    List<File> files = Utils.getProjectFiles("tomcat-jasper-7.0.19");
    // note that amount of files is 118
    int filesWithoutBlocks = populateIndex(files);
    int resourcesInIndex = index.getAllUniqueResourceId().size();
    assertThat("files without blocks", filesWithoutBlocks, is(19));
    assertThat("resources in index", resourcesInIndex, is(99));
    assertThat(filesWithoutBlocks + resourcesInIndex, is(files.size()));
    analyse(index, files);
    assertThat("resources with duplications", result.duplicatedLines.keySet().size(), is(19));
    assertThat("duplicated lines", result.duplicatedLines.entries().size(), is(1929));
  }

  @Ignore
  @Test
  public void jdk() {
    List<File> files = Utils.filesFromJdk16();
    Assume.assumeThat(files.size(), greaterThan(0));
    // note that amount of files is 7213
    int filesWithoutBlocks = populateIndex(files);
    int resourcesInIndex = index.getAllUniqueResourceId().size();
    assertThat("files without blocks", filesWithoutBlocks, is(2921));
    assertThat("resources in index", resourcesInIndex, is(4292));
    assertThat(filesWithoutBlocks + resourcesInIndex, is(files.size()));
    analyse(index, files);
    assertThat("resources with duplications", result.duplicatedLines.keySet().size(), is(1097));
    assertThat("duplicated lines", result.duplicatedLines.entries().size(), is(180600));
  }

  /**
   * @return number of files without blocks
   */
  protected int populateIndex(List<File> files) {
    int result = 0;
    TokenChunker tokenChunker = JavaTokenProducer.build();
    StatementChunker statementChunker = JavaStatementBuilder.build();
    BlockChunker blockChunker = new BlockChunker(BLOCK_SIZE);
    for (File file : files) {
      TokenQueue tokenQueue = tokenChunker.chunk(file);
      List<Statement> statements = statementChunker.chunk(tokenQueue);
      List<Block> blocks = blockChunker.chunk(file.getAbsolutePath(), statements);
      if (blocks.isEmpty()) {
        result++;
      }
      for (Block block : blocks) {
        index.insert(block);
      }
    }
    return result;
  }

  protected void analyse(CloneIndex index, List<File> files) {
    result = new Result();
    for (File file : files) {
      result.cumulate(analyse(index, file.getAbsolutePath()));
    }
    // Check consistency between what we found and what would be reported in Sonar
    assertTrue(result.duplicatedFiles == result.duplicatedLines.keySet().size());
    assertTrue(result.duplicatedLinesFromOrigins.keySet().size() == result.duplicatedLines.keySet().size());
    assertTrue(result.duplicatedLinesFromOrigins.size() == result.duplicatedLines.size());
  }

  static class Result {
    /**
     * This is what would be reported in Sonar as a Duplicated lines (Number of physical lines touched by a duplication).
     */
    SetMultimap<String, Integer> duplicatedLinesFromOrigins = HashMultimap.create();

    /**
     * This is what would be reported in Sonar as a Duplicated blocks (number of duplicated blocks of lines).
     */
    int blocks;

    /**
     * This is what would be reported in Sonar as a Duplicated files (number of files involved in a duplication of lines).
     */
    int duplicatedFiles;

    SetMultimap<String, Integer> duplicatedLines = HashMultimap.create();
    int clonesCount;
    int partsCount;

    public void cumulate(List<CloneGroup> clones) {
      if (clones.isEmpty()) {
        return;
      }
      duplicatedFiles++;
      clonesCount += clones.size();
      for (CloneGroup clone : clones) {
        partsCount += clone.getCloneParts().size();
        cumulate(clone);
      }
    }

    private void cumulate(CloneGroup clone) {
      ClonePart origin = clone.getOriginPart();
      for (ClonePart part : clone.getCloneParts()) {
        // Accumulate duplicate lines from all parts
        for (int line = part.getLineStart(); line <= part.getLineEnd(); line++) {
          duplicatedLines.put(part.getResourceId(), line);
        }

        // Accumulate duplicate lines from origin parts - this is exactly what will report Sonar
        if (part.getResourceId().equals(origin.getResourceId())) {
          blocks++;
          for (int line = part.getLineStart(); line <= part.getLineEnd(); line++) {
            duplicatedLinesFromOrigins.put(part.getResourceId(), line);
          }
        }
      }
    }
  }

  protected abstract List<CloneGroup> analyse(CloneIndex index, String resourceId);

}
