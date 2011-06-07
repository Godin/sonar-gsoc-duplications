package org.sonar.duplications;

import org.junit.Test;
import org.sonar.duplications.api.CloneIndexException;
import org.sonar.duplications.api.codeunit.block.Block;
import org.sonar.duplications.api.index.FileBlockGroup;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FileBlockGroupTest {
  @Test(expected = CloneIndexException.class)
  public void testWrongResourceId() {
    FileBlockGroup file = new FileBlockGroup("a");
    file.addBlock(new Block("b", new byte[]{3}, 1, 1, 7));
  }

  @Test
  public void testDuplicates() {
    FileBlockGroup file = new FileBlockGroup("a");
    file.addBlock(new Block("a", new byte[]{3}, 1, 1, 7));
    file.addBlock(new Block("a", new byte[]{3}, 1, 1, 7));
    assertThat(file.getAllBlocks().size(), is(1));
  }
}
