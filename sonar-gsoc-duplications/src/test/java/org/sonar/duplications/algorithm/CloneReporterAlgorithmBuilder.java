package org.sonar.duplications.algorithm;

import org.sonar.duplications.index.CloneIndex;

public interface CloneReporterAlgorithmBuilder {
  public CloneReporterAlgorithm build(CloneIndex index);
}
