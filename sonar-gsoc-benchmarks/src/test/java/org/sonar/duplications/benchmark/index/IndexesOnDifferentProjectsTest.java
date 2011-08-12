package org.sonar.duplications.benchmark.index;

import org.junit.BeforeClass;
import org.sonar.duplications.benchmark.Utils;

public class IndexesOnDifferentProjectsTest extends AbstractIndexesTestCase {

  @BeforeClass
  public static void before() {
    files = Utils.filesFromDifferentProjects();
  }

}
