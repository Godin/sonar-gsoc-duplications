package org.sonar.duplications.index;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

public class DataUtilsTest {

  @Test
  public void testSort() {
    int[] expected = new int[200];
    int[] got = new int[expected.length];
    for (int i = 0; i < expected.length; i++) {
      expected[i] = (int) (Math.random() * 900);
      got[i] = expected[i];
    }
    Arrays.sort(expected, 0, expected.length - 1);
    DataUtils.sort(new SimpleSortable(got));
    assertTrue(Arrays.equals(expected, got));
  }

  @Test
  public void testSearch() {
    int[] a = new int[] { 1, 2, 3, 3, 4, 3 };
    assertThat(DataUtils.binarySearch(new SimpleSortable(a), a.length - 1), is(2));
  }

  class SimpleSortable implements DataUtils.Sortable {
    private final int[] a;

    public SimpleSortable(int[] a) {
      this.a = a;
    }

    public int size() {
      return a.length - 1;
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
