package org.sonar.duplications;


import org.junit.Test;
import org.sonar.duplications.api.index.HashedTuple;

import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class HashedTupleTest {

  @Test
  public void fieldsTest() {
    String file = "someFile";
    int statementIndex = 4;
    byte[] hash = {1, 2, 3, 4, 5, 6};
    HashedTuple tuple = new HashedTuple(file, statementIndex, hash);
    assertThat(tuple.getFileName(), equalTo(file));
    assertThat(tuple.getStatementIndex(), equalTo(statementIndex));
    assertTrue(Arrays.equals(tuple.getSequenceHash(), hash));
  }

  @Test
  public void tupleEqualsTest() {
    HashedTuple tuple1 = new HashedTuple("somefile", 1, new byte[]{10, 126, -15});
    HashedTuple tuple2 = new HashedTuple("somefile", 1, new byte[]{10, 126, -15});
    HashedTuple tupleArr = new HashedTuple("somefile", 1, new byte[]{17});
    HashedTuple tupleIndex = new HashedTuple("somefile", 2, new byte[]{10, 126, -15});
    HashedTuple tupleName = new HashedTuple("other", 1, new byte[]{10, 126, -15});

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
    int[] indexes = {1, 2};
    byte[][] arrays = {new byte[]{1, 2, 3}, new byte[]{3, 2, 1}};

    //fileName is in hashCode()
    int defaultTupleHashCode = new HashedTuple(files[0], indexes[0], arrays[0]).hashCode();
    int fileNameTupleHashCode = new HashedTuple(files[1], indexes[0], arrays[0]).hashCode();
    assertThat(defaultTupleHashCode, not(equalTo(fileNameTupleHashCode)));

    //statementIndex is in hashCode()
    int indexTupleHashCode = new HashedTuple(files[0], indexes[1], arrays[0]).hashCode();
    assertThat(defaultTupleHashCode, not(equalTo(indexTupleHashCode)));

    //sequenceHash is in hashCode()
    int sequenceHashTupleHashCode = new HashedTuple(files[0], indexes[0], arrays[1]).hashCode();
    assertThat(defaultTupleHashCode, not(equalTo(sequenceHashTupleHashCode)));
  }
}
