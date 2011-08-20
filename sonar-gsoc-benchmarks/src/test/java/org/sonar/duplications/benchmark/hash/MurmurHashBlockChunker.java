package org.sonar.duplications.benchmark.hash;

import org.sonar.duplications.block.ByteArray;
import org.sonar.duplications.statement.Statement;

import java.util.List;

public class MurmurHashBlockChunker extends AbstractHashBlockChunker {

  public MurmurHashBlockChunker(int blockSize) {
    super(blockSize);
  }

  protected ByteArray buildBlockHash(List<Statement> statementList) {
    int totalLen = 0;
    for (Statement statement : statementList) {
      totalLen += statement.getValue().getBytes().length;
    }
    byte[] bytes = new byte[totalLen];
    int current = 0;
    for (Statement statement : statementList) {
      byte[] stmtBytes = statement.getValue().getBytes();
      int length = stmtBytes.length;
      System.arraycopy(stmtBytes, 0, bytes, current, length);
      current += length;
    }
    int messageDigest = MurmurHash2.hash(bytes, 0x1234ABCD);
    return new ByteArray(messageDigest);
  }
}
