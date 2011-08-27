package org.sonar.duplications.index;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;

public class DataUtilsTest {

  @Test
  public void testByteToIntArray() {
    // number of bytes is enough to create exactly one int (4 bytes)
    int[] result = DataUtils.byteToIntArray(new byte[] { 0x04, 0x12, 0x19, (byte) 0x86 });
    assertThat(result, is(new int[] { 0x04121986 }));
    // number of bytes is more than 4, but less than 8, so anyway 2 ints
    result = DataUtils.byteToIntArray(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x31 });
    assertThat(result, is(new int[] { 0x00000000, 0x31000000 }));
  }

  @Test
  public void testIntToByteArray() {
    byte[] result = DataUtils.intToByteArray(new int[] { 0x04121986 });
    assertThat(result, is(new byte[] { 0x04, 0x12, 0x19, (byte) 0x86 }));
  }

  @Test
  public void testSort() {
    int[] expected = new int[200];
    int[] actual = new int[expected.length];
    for (int i = 0; i < expected.length; i++) {
      expected[i] = (int) (Math.random() * 900);
      actual[i] = expected[i];
    }
    Arrays.sort(expected);
    DataUtils.sort(new SimpleSortable(actual, actual.length));
    assertThat(actual, equalTo(expected));
  }

  @Test
  public void testSearch() {
    int[] a = new int[] { 1, 2, 4, 4, 4, 5, 0 };
    SimpleSortable sortable = new SimpleSortable(a, a.length - 1);
    // search 4
    a[a.length - 1] = 4;
    assertThat(DataUtils.binarySearch(sortable), is(2));
    // search 5
    a[a.length - 1] = 5;
    assertThat(DataUtils.binarySearch(sortable), is(5));
    // search -5
    a[a.length - 1] = -5;
    assertThat(DataUtils.binarySearch(sortable), is(0));
    // search 10
    a[a.length - 1] = 10;
    assertThat(DataUtils.binarySearch(sortable), is(6));
    // search 3
    a[a.length - 1] = 3;
    assertThat(DataUtils.binarySearch(sortable), is(2));
  }

  class SimpleSortable implements DataUtils.Sortable {
    private final int[] a;
    private final int size;

    public SimpleSortable(int[] a, int size) {
      this.a = a;
      this.size = size;
    }

    public int size() {
      return size;
    }

    public void swap(int i, int j) {
      int tmp = a[i];
      a[i] = a[j];
      a[j] = tmp;
    }

    public boolean isLess(int i, int j) {
      return a[i] < a[j];
    }
  }

}
