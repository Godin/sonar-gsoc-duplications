package org.sonar.duplications.benchmark.index;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.sonar.duplications.benchmark.Utils;

@Ignore("Not relevant for the moment")
public class IndexesOnDifferentProjectsTest extends AbstractIndexesTestCase {

  @BeforeClass
  public static void before() {
    files = Utils.filesFromDifferentProjects();
  }

}
