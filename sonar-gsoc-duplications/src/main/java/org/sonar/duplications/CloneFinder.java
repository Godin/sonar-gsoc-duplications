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
import org.sonar.duplications.index.ClonePart;
import org.sonar.duplications.statement.Statement;
import org.sonar.duplications.statement.StatementChunker;
import org.sonar.duplications.token.TokenChunker;
import org.sonar.duplications.token.TokenQueue;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CloneFinder {

  private static class Key implements Comparable<Key> {
    private String resourceId;
    private int unitNum;

    private Key(String resourceId, int unitNum) {
      this.resourceId = resourceId;
      this.unitNum = unitNum;
    }

    public int compareTo(Key o) {
      if (this.resourceId.equals(o.resourceId)) {
        return this.unitNum - o.unitNum;
      }
      return this.resourceId.compareTo(o.resourceId);
    }
  }

  private static class TempClone {
    private ClonePart origPart;
    private ClonePart anotherPart;
    private int cloneLength;

    private TempClone(ClonePart origPart, ClonePart anotherPart, int cloneLength) {
      this.origPart = origPart;
      this.anotherPart = anotherPart;
      this.cloneLength = cloneLength;
    }

    public ClonePart getOrigPart() {
      return origPart;
    }

    public void setOrigPart(ClonePart origPart) {
      this.origPart = origPart;
    }

    public ClonePart getAnotherPart() {
      return anotherPart;
    }

    public void setAnotherPart(ClonePart anotherPart) {
      this.anotherPart = anotherPart;
    }

    public int getCloneLength() {
      return cloneLength;
    }

    public void setCloneLength(int cloneLength) {
      this.cloneLength = cloneLength;
    }
  }


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

    ArrayList<Clone> clones = new ArrayList<Clone>();
    ArrayList<Block> resourceBlocks = new ArrayList<Block>();

    List<Block> candidateBlockList = new ArrayList<Block>();

    for (String sourceFile : sourceFilesForDetection) {
      if (cloneIndex.containsResourceId(sourceFile))
        candidateBlockList.addAll(cloneIndex.getByResourceId(sourceFile));
      else {
        //build on the fly
        register(new File(sourceFile));
      }
    }
    List<List<Block>> sameHashBlockGroups = new ArrayList<List<Block>>();

    for (Block block : candidateBlockList) {
      List<Block> sameHashBlockGroup = new ArrayList<Block>();
      for (Block shBlock : cloneIndex.getBySequenceHash(block.getBlockHash())) {
        if (!shBlock.getResourceId().equals(block.getResourceId()) ||
            shBlock.getIndexInFile() > block.getIndexInFile()) {
          sameHashBlockGroup.add(shBlock);
        }
      }
      sameHashBlockGroups.add(sameHashBlockGroup);
      resourceBlocks.add(block);
    }

    //an empty list is needed a the end to report clone at the end of file
    sameHashBlockGroups.add(new ArrayList<Block>());

    TreeMap<Key, TempClone> prevActiveMap = new TreeMap<Key, TempClone>();

    for (int i = 0; i < sameHashBlockGroups.size(); i++) {
      TreeMap<Key, TempClone> nextActiveMap = new TreeMap<Key, TempClone>();

      for (Block block : sameHashBlockGroups.get(i)) {
        Block origBlock = resourceBlocks.get(i);
        processBlock(prevActiveMap, nextActiveMap, origBlock, block);
      }
      //sort elements of prevActiveMap by getOrigPart.getUnitStart()
      ArrayList<TempClone> sortedArr = new ArrayList<TempClone>(prevActiveMap.values());
      Collections.sort(sortedArr, new Comparator<TempClone>() {
        public int compare(TempClone o1, TempClone o2) {
          return o1.getOrigPart().getUnitStart() - o2.getOrigPart().getUnitStart();
        }
      });

      clones.addAll(reportClones(sortedArr));

      prevActiveMap = nextActiveMap;
    }

    return clones;
  }

  /**
   * @param sortedArr, array of TempClone sorted by getOrigPart().getUnitStart()
   * @return list of reported clones
   */
  private static List<Clone> reportClones(List<TempClone> sortedArr) {
    List<Clone> res = new ArrayList<Clone>();
    Clone curClone = null;
    int prevUnitStart = -1;
    for (int j = 0; j < sortedArr.size(); j++) {
      TempClone tempClone = sortedArr.get(j);
      int curUnitStart = tempClone.getOrigPart().getUnitStart();
      //if current sequence matches with different sequence in original file
      if (curUnitStart != prevUnitStart) {
        curClone = new Clone(tempClone.getCloneLength());
        curClone.addPart(tempClone.getOrigPart());
        curClone.addPart(tempClone.getAnotherPart());
        res.add(curClone);
      } else {
        curClone.addPart(tempClone.getAnotherPart());
      }
      prevUnitStart = curUnitStart;
    }
    return res;
  }

  /**
   * processes curren block - checks if current block continues one of block sequences
   * or creates new block sequence. sequences (<tt>TempClone</tt>) are put to
   * <tt>nextActiveMap</tt>
   *
   * @param prevActiveMap, map with active block sequences from previous cycle iteration
   * @param nextActiveMap, map with active block sequences after current cycle iteration
   * @param origBlock,     block of original file
   * @param block,         one of blocks with same hash as <tt>origBlock</tt>
   */
  private static void processBlock(TreeMap<Key, TempClone> prevActiveMap,
                                   TreeMap<Key, TempClone> nextActiveMap,
                                   Block origBlock, Block block) {
    ClonePart origPart = new ClonePart(origBlock);
    ClonePart anotherPart = new ClonePart(block);
    int cloneLength = 0;

    Key curKey = new Key(block.getResourceId(), block.getIndexInFile());
    if (prevActiveMap.containsKey(curKey)) {
      TempClone prevPart = prevActiveMap.get(curKey);

      origPart.setLineStart(prevPart.getOrigPart().getLineStart());
      origPart.setUnitStart(prevPart.getOrigPart().getUnitStart());

      anotherPart.setLineStart(prevPart.getAnotherPart().getLineStart());
      anotherPart.setUnitStart(prevPart.getAnotherPart().getUnitStart());

      cloneLength = prevPart.getCloneLength();

      prevActiveMap.remove(curKey);
    }

    TempClone tempClone = new TempClone(origPart, anotherPart, cloneLength + 1);

    Key nextKey = new Key(block.getResourceId(), block.getIndexInFile() + 1);
    nextActiveMap.put(nextKey, tempClone);
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
      for (int i = 0; i < children.length; i++) {
        listFiles(new File(rootDir, children[i]), fileList);
      }
    } else {
      fileList.add(rootDir.getCanonicalPath());
    }
  }

}
