package org.sonar.duplications.api.codeunit;

/**
 * @author sharif
 *
 */
public class Block {

	private final String originId; //filename

	private final byte[] blockHash;

	private final int firstUnitIndex;

	private final int firstLineNumber;

	private final int lastLineNumber;

	public Block(String originId, byte[] blockHash,
			int firstUnitIndex, int firstLineNumber, int lastLineNumber) {
		this.originId = originId;
		this.blockHash = blockHash;
		this.firstUnitIndex = firstUnitIndex;
		this.firstLineNumber = firstLineNumber;
		this.lastLineNumber = lastLineNumber;
	}

	public String getOriginId() {
		return originId;
	}

	public byte[] getChunkHash() {
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
		return other.originId.equals(originId)
				&& other.blockHash == blockHash
				&& other.firstUnitIndex == firstUnitIndex
				&& other.firstLineNumber == firstLineNumber
				&& other.lastLineNumber == lastLineNumber;
	}

	@Override
	public int hashCode() {
		return originId.hashCode() + blockHash.hashCode() + 413 * firstUnitIndex;
	}
	
	@Override
	public String toString() {
		return "["+ firstUnitIndex +"] : {"+ firstLineNumber+"-"+lastLineNumber+ "} ("+blockHash.hashCode()+")";
	}
}
