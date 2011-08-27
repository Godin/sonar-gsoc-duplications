package org.sonar.duplications.benchmark.hash;

import org.sonar.duplications.DuplicationsException;
import org.sonar.duplications.block.ByteArray;
import org.sonar.duplications.statement.Statement;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class DigestHashBlockChunker extends AbstractHashBlockChunker {

  private final MessageDigest digest;

  public static enum Algorithm {
    MD5, SHA;
  }

  public DigestHashBlockChunker(Algorithm algorithm, int blockSize) {
    this(algorithm.toString(), blockSize);
  }

  public DigestHashBlockChunker(String algorithm, int blockSize) {
    super(blockSize);
    try {
      this.digest = MessageDigest.getInstance(algorithm);
    } catch (NoSuchAlgorithmException e) {
      throw new DuplicationsException("Unable to create a digest generator", e);
    }
  }

  @Override
  protected ByteArray buildBlockHash(List<Statement> statements) {
    digest.reset();
    for (Statement statement : statements) {
      digest.update(statement.getValue().getBytes());
      // TODO Godin: separation of statements required, but I'm not sure that we use a good value for this
      digest.update((byte) 0);
    }
    return new ByteArray(digest.digest());
  }

}
