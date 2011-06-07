package org.sonar.duplications;

import org.junit.Before;
import org.junit.Test;
import org.sonar.duplications.api.codeunit.block.Block;
import org.sonar.duplications.api.index.CloneIndexBackend;
import org.sonar.duplications.api.index.FileBlockGroup;
import org.sonar.duplications.api.index.GlobalCloneIndex;
import org.sonar.duplications.backend.MemoryIndexBackend;

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
    file.addBlock(new Block("a", new byte[]{3}, 1, 1, 7));
    index.addOrUpdateFileCloneIndex(file);
    assertThat(backend.size(), is(1));
    index.removeFileCloneIndex(file.getFileResourceId());
    assertThat(backend.size(), is(0));
  }

  @Test
  public void testUpdate() {
    FileBlockGroup file1 = new FileBlockGroup("a");
    file1.addBlock(new Block("a", new byte[]{3}, 1, 1, 7));
    index.addOrUpdateFileCloneIndex(file1);
    assertThat(backend.size(), is(1));

    FileBlockGroup file2 = new FileBlockGroup("a");
    file2.addBlock(new Block("a", new byte[]{4}, 1, 1, 7));
    file2.addBlock(new Block("a", new byte[]{5}, 2, 2, 8));
    index.addOrUpdateFileCloneIndex(file2);
    assertThat(backend.size(), is(2));
  }
}
