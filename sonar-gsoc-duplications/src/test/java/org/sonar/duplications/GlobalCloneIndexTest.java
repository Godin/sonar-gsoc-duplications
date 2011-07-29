package org.sonar.duplications;

import org.junit.Before;
import org.junit.Test;
import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.FileCloneIndex;
import org.sonar.duplications.index.*;

import java.util.Set;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class GlobalCloneIndexTest {

  GlobalCloneIndex index;
  CloneIndex backend;

  @Before
  public void initialize() {
    backend = new MemoryCloneIndex();
    index = new GlobalCloneIndex(backend);
  }

  @Test
  public void testRemove() {
    FileCloneIndex file = new FileCloneIndex("a");
    file.addBlock(new Block("a", "123", 1, 1, 7));
    index.addOrUpdateFileCloneIndex(file);
    assertThat(backend.size(), is(1));
    index.removeFileCloneIndex(file.getFileResourceId());
    assertThat(backend.size(), is(0));
  }

  @Test
  public void testUpdate() {
    FileCloneIndex file1 = new FileCloneIndex("a");
    file1.addBlock(new Block("a", "123", 1, 1, 7));
    index.addOrUpdateFileCloneIndex(file1);
    assertThat(backend.size(), is(1));

    FileCloneIndex file2 = new FileCloneIndex("a");
    file2.addBlock(new Block("a", "1234", 1, 1, 7));
    file2.addBlock(new Block("a", "12345", 2, 2, 8));
    index.addOrUpdateFileCloneIndex(file2);
    assertThat(backend.size(), is(2));
  }

  @Test
  public void testClonesWithoutDuplications() {
    FileCloneIndex fileA = new FileCloneIndex("a");
    fileA.addBlock(new Block("a", "0", 0, 0, 5));
    fileA.addBlock(new Block("a", "1", 1, 1, 6));
    fileA.addBlock(new Block("a", "2", 2, 2, 7));
    fileA.addBlock(new Block("a", "3", 3, 3, 8));
    fileA.addBlock(new Block("a", "4", 4, 4, 9));

    FileCloneIndex fileB = new FileCloneIndex("b");
    fileB.addBlock(new Block("b", "1", 1, 1, 6));
    fileB.addBlock(new Block("b", "2", 2, 2, 7));
    fileB.addBlock(new Block("b", "3", 3, 3, 8));

    FileCloneIndex fileC = new FileCloneIndex("c");
    fileC.addBlock(new Block("c", "2", 1, 1, 6));
    fileC.addBlock(new Block("c", "3", 2, 2, 7));
    fileC.addBlock(new Block("c", "4", 3, 3, 8));

    index.addOrUpdateFileCloneIndex(fileA);
    index.addOrUpdateFileCloneIndex(fileB);
    index.addOrUpdateFileCloneIndex(fileC);

    Set<Clone> items = index.getClones();

    ClonePart part11 = new ClonePart("a", 1, 1, 8);
    ClonePart part12 = new ClonePart("b", 1, 1, 8);
    Clone expected1 = new Clone(3)
        .addPart(part11)
        .addPart(part12);
    expected1.setOriginPart(part11);

    assertThat(items, hasItem(expected1));

    ClonePart part21 = new ClonePart("a", 2, 2, 9);
    ClonePart part22 = new ClonePart("c", 1, 1, 8);
    Clone expected2 = new Clone(3)
        .addPart(part21)
        .addPart(part22);
    expected2.setOriginPart(part21);

    assertThat(items, hasItem(expected2));
  }
}
