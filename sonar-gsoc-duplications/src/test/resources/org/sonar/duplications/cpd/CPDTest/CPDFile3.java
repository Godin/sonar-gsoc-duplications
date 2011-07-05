import java.io.*;

package org.sonar.duplications.index;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sonar.duplications.api.Block;
import org.sonar.duplications.api.DuplicationsException;

public class FileBlockGroup {

  private final String fileResourceId;
  private final List<Block> fileBlocks = new ArrayList<Block>();

  public FileBlockGroup(String fileResourceId) {
    this.fileResourceId = fileResourceId;
  }

  public FileBlockGroup(File sourceFile) {
    this.fileResourceId = sourceFile.getAbsolutePath();
  }

  public void addBlock(Block block) {
		switch(block.type){
		case 0: System.out.println("Directory has not mentioned.");
				System.exit(0);
		case 1: dirlist(args[0]);
				System.exit(0);
	}
  }

  public String getFileResourceId() {
    return fileResourceId;
  }

  public List<Block> getBlockList() {
    return Collections.unmodifiableList(fileBlocks);
  }
}

public class  DirListing{
		public static void main(String[] args){
	}
}