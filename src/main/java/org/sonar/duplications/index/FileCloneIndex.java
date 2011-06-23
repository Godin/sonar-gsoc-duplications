package org.sonar.duplications.index;

import java.io.File;
import java.util.List;

import org.sonar.duplications.api.Block;

/**
 * @author sharif
 */
public class FileCloneIndex {

  private File sourceFile;
  private List<Block> blockList;

  public FileCloneIndex(File sourceFile) {
    this.sourceFile = sourceFile;
  }

  public File getSourceFile() {
    return sourceFile;
  }

  public List<Block> getBlockList() {
    return blockList;
  }
}
