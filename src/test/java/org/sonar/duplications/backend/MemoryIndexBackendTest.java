package org.sonar.duplications.backend;

import org.junit.Before;
import org.junit.Test;
import org.sonar.duplications.api.index.HashedTuple;

import java.util.SortedSet;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class MemoryIndexBackendTest {

    MemoryIndexBackend memBack;

    @Before
    public void initialize() {
        memBack = new MemoryIndexBackend();
    }

    @Test
    public void testClearAll() {
        assertThat(memBack.size(), is(0));
        for (int i = 0; i < 10; i++) {
            memBack.insert(new HashedTuple("a", i, new byte[]{0}));
        }
        assertThat(memBack.size(), is(10));

        memBack.removeAll();
        assertThat(memBack.size(), is(0));
    }

    @Test
    public void byFileName() {
        HashedTuple tuple1 = new HashedTuple("a", 0, new byte[]{0});
        HashedTuple tuple2 = new HashedTuple("a", 1, new byte[]{0});

        assertThat(memBack.getByFilename("a").size(), is(0));

        memBack.insert(tuple1);
        assertThat(memBack.getByFilename("a").size(), is(1));

        memBack.insert(tuple2);
        assertThat(memBack.getByFilename("a").size(), is(2));
    }

    @Test
    public void bySequenceHash() {
        HashedTuple tuple1 = new HashedTuple("a", 0, new byte[]{0});
        HashedTuple tuple2 = new HashedTuple("a", 1, new byte[]{0});

        assertThat(memBack.getBySequenceHash(new byte[]{0}).size(), is(0));

        memBack.insert(tuple1);
        assertThat(memBack.getBySequenceHash(new byte[]{0}).size(), is(1));

        memBack.insert(tuple2);
        assertThat(memBack.getBySequenceHash(new byte[]{0}).size(), is(2));
    }

    @Test
    public void insertSame() {
        HashedTuple tuple = new HashedTuple("a", 0, new byte[]{0});
        HashedTuple tupleSame = new HashedTuple("a", 0, new byte[]{0});

        assertThat(memBack.getByFilename("a").size(), is(0));
        assertThat(memBack.getBySequenceHash(new byte[]{0}).size(), is(0));

        memBack.insert(tuple);
        assertThat(memBack.getByFilename("a").size(), is(1));
        assertThat(memBack.getBySequenceHash(new byte[]{0}).size(), is(1));

        memBack.insert(tupleSame);
        assertThat(memBack.getByFilename("a").size(), is(1));
        assertThat(memBack.getBySequenceHash(new byte[]{0}).size(), is(1));
    }

    @Test
    public void testSorted() {
        for (int i = 0; i < 10; i++) {
            memBack.insert(new HashedTuple("a", 10 - i, new byte[]{1}));
        }
        assertThat(memBack.getByFilename("a").size(), is(10));
        assertThat(memBack.getBySequenceHash(new byte[]{1}).size(), is(10));

        SortedSet<HashedTuple> set = memBack.getByFilename("a");
        int prevStatementIndex = 0;
        for (HashedTuple tuple : set) {
            assertTrue(tuple.getStatementIndex() > prevStatementIndex);
            prevStatementIndex = tuple.getStatementIndex();
        }
    }
}
