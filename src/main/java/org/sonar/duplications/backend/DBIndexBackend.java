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
package org.sonar.duplications.backend;

import org.sonar.duplications.api.index.HashedStatementIndex;
import org.sonar.duplications.api.index.HashedTuple;

import java.util.Set;
import java.util.SortedSet;

//TODO: only stub now
public class DBIndexBackend implements HashedStatementIndex {
    public SortedSet<HashedTuple> getByFilename(String fileName) {
        return null;
    }

    public Set<HashedTuple> getBySequenceHash(byte[] sequenceHash) {
        return null;
    }

    public void insert(HashedTuple tuple) {
    }

    public void remove(String fileName) {
    }

    public void remove(HashedTuple tuple) {
    }

    public void removeAll() {
    }

    public int size() {
        return 0;
    }
}
