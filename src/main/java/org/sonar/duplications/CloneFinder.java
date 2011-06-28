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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.BlockChunker;
import org.sonar.duplications.index.Clone;
import org.sonar.duplications.index.CloneIndex;
import org.sonar.duplications.index.ClonePart;
import org.sonar.duplications.statement.StatementChunker;
import org.sonar.duplications.token.TokenChunker;

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

  public void register(File ... sourceFiles){
	  for (File file : sourceFiles)
		  register(file);
  }
  
  public void register(File sourceFile) {
    List<Block> blocks = blockChunker.chunk(sourceFile.getAbsolutePath(),stmtChunker.chunk(tokenChunker.chunk(sourceFile)));
    
    for(Block block : blocks)
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

	
	public List<Clone> findClones(){
		if(sourceFilesForDetection.isEmpty())
			throw new DuplicationsException("No source file added");
		
		ArrayList<Clone> clones = new ArrayList<Clone>();
		
		//2: let f be the list of tuples corresponding to filename sorted by statement index either read from the index or calculated on the fly
		List<Block> candidateBlockList = new ArrayList<Block>();
		
		for(String sourceFile : sourceFilesForDetection){
			if(cloneIndex.containsResourceId(sourceFile))
				candidateBlockList.addAll(cloneIndex.getByResourceId(sourceFile));
			else{
				//build on the fly
				register(new File(sourceFile));
			}
		}
			
		//3: let c be a list with c(0) = ; 
		List<List<Block>> sameHashBlockGroups = new ArrayList<List<Block>>();
		
		//4: for i := 1 to length(f) do
		//5: retrieve tuples with same sequence hash as f(i)
		//6: store this set as c(i)
		for(Block block : candidateBlockList){
			//if(block.isProcessed) continue;
			
			List<Block> sameHashBlockGroup = new ArrayList<Block>();
			for(Block shBlock : cloneIndex.getBySequenceHash(block.getBlockHash())){
				//shBlock.isProcessed = true;
				sameHashBlockGroup.add(shBlock);
			}
			
			//if(!sameHashBlockGroup.isEmpty())
				sameHashBlockGroups.add(sameHashBlockGroup);
		}
		
		//an empty list is needed a the end to report clone at the end of file
		//this just makes the condition at line 13 true at the last iteration
		sameHashBlockGroups.add(new ArrayList<Block>());
		
		//7: for i := 1 to length(c) do
		for(int i = 0; i <sameHashBlockGroups.size(); i++){
			List<Block> currentBlockGroup = sameHashBlockGroups.get(i);
		//8: if |c(i)| < 2 or c(i) subsumedby c(i - 1) then
			if(currentBlockGroup.size() < 2 || 
					(i > 0 && subsumedBy(currentBlockGroup, sameHashBlockGroups.get(i - 1))))
		//9: continue with next loop iteration
				continue;
		//10: let a := c(i)
			
		//11: for j := i + 1 to length(c) do
			for(int j = i+1; j<sameHashBlockGroups.size(); j++){
		//12: let a0 := a intersect c(j)
				List<Block> intersectedBlockGroup = intersect(currentBlockGroup, sameHashBlockGroups.get(j));
		//13: if |a0| < |a| then
				if(intersectedBlockGroup.size() < currentBlockGroup.size()){
		//14: report clones from c(i) to a (see text)
			          reportClone(sameHashBlockGroups.get(i), currentBlockGroup, j - i, clones);
				}
		//15: a := a0
				currentBlockGroup = intersectedBlockGroup;
		//16: if |a| < 2 or a subsumedby c(i -1) then
				if(currentBlockGroup.size() < 2 || 
						(i > 0 && subsumedBy(currentBlockGroup, sameHashBlockGroups.get(i - 1))))
		//17: break inner loop
					break;
			}
		}
		
		return clones;
	}
	
	/**
	 * reports a clone group
	 * 
	 * @param beginSet
	 * @param endSet
	 * @param cloneLength
	 * @param clones
	 */
	private static void reportClone(List<Block> beginSet, List<Block> endSet,
			int cloneLength, List<Clone> clones) {

		Clone clone = new Clone(cloneLength);
		for (Block beginBlock : beginSet) {

			for (Block endBlock : endSet) {
				if (beginBlock.getResourceId().equals(endBlock.getResourceId())
						|| beginBlock.getIndexInFile() + cloneLength - 1 == endBlock
								.getIndexInFile()) {

					ClonePart part = new ClonePart(beginBlock.getResourceId(),
							beginBlock.getIndexInFile(),
							beginBlock.getFirstLineNumber(),
							endBlock.getLastLineNumber());
					clone.addPart(part);
					break;
				}
			}

		}

		if (!clones.contains(clone))
			clones.add(clone);
	}

	/**
	 * implementation of the special subset operator used in the algorithm in text
	 * 
	 * @param list1
	 * @param list2
	 * @return
	 */
	private boolean subsumedBy(List<Block> list1, List<Block> list2) {
		boolean finalResult = true;
		boolean partialResult;
		for(Block block1 : list1){
			//search for a block2 to cover block1
			partialResult = false;
			for(Block block2 : list2){
				if(block1.getResourceId().equals(block2.getResourceId())
						&& (block1.getIndexInFile()-1) == block2.getIndexInFile()){
					//block1 is covered by block2 
					partialResult = true;
					break;
				}
			}
			
			if(!partialResult){
				//block1 is not covered by any block2 
				finalResult = false;
				break;
			}
		}
		
		return finalResult;
	}

	/**
	 * implementation of the special intersect operator used in the algorithm in text
	 * 
	 * @param list1
	 * @param list2
	 * @return
	 */
	private List<Block> intersect(List<Block> list1, List<Block> list2) {
		List<Block> result = new ArrayList<Block>();
		for(Block block1 : list1){
			for(Block block2 : list2){
				if(block1.getResourceId().equals(block2.getResourceId())
						&& block1.getIndexInFile()+1 == block2.getIndexInFile()){
					result.add(block2);
					break;
				}
			}
		}
		return result;
	}

	/**
	 * list files in a specified directory and append to fileList
	 * 
	 * @param rootDir, directory to list files
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
