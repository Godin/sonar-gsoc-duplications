package org.sonar.duplications;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.sonar.duplications.algorithm.AdvancedGroupCloneReporter;
import org.sonar.duplications.algorithm.AdvancedPairCloneReporter;
import org.sonar.duplications.algorithm.CloneReporterAlgorithm;
import org.sonar.duplications.algorithm.CloneReporterAlgorithmBuilder;
import org.sonar.duplications.index.CloneIndex;

import java.util.Arrays;
import java.util.Collection;

@RunWith(value = Parameterized.class)
public abstract class BaseCloneReporterTest {

  protected CloneReporterAlgorithmBuilder cloneReporterBuilder;

  @Parameters
  public static Collection<Object[]> data() {
    CloneReporterAlgorithmBuilder builder1 = new CloneReporterAlgorithmBuilder() {
      public CloneReporterAlgorithm build(CloneIndex index) {
        return new AdvancedGroupCloneReporter(index);
      }
    };

    CloneReporterAlgorithmBuilder builder2 = new CloneReporterAlgorithmBuilder() {
      public CloneReporterAlgorithm build(CloneIndex index) {
        return new AdvancedPairCloneReporter(index);
      }
    };
    Object[][] data = new Object[][]{{builder1}, {builder2}};
    return Arrays.asList(data);
  }

  public BaseCloneReporterTest(CloneReporterAlgorithmBuilder builder) {
    this.cloneReporterBuilder = builder;
  }

}
