package org.sonar.duplications.benchmark.hash;

import org.sonar.duplications.DuplicationsException;
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

  private static final String HEXES = "0123456789abcdef";

  private String getHex(byte[] raw) {
    if (raw == null) {
      return null;
    }
    final StringBuilder hex = new StringBuilder(2 * raw.length);
    for (final byte b : raw) {
      hex.append(HEXES.charAt((b & 0xF0) >> 4))
          .append(HEXES.charAt((b & 0x0F)));
    }
    return hex.toString();
  }

  protected String buildBlockHash(List<Statement> statementList) {
    digest.reset();
    for (Statement statement : statementList) {
      digest.update(statement.getValue().getBytes());
    }
    byte[] messageDigest = digest.digest();
    return getHex(messageDigest);
  }
}
