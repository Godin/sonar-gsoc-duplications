package org.sonar.duplications;

import org.junit.Test;
import org.sonar.duplications.index.Clone;
import org.sonar.duplications.index.ClonePart;
import org.sonar.duplications.index.MemoryCloneIndex;
import org.sonar.duplications.java.JavaCloneFinder;

import java.io.File;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class CloneGroupDuplicationTest {

  MemoryCloneIndex mci = new MemoryCloneIndex();
  File file1 = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest/CPDFile1.java");
  File file2 = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest/CPDFile2.java");
  File file3 = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest/CPDFile3.java");
  File dir = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest/");

  //for bigger clone
  File file21 = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest2/CPDFile21.java");
  File file22 = DuplicationsTestUtil.findFile("/org/sonar/duplications/cpd/CPDTest2/CPDFile22.java");


  CloneFinder cf = JavaCloneFinder.build(mci);

  @Test
  public void shouldFindDuplicateInFile() {

    initTestData(file1, file2);

    cf.addSourceFileForDetection(file1.getAbsolutePath());

    List<Clone> cloneList = cf.findClones();

    ClonePart part1 = new ClonePart(file1.getAbsolutePath(), 3, 6, 11);
    ClonePart part2 = new ClonePart(file2.getAbsolutePath(), 9, 28, 33);
    Clone expected = new Clone(5)
        .addPart(part1)
        .addPart(part2);
    expected.setOriginPart(part1);

    assertThat(cloneList, hasItem(expected));
    assertThat(cloneList.size(), is(1));
  }

  @Test
  public void shouldFindTriplicateInFile() {

    initTestData(file1, file2, file3);

    cf.clearSourceFilesForDetection();
    cf.addSourceFileForDetection(file1.getAbsolutePath());
    List<Clone> cloneList1 = cf.findClones();

    assertThat(cloneList1.size(), is(2));

    ClonePart part11 = new ClonePart(file1.getAbsolutePath(), 3, 6, 9);
    ClonePart part12 = new ClonePart(file2.getAbsolutePath(), 9, 28, 31);
    ClonePart part13 = new ClonePart(file3.getAbsolutePath(), 9, 28, 31);
    Clone expected1 = new Clone(2)
        .addPart(part11)
            //TODO discuss and fix for CloneGroups
            //.addPart(part12)
        .addPart(part13);
    expected1.setOriginPart(part11);

    assertThat(cloneList1, hasItem(expected1));

    //and a bigger clone between file1 and file2 as like the previous test case
    ClonePart part21 = new ClonePart(file1.getAbsolutePath(), 3, 6, 11);
    ClonePart part22 = new ClonePart(file2.getAbsolutePath(), 9, 28, 33);
    Clone expected2 = new Clone(5)
        .addPart(part21)
        .addPart(part22);
    expected2.setOriginPart(part21);

    assertThat(cloneList1, hasItem(expected2));
  }

  @Test
  public void shouldFindDuplicateInFileWithBiggerClone() {

    //separate source file contains bigger clone by adding more similar code in existing clone
    //now it should report only one clone but bigger in size
    initTestData(file21, file22);

    cf.addSourceFileForDetection(file21.getAbsolutePath());

    List<Clone> cloneList = cf.findClones();

    ClonePart part1 = new ClonePart(file21.getAbsolutePath(), 3, 6, 18);
    ClonePart part2 = new ClonePart(file22.getAbsolutePath(), 9, 28, 40);

    Clone expected = new Clone(12)
        .addPart(part1)
        .addPart(part2);
    expected.setOriginPart(part1);

    assertThat(cloneList, hasItem(expected));
    assertThat(cloneList.size(), is(1));
  }

  @Test
  public void shouldFindDuplicateInDirectory() {

    initTestData(file1, file2, file3);

    cf.addSourceDirectoryForDetection(dir.getAbsolutePath());

    List<Clone> cloneList = cf.findClones();

    Clone expected1 = new Clone(2)
        .addPart(new ClonePart(file1.getAbsolutePath(), 3, 6, 9))
        .addPart(new ClonePart(file3.getAbsolutePath(), 9, 28, 31));
    expected1.setOriginPart(new ClonePart(file1.getAbsolutePath(), 3, 6, 9));

    assertThat(cloneList, hasItem(expected1));

    Clone expected2 = new Clone(5)
        .addPart(new ClonePart(file1.getAbsolutePath(), 3, 6, 11))
        .addPart(new ClonePart(file2.getAbsolutePath(), 9, 28, 33));
    expected2.setOriginPart(new ClonePart(file1.getAbsolutePath(), 3, 6, 11));

    assertThat(cloneList, hasItem(expected2));

    Clone expected3 = new Clone(11)
        .addPart(new ClonePart(file2.getAbsolutePath(), 0, 13, 31))
        .addPart(new ClonePart(file3.getAbsolutePath(), 0, 13, 31));
    expected3.setOriginPart(new ClonePart(file2.getAbsolutePath(), 0, 13, 31));

    assertThat(cloneList, hasItem(expected3));

    Clone expected4 = new Clone(3)
        .addPart(new ClonePart(file2.getAbsolutePath(), 17, 33, 47))
        .addPart(new ClonePart(file3.getAbsolutePath(), 14, 31, 45));
    expected4.setOriginPart(new ClonePart(file2.getAbsolutePath(), 17, 33, 47));

    assertThat(cloneList, hasItem(expected4));
  }

  @Test
  public void shouldReportCloneWithSmallBlockSize() {

    //find clone with smaller block size
    cf = JavaCloneFinder.build(mci, 4);

    initTestData(file1, file2, file3);

    cf.addSourceDirectoryForDetection(dir.getAbsolutePath());

    List<Clone> cloneList = cf.findClones();

    ClonePart part1 = new ClonePart(file1.getAbsolutePath(), 12, 15, 18);
    ClonePart part2 = new ClonePart(file3.getAbsolutePath(), 21, 49, 52);
    Clone expectedSmallClone = new Clone(1)
        .addPart(part1)
        .addPart(part2);
    expectedSmallClone.setOriginPart(part1);

    assertThat(cloneList, hasItem(expectedSmallClone));
  }

  @Test
  public void shouldNotReportCloneSmallerThanBlockSize() {

    //find clone with minimum block size 5
    cf = JavaCloneFinder.build(mci, 5);

    initTestData(file1, file2, file3);

    cf.addSourceDirectoryForDetection(dir.getAbsolutePath());

    List<Clone> cloneList = cf.findClones();

    ClonePart part1 = new ClonePart(file1.getAbsolutePath(), 12, 15, 18);
    ClonePart part2 = new ClonePart(file3.getAbsolutePath(), 21, 49, 52);
    Clone expectedSmallClone = new Clone(2)
        .addPart(part1)
        .addPart(part2);
    expectedSmallClone.setOriginPart(part1);

    assertThat(cloneList, not(hasItem(expectedSmallClone)));
  }

  private void initTestData(File... files) {
    mci.removeAll();
    cf.register(files);
  }
}
