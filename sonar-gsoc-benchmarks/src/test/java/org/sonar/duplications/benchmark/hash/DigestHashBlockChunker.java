package org.sonar.duplications.benchmark.hash;

import org.sonar.duplications.DuplicationsException;
import org.sonar.duplications.block.ByteArray;
import org.sonar.duplications.statement.Statement;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class DigestHashBlockChunker extends AbstractHashBlockChunker {

  protected final MessageDigest digest;

  public static enum Algorithm {
    MD5, SHA;
  }

  public DigestHashBlockChunker(Algorithm algorithm, int blockSize) {
    super(blockSize);
    try {
      this.digest = MessageDigest.getInstance(algorithm.toString());
    } catch (NoSuchAlgorithmException e) {
      throw new DuplicationsException("Unable to create a digest generator", e);
    }
  }

  protected ByteArray buildBlockHash(List<Statement> statementList) {
    digest.reset();
    for (Statement statement : statementList) {
      digest.update(statement.getValue().getBytes());
    }
    byte[] messageDigest = digest.digest();
    return new ByteArray(messageDigest);
  }
}
