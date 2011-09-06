/*
 * Sonar, open source software quality management tool.
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.sonar.duplications.block.Block;
import org.sonar.duplications.index.ClonePart;

import com.google.common.collect.Lists;

public class ClonePair {

  private Block firstOriginBlock;
  private Block lastOriginBlock;
  private Block firstOtherBlock;
  private Block lastOtherBlock;

  private List<ClonePart> parts;
  private int length;

  private boolean constructed = false;

  public ClonePair(Block originBlock, Block otherBlock) {
    this.firstOriginBlock = originBlock;
    this.lastOriginBlock = originBlock;
    this.firstOtherBlock = otherBlock;
    this.lastOtherBlock = otherBlock;
    this.length = 1;
  }

  public void increase(Block originBlock, Block otherBlock) {
    this.lastOriginBlock = originBlock;
    this.lastOtherBlock = otherBlock;
    this.length++;
  }

  public void finishConstruction() {
    if (constructed) {
      return;
    }
    ClonePart origin = new ClonePart(firstOriginBlock.getResourceId(), firstOriginBlock.getIndexInFile(), firstOriginBlock.getFirstLineNumber(), lastOriginBlock.getLastLineNumber());
    ClonePart other = new ClonePart(firstOtherBlock.getResourceId(), firstOtherBlock.getIndexInFile(), firstOtherBlock.getFirstLineNumber(), lastOtherBlock.getLastLineNumber());
    this.parts = Lists.newArrayList(origin, other);
    constructed = true;
  }

  public int getCloneUnitLength() {
    return length;
  }

  public ClonePart getOriginPart() {
    finishConstruction();
    return parts.get(0);
  }

  public ClonePart getAnotherPart() {
    finishConstruction();
    return parts.get(1);
  }

  public List<ClonePart> getCloneParts() {
    return Collections.unmodifiableList(parts);
  }

  public boolean containsIn(ClonePair other) {
    if (!getOriginPart().getResourceId().equals(other.getOriginPart().getResourceId())) {
      return false;
    }
    boolean[] used = new boolean[other.getCloneParts().size()];
    Arrays.fill(used, false);
    for (ClonePart first : this.getCloneParts()) {
      int firstUnitEnd = first.getUnitStart() + getCloneUnitLength();
      boolean found = false;
      int counter = 0;
      for (ClonePart second : other.getCloneParts()) {
        if (used[counter]) {
          counter++;
          continue;
        }
        int secondUnitEnd = second.getUnitStart() + other.getCloneUnitLength();

        if (first.getResourceId().equals(second.getResourceId()) &&
            first.getUnitStart() >= second.getUnitStart() &&
            firstUnitEnd <= secondUnitEnd) {
          found = true;
          used[counter] = true;
          break;
        }
        counter++;
      }
      if (!found) {
        return false;
      }
    }
    return true;
  }

}
