package org.sonar.duplications.algorithm;

import org.junit.Before;
import org.junit.Test;
import org.sonar.duplications.api.index.HashedStatementIndex;
import org.sonar.duplications.api.index.HashedTuple;
import org.sonar.duplications.backend.MemoryIndexBackend;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CloneReporterTest {

    HashedStatementIndex indexBackend;

    @Before
    public void initialize() {
        indexBackend = new MemoryIndexBackend();
    }

    @Test
    public void testSimple() {
        indexBackend.insert(new HashedTuple("a", 0, new byte[]{0}));
        indexBackend.insert(new HashedTuple("a", 1, new byte[]{1}));
        indexBackend.insert(new HashedTuple("a", 2, new byte[]{2}));
        indexBackend.insert(new HashedTuple("a", 3, new byte[]{3}));
        indexBackend.insert(new HashedTuple("a", 4, new byte[]{4}));
        indexBackend.insert(new HashedTuple("a", 5, new byte[]{5}));
        indexBackend.insert(new HashedTuple("a", 6, new byte[]{6}));
        indexBackend.insert(new HashedTuple("a", 7, new byte[]{7}));
        indexBackend.insert(new HashedTuple("a", 8, new byte[]{8}));

        indexBackend.insert(new HashedTuple("b", 2, new byte[]{3}));
        indexBackend.insert(new HashedTuple("b", 3, new byte[]{4}));
        indexBackend.insert(new HashedTuple("b", 4, new byte[]{5}));
        indexBackend.insert(new HashedTuple("b", 5, new byte[]{6}));

        indexBackend.insert(new HashedTuple("c", 1, new byte[]{5}));
        indexBackend.insert(new HashedTuple("c", 2, new byte[]{6}));
        indexBackend.insert(new HashedTuple("c", 3, new byte[]{7}));

        List<CloneItem> items = CloneReporter.reportClones("a", indexBackend);
        assertThat(items.size(), is(2));
        assertThat(items, hasItem(new CloneItem("a", 3, "b", 2, 4)));
        assertThat(items, hasItem(new CloneItem("a", 5, "c", 1, 3)));
    }

    @Test
    public void testSameClones() {
        indexBackend.insert(new HashedTuple("a", 0, new byte[]{0}));
        indexBackend.insert(new HashedTuple("a", 1, new byte[]{1}));
        indexBackend.insert(new HashedTuple("a", 2, new byte[]{2}));
        indexBackend.insert(new HashedTuple("a", 3, new byte[]{3}));
        indexBackend.insert(new HashedTuple("a", 4, new byte[]{4}));

        indexBackend.insert(new HashedTuple("b", 1, new byte[]{1}));
        indexBackend.insert(new HashedTuple("b", 2, new byte[]{2}));
        indexBackend.insert(new HashedTuple("b", 3, new byte[]{3}));

        indexBackend.insert(new HashedTuple("c", 1, new byte[]{1}));
        indexBackend.insert(new HashedTuple("c", 2, new byte[]{2}));
        indexBackend.insert(new HashedTuple("c", 3, new byte[]{3}));

        List<CloneItem> items = CloneReporter.reportClones("a", indexBackend);
        assertThat(items.size(), is(2));
        assertThat(items, hasItem(new CloneItem("a", 1, "b", 1, 3)));
        assertThat(items, hasItem(new CloneItem("a", 1, "c", 1, 3)));
    }

    @Test
    public void testBegin() {
        indexBackend.insert(new HashedTuple("a", 0, new byte[]{0}));
        indexBackend.insert(new HashedTuple("a", 1, new byte[]{1}));
        indexBackend.insert(new HashedTuple("a", 2, new byte[]{2}));

        indexBackend.insert(new HashedTuple("b", 0, new byte[]{0}));
        indexBackend.insert(new HashedTuple("b", 1, new byte[]{1}));

        List<CloneItem> items = CloneReporter.reportClones("a", indexBackend);
        assertThat(items.size(), is(1));
        assertThat(items, hasItem(new CloneItem("a", 0, "b", 0, 2)));
    }

    @Test
    public void testEnd() {
        indexBackend.insert(new HashedTuple("a", 0, new byte[]{0}));
        indexBackend.insert(new HashedTuple("a", 1, new byte[]{1}));
        indexBackend.insert(new HashedTuple("a", 2, new byte[]{2}));

        indexBackend.insert(new HashedTuple("b", 1, new byte[]{1}));
        indexBackend.insert(new HashedTuple("b", 2, new byte[]{2}));

        List<CloneItem> items = CloneReporter.reportClones("a", indexBackend);
        assertThat(items.size(), is(1));
        assertThat(items, hasItem(new CloneItem("a", 1, "b", 1, 2)));
    }
}
