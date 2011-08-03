package org.sonar.duplications;

import org.junit.Test;
import org.sonar.duplications.block.FileBlockGroup;
import org.sonar.duplications.index.CloneGroup;
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

    initTestData();

    FileBlockGroup fileBlockGroup = cf.tokenize(file1);
    cf.register(fileBlockGroup);
    cf.register(file2);
    List<CloneGroup> cloneList = cf.findClones(fileBlockGroup);

    ClonePart part1 = new ClonePart(file1.getAbsolutePath(), 3, 4, 12);
    ClonePart part2 = new ClonePart(file2.getAbsolutePath(), 9, 25, 33);
    CloneGroup expected = new CloneGroup(5)
        .addPart(part1)
        .addPart(part2);
    expected.setOriginPart(part1);

    assertThat(cloneList, hasItem(expected));
    assertThat(cloneList.size(), is(1));
  }

  @Test
  public void shouldFindTriplicateInFile() {

    initTestData();

    FileBlockGroup fileBlockGroup = cf.tokenize(file1);
    cf.register(fileBlockGroup);
    cf.register(file2);
    cf.register(file3);
    List<CloneGroup> cloneList = cf.findClones(fileBlockGroup);

    assertThat(cloneList.size(), is(2));

    ClonePart part11 = new ClonePart(file1.getAbsolutePath(), 3, 4, 9);
    ClonePart part12 = new ClonePart(file2.getAbsolutePath(), 9, 25, 33);
    ClonePart part13 = new ClonePart(file3.getAbsolutePath(), 9, 25, 30);
    CloneGroup expected1 = new CloneGroup(2)
        .addPart(part11)
            //TODO discuss and fix for CloneGroups
            //.addPart(part12)
        .addPart(part13);
    expected1.setOriginPart(part11);

    assertThat(cloneList, hasItem(expected1));

    //and a bigger clone between file1 and file2 as like the previous test case
    ClonePart part21 = new ClonePart(file1.getAbsolutePath(), 3, 4, 12);
    ClonePart part22 = new ClonePart(file2.getAbsolutePath(), 9, 25, 33);
    CloneGroup expected2 = new CloneGroup(5)
        .addPart(part21)
        .addPart(part22);
    expected2.setOriginPart(part21);

    assertThat(cloneList, hasItem(expected2));
  }

  @Test
  public void shouldFindDuplicateInFileWithBiggerClone() {
    //separate source file contains bigger clone by adding more similar code in existing clone
    //now it should report only one clone but bigger in size
    initTestData();

    FileBlockGroup fileBlockGroup21 = cf.tokenize(file21);
    cf.register(fileBlockGroup21);
    cf.register(file22);
    List<CloneGroup> cloneList21 = cf.findClones(fileBlockGroup21);

    ClonePart part21 = new ClonePart(file21.getAbsolutePath(), 3, 4, 19);
    ClonePart part22 = new ClonePart(file22.getAbsolutePath(), 9, 25, 40);

    CloneGroup expected = new CloneGroup(12)
        .addPart(part21)
        .addPart(part22);
    expected.setOriginPart(part21);

    assertThat(cloneList21, hasItem(expected));
    assertThat(cloneList21.size(), is(1));
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

    CloneGroup expected1 = new CloneGroup(2)
        .addPart(new ClonePart(file1.getAbsolutePath(), 3, 4, 9))
        .addPart(new ClonePart(file3.getAbsolutePath(), 9, 25, 30));
    expected1.setOriginPart(new ClonePart(file1.getAbsolutePath(), 3, 4, 9));

    assertThat(cloneList1, hasItem(expected1));

    CloneGroup expected2 = new CloneGroup(5)
        .addPart(new ClonePart(file1.getAbsolutePath(), 3, 4, 12))
        .addPart(new ClonePart(file2.getAbsolutePath(), 9, 25, 33));
    expected2.setOriginPart(new ClonePart(file1.getAbsolutePath(), 3, 4, 12));

    assertThat(cloneList1, hasItem(expected2));

    CloneGroup expected3 = new CloneGroup(11)
        .addPart(new ClonePart(file2.getAbsolutePath(), 0, 10, 30))
        .addPart(new ClonePart(file3.getAbsolutePath(), 0, 10, 30));
    expected3.setOriginPart(new ClonePart(file2.getAbsolutePath(), 0, 10, 30));

    assertThat(cloneList2, hasItem(expected3));

    CloneGroup expected4 = new CloneGroup(3)
        .addPart(new ClonePart(file2.getAbsolutePath(), 17, 33, 47))
        .addPart(new ClonePart(file3.getAbsolutePath(), 14, 30, 44));
    expected4.setOriginPart(new ClonePart(file2.getAbsolutePath(), 17, 33, 47));

    assertThat(cloneList2, hasItem(expected4));
  }

  @Test
  public void shouldReportCloneWithSmallBlockSize() {
    //find clone with smaller block size
    cf = JavaCloneFinder.build(mci, 4);

    initTestData();

    FileBlockGroup fileBlockGroup1 = cf.tokenize(file1);
    cf.register(fileBlockGroup1);
    cf.register(file2);
    cf.register(file3);

    List<CloneGroup> cloneList1 = cf.findClones(fileBlockGroup1);

    ClonePart part1 = new ClonePart(file1.getAbsolutePath(), 12, 16, 19);
    ClonePart part2 = new ClonePart(file3.getAbsolutePath(), 21, 48, 51);
    CloneGroup expectedSmallClone = new CloneGroup(1)
        .addPart(part1)
        .addPart(part2);
    expectedSmallClone.setOriginPart(part1);

    assertThat(cloneList1, hasItem(expectedSmallClone));
  }

  @Test
  public void shouldNotReportCloneSmallerThanBlockSize() {
    //find clone with minimum block size 5
    cf = JavaCloneFinder.build(mci, 5);

    initTestData();

    FileBlockGroup fileBlockGroup1 = cf.tokenize(file1);
    cf.register(fileBlockGroup1);
    cf.register(file2);
    cf.register(file3);

    List<CloneGroup> cloneList1 = cf.findClones(fileBlockGroup1);

    ClonePart part1 = new ClonePart(file1.getAbsolutePath(), 12, 15, 18);
    ClonePart part2 = new ClonePart(file3.getAbsolutePath(), 21, 49, 52);
    CloneGroup expectedSmallClone = new CloneGroup(2)
        .addPart(part1)
        .addPart(part2);
    expectedSmallClone.setOriginPart(part1);

    assertThat(cloneList1, not(hasItem(expectedSmallClone)));
  }

  private void initTestData(File... files) {
    mci.removeAll();
    for (File file : files)
      cf.register(file);
  }
}
