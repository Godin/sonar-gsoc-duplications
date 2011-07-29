package org.sonar.duplications.benchmark.perf;

import org.junit.Test;

public class OpenEjbTest extends AbstractCompare {

  @Test
  public void test() {
    compare("openejb-jee-3.1.4");
  }

}
