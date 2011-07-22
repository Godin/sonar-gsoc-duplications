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
package org.sonar.duplications;

import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.BlockChunker;
import org.sonar.duplications.index.Clone;
import org.sonar.duplications.index.CloneIndex;
import org.sonar.duplications.index.CloneReporter;
import org.sonar.duplications.statement.Statement;
import org.sonar.duplications.statement.StatementChunker;
import org.sonar.duplications.token.TokenChunker;
import org.sonar.duplications.token.TokenQueue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CloneFinder {

  private TokenChunker tokenChunker;
  private StatementChunker stmtChunker;
  private BlockChunker blockChunker;
  private CloneIndex cloneIndex;

  private List<String> sourceFilesForDetection = new ArrayList<String>();

  private CloneFinder(Builder builder) {
    this.tokenChunker = builder.tokenChunker;
    this.stmtChunker = builder.stmtChunker;
    this.blockChunker = builder.blockChunker;
    this.cloneIndex = builder.cloneIndex;
  }

  public void register(File... sourceFiles) {
    for (File file : sourceFiles)
      register(file);
  }

  public void register(File sourceFile) {
    List<Block> blocks;
    String absolutePath = sourceFile.getAbsolutePath();
    try {
      TokenQueue tokenQueue = tokenChunker.chunk(sourceFile);
      List<Statement> statements = stmtChunker.chunk(tokenQueue);
      blocks = blockChunker.chunk(absolutePath, statements);
    } catch (Exception e) {
      throw new DuplicationsException("Exception during registering file: " + absolutePath, e);
    }

    for (Block block : blocks)
      cloneIndex.insert(block);
  }

  public static Builder build() {
    return new Builder();
  }

  public static final class Builder {

    private TokenChunker tokenChunker;
    private StatementChunker stmtChunker;
    private BlockChunker blockChunker;
    private CloneIndex cloneIndex;

    public Builder setTokenChunker(TokenChunker tokenChunker) {
      this.tokenChunker = tokenChunker;
      return this;
    }

    public Builder setStatementChunker(StatementChunker stmtChunker) {
      this.stmtChunker = stmtChunker;
      return this;
    }

    public Builder setBlockChunker(BlockChunker blockChunker) {
      this.blockChunker = blockChunker;
      return this;
    }

    public Builder setCloneIndex(CloneIndex cloneIndex) {
      this.cloneIndex = cloneIndex;
      return this;
    }

    public CloneFinder build() {
      return new CloneFinder(this);
    }
  }

  public void addSourceFileForDetection(String fileName) {
    File file;
    try {
      file = new File(fileName);
      if (!file.isFile())
        throw new Exception("Invalid file name :" + fileName);
    } catch (Exception e) {
      throw new DuplicationsException("Invalid file name :" + fileName, e);
    }

    sourceFilesForDetection.add(file.getAbsolutePath());
  }

  public void clearSourceFilesForDetection() {
    sourceFilesForDetection.clear();
  }

  public void addSourceDirectoryForDetection(String dirName) {
    File file;
    try {
      file = new File(dirName);
      if (!file.isDirectory())
        throw new Exception("Invalid directory name :" + dirName);
    } catch (Exception e) {
      throw new DuplicationsException("Invalid directory name :" + dirName, e);
    }

    try {
      listFiles(file, sourceFilesForDetection);
    } catch (IOException e) {
      throw new DuplicationsException("Error in directory listing :" + dirName, e);
    }
  }


  public List<Clone> findClones() {
    if (sourceFilesForDetection.isEmpty())
      throw new DuplicationsException("No source file added");

    List<Block> candidateBlockList = new ArrayList<Block>();

    for (String sourceFile : sourceFilesForDetection) {
      if (cloneIndex.containsResourceId(sourceFile))
        candidateBlockList.addAll(cloneIndex.getByResourceId(sourceFile));
      else {
        //build on the fly
        register(new File(sourceFile));
      }
    }

    return CloneReporter.reportClones(candidateBlockList, cloneIndex);
  }

  /**
   * list files in a specified directory and append to fileList
   *
   * @param rootDir,  directory to list files
   * @param fileList, file name list to add files in the specified directory
   * @throws IOException
   */
  private void listFiles(File rootDir, List<String> fileList)
      throws IOException {
    if (rootDir.isDirectory()) {
      String[] children = rootDir.list();
      for (String child : children) {
        listFiles(new File(rootDir, child), fileList);
      }
    } else {
      fileList.add(rootDir.getCanonicalPath());
    }
  }

}
