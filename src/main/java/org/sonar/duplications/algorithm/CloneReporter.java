/*
 * Sonar, open source software quality management tool.
 * Written (W) 2011 Andrew Tereskin
 * Copyright (C) 2008-2011 SonarSource
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
package org.sonar.duplications.algorithm;

import org.sonar.duplications.api.index.HashedStatementIndex;
import org.sonar.duplications.api.index.HashedTuple;

import java.util.*;

public class CloneReporter {

  /**
   * Use this wrapper int Set to intersect only by fileName
   */
  private static class HashedTupleWrapper implements Comparable {

    private final HashedTuple tuple;

    private HashedTupleWrapper(HashedTuple tuple) {
      this.tuple = tuple;
    }

    public HashedTuple getTuple() {
      return tuple;
    }

    @Override
    public boolean equals(Object object) {
      if (object instanceof HashedTupleWrapper) {
        HashedTupleWrapper another = (HashedTupleWrapper) object;
        return another.tuple.getFileName().equals(tuple.getFileName());
      }
      return false;
    }

    @Override
    public int hashCode() {
      return tuple.getFileName().hashCode();
    }

    public int compareTo(Object object) {
      if (object instanceof HashedTupleWrapper) {
        HashedTupleWrapper another = (HashedTupleWrapper) object;
        return tuple.getFileName().compareTo(another.tuple.getFileName());
      }
      return -1;
    }
  }


  public static List<CloneItem> reportClones(String filename, HashedStatementIndex index) {
    SortedSet<HashedTuple> fileSet = index.getByFilename(filename);

    int totalSequences = fileSet.size();
    ArrayList<Set<HashedTupleWrapper>> tuplesC = new ArrayList<Set<HashedTupleWrapper>>(totalSequences);
    ArrayList<HashedTuple> fileTuples = new ArrayList<HashedTuple>(totalSequences);

    ArrayList<CloneItem> clones = new ArrayList<CloneItem>();

    prepareSets(fileSet, tuplesC, fileTuples, index);

    for (int i = 0; i < totalSequences; i++) {
      boolean containsInPrev = i > 0 && tuplesC.get(i - 1).containsAll(tuplesC.get(i));
      if (tuplesC.get(i).size() < 2 || containsInPrev) {
        continue;
      }

      Set<HashedTupleWrapper> current = new TreeSet<HashedTupleWrapper>(tuplesC.get(i));
      for (int j = i + 1; j < totalSequences + 1; j++) {
        Set<HashedTupleWrapper> intersected = new TreeSet<HashedTupleWrapper>(current);
        //do intersection
        intersected.retainAll(tuplesC.get(j));

        //if intersection size is smaller than original
        if (intersected.size() < current.size()) {
          //report clones from tuplesC[i] to current
          int cloneLength = j - i;
          Set<HashedTupleWrapper> beginSet = tuplesC.get(i);
          Set<HashedTupleWrapper> prebeginSet = new TreeSet<HashedTupleWrapper>();
          if (i > 0) {
            prebeginSet = tuplesC.get(i - 1);
          }
          reportClone(fileTuples.get(i), beginSet, prebeginSet, intersected, cloneLength, clones);
        }

        current = intersected;
        boolean inPrev = i > 0 && tuplesC.get(i - 1).containsAll(current);
        if (current.size() < 2 || inPrev) {
          break;
        }
      }
    }
    return clones;
  }

  private static void prepareSets(SortedSet<HashedTuple> fileSet, List<Set<HashedTupleWrapper>> tuplesC,
                                  List<HashedTuple> fileTuples, HashedStatementIndex index) {
    for (HashedTuple tuple : fileSet) {
      Set<HashedTuple> set = index.getBySequenceHash(tuple.getSequenceHash());
      Set<HashedTupleWrapper> wrapSet = new TreeSet<HashedTupleWrapper>();
      for (HashedTuple tup : set) {
        wrapSet.add(new HashedTupleWrapper(tup));
      }
      fileTuples.add(tuple);
      tuplesC.add(wrapSet);
    }
    //to fix last element bug
    tuplesC.add(new TreeSet<HashedTupleWrapper>());
  }

  private static void reportClone(HashedTuple beginTuple, Set<HashedTupleWrapper> beginSet, Set<HashedTupleWrapper> prebeginSet,
                                  Set<HashedTupleWrapper> intersected, int cloneLength, List<CloneItem> clones) {
    String firstFile = beginTuple.getFileName();
    int firstStart = beginTuple.getStatementIndex();

    //cycle in filenames in clone start position
    for (HashedTupleWrapper tmpTupWrap : beginSet) {
      HashedTuple tmpTuple = tmpTupWrap.getTuple();
      if (!firstFile.equals(tmpTuple.getFileName()) && !intersected.contains(tmpTupWrap)
          && !prebeginSet.contains(tmpTupWrap)) {
        String secondFile = tmpTuple.getFileName();
        int secondStart = tmpTuple.getStatementIndex();

        CloneItem item = new CloneItem();
        item.setFirstFileName(firstFile);
        item.setSecondFileName(secondFile);
        item.setFirstStart(firstStart);
        item.setSecondStart(secondStart);
        item.setCloneLength(cloneLength);
        clones.add(item);
      }
    }
  }
}
