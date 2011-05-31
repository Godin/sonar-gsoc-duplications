/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2011 SonarSource
 * Written (W) 2011 Andrew Tereskin
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.duplications.backend;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.TreeMultimap;
import org.sonar.duplications.api.index.HashedStatementIndex;
import org.sonar.duplications.api.index.HashedTuple;

import java.util.Arrays;
import java.util.Set;
import java.util.SortedSet;

public class MemoryIndexBackend implements HashedStatementIndex {

    private final TreeMultimap<String, HashedTuple> filenameIndex;
    private final HashMultimap<ByteArrayWrap, HashedTuple> sequenceHashIndex;

    private static final class ByteArrayWrap {

        private final byte[] data;
        private final int dataHashCode;

        public static ByteArrayWrap create(byte[] data) {
            return new ByteArrayWrap(data);
        }

        private ByteArrayWrap(byte[] data) {
            if (data == null) {
                throw new NullPointerException();
            }
            this.data = data;
            this.dataHashCode = Arrays.hashCode(data);
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof ByteArrayWrap)) {
                return false;
            }
            return Arrays.equals(data, ((ByteArrayWrap) other).data);
        }

        @Override
        public int hashCode() {
            return this.dataHashCode;
        }
    }


    public MemoryIndexBackend() {
        filenameIndex = TreeMultimap.create();
        sequenceHashIndex = HashMultimap.create();
    }

    public SortedSet<HashedTuple> getByFilename(String fileName) {
        return filenameIndex.get(fileName);
    }

    public Set<HashedTuple> getBySequenceHash(byte[] sequenceHash) {
        return sequenceHashIndex.get(ByteArrayWrap.create(sequenceHash));
    }

    public void insert(HashedTuple tuple) {
        filenameIndex.put(tuple.getFileName(), tuple);
        ByteArrayWrap wrap = ByteArrayWrap.create(tuple.getSequenceHash());
        sequenceHashIndex.put(wrap, tuple);
    }

    public void remove(String fileName) {
        Set<HashedTuple> set = filenameIndex.get(fileName);
        filenameIndex.removeAll(fileName);
        for (HashedTuple tuple : set) {
            ByteArrayWrap wrap = ByteArrayWrap.create(tuple.getSequenceHash());
            sequenceHashIndex.remove(wrap, tuple);
        }
    }

    public void remove(HashedTuple tuple) {
        filenameIndex.remove(tuple.getFileName(), tuple);
        ByteArrayWrap wrap = ByteArrayWrap.create(tuple.getSequenceHash());
        sequenceHashIndex.remove(wrap, tuple);
    }

    public void removeAll() {
        filenameIndex.clear();
        sequenceHashIndex.clear();
    }

    public int size() {
        return filenameIndex.size();
    }
}
