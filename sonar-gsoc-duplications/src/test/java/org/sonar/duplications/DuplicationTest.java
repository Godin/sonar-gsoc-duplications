package org.sonar.duplications;

import org.junit.Before;
import org.junit.Test;
import org.sonar.duplications.algorithm.CloneReporterAlgorithmBuilder;
import org.sonar.duplications.block.FileBlockGroup;
import org.sonar.duplications.index.CloneGroup;
import org.sonar.duplications.index.ClonePart;
import org.sonar.duplications.index.MemoryCloneIndex;
import org.sonar.duplications.java.JavaCloneFinder;

import java.io.File;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DuplicationTest extends BaseCloneReporterTest {

  private File file1 = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest/CPDFile1.java");
  private File file2 = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest/CPDFile2.java");
  private File file3 = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest/CPDFile3.java");
  private File dir = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest/");

  private MemoryCloneIndex mci;
  private CloneFinder cf;

  public DuplicationTest(CloneReporterAlgorithmBuilder builder) {
    super(builder);
  }

  @Before
  public void setUp() {
    mci = new MemoryCloneIndex();
    cf = JavaCloneFinder.build(mci, 5, cloneReporterBuilder.build(mci));
  }

  @Test
  public void shouldFindDuplicateInFile() {
    initTestData();

    FileBlockGroup fileBlockGroup1 = cf.tokenize(file1);
    cf.register(fileBlockGroup1);
    cf.register(file2);

    List<CloneGroup> cloneList1 = cf.findClones(fileBlockGroup1);

    ClonePart part1 = new ClonePart(file1.getAbsolutePath(), 3, 4, 12);
    ClonePart part2 = new ClonePart(file2.getAbsolutePath(), 9, 25, 33);
    CloneGroup expected = new CloneGroup(5)
        .addPart(part1)
        .addPart(part2);
    expected.setOriginPart(part1);
    assertThat(cloneList1, hasItem(expected));
    assertThat(cloneList1.size(), is(1));
  }

  @Test
  public void shouldFindDuplicateInDirectory() {
    initTestData();

    FileBlockGroup fileBlockGroup1 = cf.tokenize(file1);
    FileBlockGroup fileBlockGroup2 = cf.tokenize(file2);
    cf.register(fileBlockGroup1);
    cf.register(fileBlockGroup2);
    cf.register(file3);

    List<CloneGroup> cloneList1 = cf.findClones(fileBlockGroup1);
    List<CloneGroup> cloneList2 = cf.findClones(fileBlockGroup2);

    ClonePart part11 = new ClonePart(file1.getAbsolutePath(), 3, 4, 9);
    ClonePart part12 = new ClonePart(file3.getAbsolutePath(), 9, 25, 30);
    CloneGroup expected1 = new CloneGroup(2)
        .addPart(part11)
        .addPart(part12);
    expected1.setOriginPart(part11);
    assertThat(cloneList1, hasItem(expected1));

    ClonePart part21 = new ClonePart(file2.getAbsolutePath(), 17, 33, 47);
    ClonePart part22 = new ClonePart(file3.getAbsolutePath(), 14, 30, 44);
    CloneGroup expected2 = new CloneGroup(3)
        .addPart(part21)
        .addPart(part22);
    expected2.setOriginPart(part21);
    assertThat(cloneList2, hasItem(expected2));
  }

  private void initTestData(File... files) {
    mci.removeAll();
    for (File file : files)
      cf.register(file);
  }
}
