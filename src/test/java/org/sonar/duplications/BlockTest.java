package org.sonar.duplications;


import org.junit.Test;
import org.sonar.duplications.api.codeunit.block.Block;

import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class BlockTest {

  @Test
  public void fieldsTest() {
    String fileName = "someFile";
    int statementIndex = 4;
    byte[] hash = {1, 2, 3, 4, 5, 6};
    Block tuple = new Block(fileName, hash, statementIndex, 0, 10);
    assertThat(tuple.getResourceId(), equalTo(fileName));
    assertThat(tuple.getFirstUnitIndex(), equalTo(statementIndex));
    assertTrue(Arrays.equals(tuple.getBlockHash(), hash));
  }

  @Test
  public void tupleEqualsTest() {
    Block tuple1 = new Block("somefile", new byte[]{10, 126, -15}, 1, 1, 10);
    Block tuple2 = new Block("somefile", new byte[]{10, 126, -15}, 1, 1, 10);
    Block tupleArr = new Block("somefile", new byte[]{17}, 1, 1, 10);
    Block tupleIndex = new Block("somefile", new byte[]{10, 126, -15}, 2, 1, 10);
    Block tupleName = new Block("other", new byte[]{10, 126, -15}, 1, 1, 10);

    assertTrue(tuple1.equals(tuple2));
    assertThat(tuple1.toString(), is(tuple2.toString()));

    assertFalse(tuple1.equals(tupleArr));
    assertThat(tuple1.toString(), not(equalTo(tupleArr.toString())));

    assertFalse(tuple1.equals(tupleIndex));
    assertThat(tuple1.toString(), not(equalTo(tupleIndex.toString())));

    assertFalse(tuple1.equals(tupleName));
    assertThat(tuple1.toString(), not(equalTo(tupleName.toString())));
  }

  @Test
  public void hashCodeTest() {
    String[] files = {"file1", "file2"};
    int[] unitIndexes = {1, 2};
    byte[][] arrays = {new byte[]{1, 2, 3}, new byte[]{3, 2, 1}};

    //fileName is in hashCode()
    int defaultTupleHashCode = new Block(files[0], arrays[0], unitIndexes[0], 1, 10).hashCode();
    int fileNameTupleHashCode = new Block(files[1], arrays[0], unitIndexes[0], 1, 10).hashCode();
    assertThat(defaultTupleHashCode, not(equalTo(fileNameTupleHashCode)));

    //statementIndex is in hashCode()
    int indexTupleHashCode = new Block(files[0], arrays[0], unitIndexes[1], 1, 10).hashCode();
    assertThat(defaultTupleHashCode, not(equalTo(indexTupleHashCode)));

    //sequenceHash is in hashCode()
    int sequenceHashTupleHashCode = new Block(files[0], arrays[1], unitIndexes[0], 1, 10).hashCode();
    assertThat(defaultTupleHashCode, not(equalTo(sequenceHashTupleHashCode)));
  }
}
