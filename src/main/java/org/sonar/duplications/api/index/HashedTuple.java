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
package org.sonar.duplications.api.index;

import java.util.Arrays;

public class HashedTuple implements Comparable {

    static final String HEXES = "0123456789ABCDEF";

    private final String fileName;
    private final int statementIndex;
    private final byte[] sequenceHash;

    private final int fileNameHashCode;
    private final int arrayHashCode;

    public HashedTuple(String fileName, int statementIndex, byte[] sequenceHash) {
        if (sequenceHash == null) {
            throw new IllegalArgumentException("sequenceHash argument cannot be null");
        }
        this.fileName = fileName;
        this.statementIndex = statementIndex;
        this.sequenceHash = sequenceHash;
        this.arrayHashCode = Arrays.hashCode(sequenceHash);
        this.fileNameHashCode = fileName.hashCode();
    }

    public String getFileName() {
        return fileName;
    }

    public int getStatementIndex() {
        return statementIndex;
    }

    public byte[] getSequenceHash() {
        return sequenceHash;
    }

    private static String getHex(byte[] raw) {
        if (raw == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4))
                    .append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof HashedTuple) {
            HashedTuple anotherTuple = (HashedTuple) object;
            return anotherTuple.fileName.equals(fileName)
                    && anotherTuple.statementIndex == statementIndex
                    && Arrays.equals(anotherTuple.sequenceHash, sequenceHash);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return fileNameHashCode + statementIndex + this.arrayHashCode;
    }

    @Override
    public String toString() {
        return "'" + fileName + "'[" + statementIndex + "]:" + getHex(sequenceHash);
    }

    /**
     * Need to compare only tuples with same fileName
     *
     * @param object
     * @return
     */
    public int compareTo(Object object) {
        if (object instanceof HashedTuple) {
            HashedTuple anotherTuple = (HashedTuple) object;
            if (anotherTuple.fileName.equals(fileName)) {
                return statementIndex - anotherTuple.statementIndex;
            }
        }
        return -1;
    }
}
