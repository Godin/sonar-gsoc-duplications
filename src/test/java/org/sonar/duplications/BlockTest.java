package org.sonar.duplications;


import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.sonar.duplications.api.codeunit.Block;

public class BlockTest {

  @Test
  public void fieldsTest() {
    String fileName = "someFile";
    int statementIndex = 4;
    String hash = "123456";
    Block tuple = new Block(fileName, hash, statementIndex, 0, 10);
    assertThat(tuple.getResourceId(), equalTo(fileName));
    assertThat(tuple.getIndexInFile(), equalTo(statementIndex));
    assertEquals(tuple.getBlockHash(), hash);
  }

  @Test
  public void tupleEqualsTest() {
    Block tuple1 = new Block("somefile", "abc123", 1, 1, 10);
    Block tuple2 = new Block("somefile", "abc123", 1, 1, 10);
    Block tupleArr = new Block("somefile", "xyz", 1, 1, 10);
    Block tupleIndex = new Block("somefile", "abc123", 2, 1, 10);
    Block tupleName = new Block("other", "abc123", 1, 1, 10);

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
    String[] arrays = {"123", "321"};

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
