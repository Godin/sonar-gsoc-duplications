package org.sonar.duplications;

import org.junit.Before;
import org.junit.Test;
import org.sonar.duplications.algorithm.Clone;
import org.sonar.duplications.api.codeunit.Block;
import org.sonar.duplications.api.index.CloneIndexBackend;
import org.sonar.duplications.api.index.FileBlockGroup;
import org.sonar.duplications.api.index.GlobalCloneIndex;
import org.sonar.duplications.backend.MemoryIndexBackend;

import java.util.Set;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class GlobalCloneIndexTest {

  GlobalCloneIndex index;
  CloneIndexBackend backend;

  @Before
  public void initialize() {
    backend = new MemoryIndexBackend();
    index = new GlobalCloneIndex(backend);
  }

  @Test
  public void testRemove() {
    FileBlockGroup file = new FileBlockGroup("a");
    file.addBlock(new Block("a", "123", 1, 1, 7));
    index.addOrUpdateFileCloneIndex(file);
    assertThat(backend.size(), is(1));
    index.removeFileCloneIndex(file.getFileResourceId());
    assertThat(backend.size(), is(0));
  }

  @Test
  public void testUpdate() {
    FileBlockGroup file1 = new FileBlockGroup("a");
    file1.addBlock(new Block("a", "123", 1, 1, 7));
    index.addOrUpdateFileCloneIndex(file1);
    assertThat(backend.size(), is(1));

    FileBlockGroup file2 = new FileBlockGroup("a");
    file2.addBlock(new Block("a", "1234", 1, 1, 7));
    file2.addBlock(new Block("a", "12345", 2, 2, 8));
    index.addOrUpdateFileCloneIndex(file2);
    assertThat(backend.size(), is(2));
  }

  @Test
  public void testClonesWithoutDuplications() {
    FileBlockGroup fileA = new FileBlockGroup("a");
    fileA.addBlock(new Block("a", "0", 0, 0, 5));
    fileA.addBlock(new Block("a", "1", 1, 1, 6));
    fileA.addBlock(new Block("a", "2", 2, 2, 7));
    fileA.addBlock(new Block("a", "3", 3, 3, 8));
    fileA.addBlock(new Block("a", "4", 4, 4, 9));

    FileBlockGroup fileB = new FileBlockGroup("b");
    fileB.addBlock(new Block("b", "1", 1, 1, 6));
    fileB.addBlock(new Block("b", "2", 2, 2, 7));
    fileB.addBlock(new Block("b", "3", 3, 3, 8));

    FileBlockGroup fileC = new FileBlockGroup("c");
    fileC.addBlock(new Block("c", "1", 1, 1, 6));
    fileC.addBlock(new Block("c", "2", 2, 2, 7));
    fileC.addBlock(new Block("c", "3", 3, 3, 8));

    index.addOrUpdateFileCloneIndex(fileA);
    index.addOrUpdateFileCloneIndex(fileB);
    index.addOrUpdateFileCloneIndex(fileC);

    Set<Clone> items = index.getClones();
    assertThat(items, hasItem(new Clone("a", 1, 1, 8, "b", 1, 1, 8, 3)));
    assertThat(items, hasItem(new Clone("b", 1, 1, 8, "c", 1, 1, 8, 3)));
    assertThat(items, hasItem(new Clone("c", 1, 1, 8, "a", 1, 1, 8, 3)));

    assertThat(items.size(), is(3));
  }
}
