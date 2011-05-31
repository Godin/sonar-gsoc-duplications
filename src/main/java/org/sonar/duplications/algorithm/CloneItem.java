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

public class CloneItem {
    private String firstFileName;
    private String secondFileName;

    private int firstStart;
    private int secondStart;

    private int cloneLength;

    public CloneItem() {
    }

    public CloneItem(String firstFile, int firstStart, String secondFile, int secondStart, int cloneLength) {
        this.firstFileName = firstFile;
        this.secondFileName = secondFile;
        this.firstStart = firstStart;
        this.secondStart = secondStart;
        this.cloneLength = cloneLength;
    }

    public String getFirstFileName() {
        return firstFileName;
    }

    public void setFirstFileName(String firstFileName) {
        this.firstFileName = firstFileName;
    }

    public String getSecondFileName() {
        return secondFileName;
    }

    public void setSecondFileName(String secondFileName) {
        this.secondFileName = secondFileName;
    }

    public int getFirstStart() {
        return firstStart;
    }

    public void setFirstStart(int firstStart) {
        this.firstStart = firstStart;
    }

    public int getSecondStart() {
        return secondStart;
    }

    public void setSecondStart(int secondStart) {
        this.secondStart = secondStart;
    }

    public int getCloneLength() {
        return cloneLength;
    }

    public void setCloneLength(int cloneLength) {
        this.cloneLength = cloneLength;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof CloneItem) {
            CloneItem another = (CloneItem) object;
            return another.firstFileName.equals(firstFileName)
                    && another.firstStart == firstStart
                    && another.secondFileName.equals(secondFileName)
                    && another.secondStart == secondStart
                    && another.cloneLength == cloneLength;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return firstFileName.hashCode() + secondFileName.hashCode() +
                firstStart + secondStart + cloneLength;
    }

    @Override
    public String toString() {
        return "'" + firstFileName + "':[" + firstStart + "] - '" +
                secondFileName + "':[" + secondStart + "] " + cloneLength;
    }
}
