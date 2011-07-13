package org.sonar.duplications;

import org.junit.Before;
import org.junit.Test;
import org.sonar.duplications.index.Clone;
import org.sonar.duplications.index.ClonePart;
import org.sonar.duplications.index.MemoryCloneIndex;
import org.sonar.duplications.java.JavaCloneFinder;

import java.io.File;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DuplicationTest {

  private File file1 = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest/CPDFile1.java");
  private File file2 = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest/CPDFile2.java");
  private File file3 = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest/CPDFile3.java");
  private File dir = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest/");

  private MemoryCloneIndex mci;
  private CloneFinder cf;

  @Before
  public void setUp() {
    mci = new MemoryCloneIndex();
    cf = JavaCloneFinder.build(mci);
  }

  @Test
  public void shouldFindDuplicateInFile() {
    initTestData(file1, file2);

    cf.addSourceFileForDetection(file1.getAbsolutePath());

    List<Clone> cloneList = cf.findClones();

    Clone expected = new Clone(5)
        .addPart(new ClonePart(file1.getAbsolutePath(), 3, 6, 11))
        .addPart(new ClonePart(file2.getAbsolutePath(), 9, 28, 33));
    assertThat(cloneList, hasItem(expected));
    assertThat(cloneList.size(), is(1));
  }

  @Test
  public void shouldFindDuplicateInDirectory() {
    initTestData(file1, file2, file3);

    cf.addSourceDirectoryForDetection(dir.getAbsolutePath());

    List<Clone> cloneList = cf.findClones();

    assertThat(cloneList.size(), is(4));

    Clone expected1 = new Clone(2)
        .addPart(new ClonePart(file1.getAbsolutePath(), 3, 6, 9))
        .addPart(new ClonePart(file3.getAbsolutePath(), 9, 28, 31));
    assertThat(cloneList, hasItem(expected1));

    Clone expected2 = new Clone(3)
        .addPart(new ClonePart(file2.getAbsolutePath(), 17, 33, 47))
        .addPart(new ClonePart(file3.getAbsolutePath(), 14, 31, 45));
    assertThat(cloneList, hasItem(expected2));

  }

  private void initTestData(File... files) {
    mci.removeAll();
    cf.register(files);
  }
}
