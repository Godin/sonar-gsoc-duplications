package org.sonar.duplications.block;

/**
 * @author sharif
 */
public class Block {

  private final String resourceId;
  private final String blockHash;
  private final int indexInFile;
  private final int firstLineNumber;
  private final int lastLineNumber;

  private int hash;

  public Block(String resourceId, String blockHash, int indexInFile, int firstLineNumber, int lastLineNumber) {
    this.resourceId = resourceId;
    this.blockHash = blockHash;
    this.indexInFile = indexInFile;
    this.firstLineNumber = firstLineNumber;
    this.lastLineNumber = lastLineNumber;
  }

  public String getResourceId() {
    return resourceId;
  }

  public String getBlockHash() {
    return blockHash;
  }

  public int getIndexInFile() {
    return indexInFile;
  }

  public int getFirstLineNumber() {
    return firstLineNumber;
  }

  public int getLastLineNumber() {
    return lastLineNumber;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Block)) {
      return false;
    }
    Block other = (Block) obj;
    return other.resourceId.equals(resourceId) && other.blockHash.equals(blockHash) && other.indexInFile == indexInFile
        && other.firstLineNumber == firstLineNumber && other.lastLineNumber == lastLineNumber;
  }

  @Override
  public int hashCode() {
    int h = hash;
    if (h == 0) {
      h = resourceId.hashCode();
      h = 31 * h + blockHash.hashCode();
      h = 31 * h + indexInFile;
      h = 31 * h + firstLineNumber;
      h = 31 * h + lastLineNumber;
      hash = h;
    }
    return h;
  }

  @Override
  public String toString() {
    return "'" + resourceId + "'[" + indexInFile + "|" + firstLineNumber + "-" + lastLineNumber + "]:" + blockHash;
  }

}
