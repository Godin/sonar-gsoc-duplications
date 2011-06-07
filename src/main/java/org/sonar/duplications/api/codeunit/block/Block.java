package org.sonar.duplications.api.codeunit.block;

import org.sonar.duplications.util.BlockUtils;

import java.util.Arrays;

/**
 * @author sharif
 */
public class Block {

  private final String resourceId; //filename

  private final byte[] blockHash;

  private final int firstUnitIndex;

  private final int firstLineNumber;

  private final int lastLineNumber;

  private final int arrHashCode;

  public Block(String resourceId, byte[] blockHash,
               int firstUnitIndex, int firstLineNumber, int lastLineNumber) {
    this.resourceId = resourceId;
    this.blockHash = blockHash;
    this.firstUnitIndex = firstUnitIndex;
    this.firstLineNumber = firstLineNumber;
    this.lastLineNumber = lastLineNumber;
    this.arrHashCode = Arrays.hashCode(blockHash);
  }

  public String getResourceId() {
    return resourceId;
  }

  public byte[] getBlockHash() {
    return blockHash;
  }

  public int getFirstUnitIndex() {
    return firstUnitIndex;
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
    return other.resourceId.equals(resourceId)
        && Arrays.equals(other.blockHash, blockHash)
        && other.firstUnitIndex == firstUnitIndex
        && other.firstLineNumber == firstLineNumber
        && other.lastLineNumber == lastLineNumber;
  }

  @Override
  public int hashCode() {
    return resourceId.hashCode() + arrHashCode + 413 * firstUnitIndex;
  }

  @Override
  public String toString() {
    return "'" + resourceId + "'[" + firstUnitIndex + "|" + firstLineNumber
        + "-" + lastLineNumber + "]:" + BlockUtils.getHex(blockHash);
  }

}
