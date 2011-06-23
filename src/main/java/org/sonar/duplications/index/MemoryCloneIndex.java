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
package org.sonar.duplications.index;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;

import org.sonar.duplications.block.Block;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.TreeMultimap;

public class MemoryCloneIndex implements CloneIndex {

  private final TreeMultimap<String, Block> filenameIndex;
  private final HashMultimap<String, Block> sequenceHashIndex;

 /**
 * block hashes are now stored in String, so this class is no longer needed
 */
@Deprecated
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
      return other instanceof ByteArrayWrap &&
          Arrays.equals(data, ((ByteArrayWrap) other).data);
    }

    @Override
    public int hashCode() {
      return this.dataHashCode;
    }
  }

  private static final class ValueComparator implements Comparator<Block> {

    public int compare(Block o1, Block o2) {
      if (o2.getResourceId().equals(o1.getResourceId())) {
        return o1.getIndexInFile() - o2.getIndexInFile();
      }
      return -1;
    }

    public boolean equals(Object obj) {
      return obj instanceof ValueComparator;
    }
  }

  private static final class KeyComparator implements Comparator<String> {

    public int compare(String o1, String o2) {
      return o1.compareTo(o2);
    }

    public boolean equals(Object obj) {
      return obj instanceof KeyComparator;
    }
  }

  public MemoryCloneIndex() {
    filenameIndex = TreeMultimap.create(new KeyComparator(), new ValueComparator());
    sequenceHashIndex = HashMultimap.create();
  }

  public Set<String> getAllUniqueResourceId() {
    return filenameIndex.keySet();
  }

  public boolean containsResourceId(String resourceId) {
    return filenameIndex.containsKey(resourceId);
  }

  public SortedSet<Block> getByResourceId(String fileName) {
    return filenameIndex.get(fileName);
  }

  public Set<Block> getBySequenceHash(String sequenceHash) {
    return sequenceHashIndex.get(sequenceHash);
  }

  public void insert(Block tuple) {
    filenameIndex.put(tuple.getResourceId(), tuple);
    //ByteArrayWrap wrap = ByteArrayWrap.create(tuple.getBlockHash());
    sequenceHashIndex.put(tuple.getBlockHash(), tuple);
  }

  public void remove(String fileName) {
    Set<Block> set = filenameIndex.get(fileName);
    filenameIndex.removeAll(fileName);
    for (Block tuple : set) {
      //ByteArrayWrap wrap = ByteArrayWrap.create(tuple.getBlockHash());
      sequenceHashIndex.remove(tuple.getBlockHash(), tuple);
    }
  }

  public void remove(Block tuple) {
    filenameIndex.remove(tuple.getResourceId(), tuple);
    //ByteArrayWrap wrap = ByteArrayWrap.create(tuple.getBlockHash());
    sequenceHashIndex.remove(tuple.getBlockHash(), tuple);
  }

  public void removeAll() {
    filenameIndex.clear();
    sequenceHashIndex.clear();
  }

  public int size() {
    return filenameIndex.size();
  }
}
