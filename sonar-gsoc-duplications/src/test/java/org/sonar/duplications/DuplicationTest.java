package org.sonar.duplications;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.sonar.duplications.index.Clone;
import org.sonar.duplications.index.ClonePart;
import org.sonar.duplications.index.MemoryCloneIndex;
import org.sonar.duplications.java.JavaCloneFinder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    Clone expected = new Clone(3)
        .addPart(new ClonePart(file1.getAbsolutePath(), 3, 6, 11))
        .addPart(new ClonePart(file2.getAbsolutePath(), 18, 28, 33));
    assertThat(cloneList, hasItem(expected));
    assertThat(cloneList.size(), is(1));
  }

  @Test
  public void shouldFindDuplicateInDirectory() {
    initTestData(file1, file2, file3);

    cf.addSourceDirectoryForDetection(dir.getAbsolutePath());

    List<Clone> rawCloneList = cf.findClones();
    List<Clone> cloneList = new ArrayList<Clone>();
    Set<Clone> cloneSet = new HashSet<Clone>();
    for (Clone clone : rawCloneList) {
      if (!cloneSet.contains(clone)) {
        cloneSet.add(clone);
        cloneList.add(clone);
      }
    }

    Clone expected1 = new Clone(1)
        .addPart(new ClonePart(file1.getAbsolutePath(), 0, 6, 8))
        .addPart(new ClonePart(file2.getAbsolutePath(), 2, 28, 30))
        .addPart(new ClonePart(file3.getAbsolutePath(), 4, 28, 30));
    assertThat(cloneList, hasItem(expected1));

    Clone expected2 = new Clone(1)
        .addPart(new ClonePart(file1.getAbsolutePath(), 3, 6, 9))
        .addPart(new ClonePart(file3.getAbsolutePath(), 39, 28, 31));
    assertThat(cloneList, hasItem(expected2));

    Clone expected3 = new Clone(3)
        .addPart(new ClonePart(file2.getAbsolutePath(), 23, 33, 47))
        .addPart(new ClonePart(file3.getAbsolutePath(), 42, 31, 45));
    assertThat(cloneList, hasItem(expected3));

    assertThat(cloneList.size(), is(7));
  }

  private void initTestData(File... files) {
    mci.removeAll();
    cf.register(files);
  }
}
