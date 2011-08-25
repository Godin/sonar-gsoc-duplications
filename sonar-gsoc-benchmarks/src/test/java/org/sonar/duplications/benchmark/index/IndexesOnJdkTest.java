package org.sonar.duplications.benchmark.index;

import static org.hamcrest.Matchers.greaterThan;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.sonar.duplications.benchmark.Utils;

@Ignore("Not relevant for the moment")
public class IndexesOnJdkTest extends AbstractIndexesTestCase {

  @BeforeClass
  public static void before() {
    BENCHMARK_ROUNDS = 2;
    WARMUP_ROUNDS = 1;

    files = Utils.filesFromJdk16();
    Assume.assumeThat(files.size(), greaterThan(0));
  }

}
