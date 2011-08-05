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
package org.sonar.duplications.algorithm;

import com.google.common.collect.Lists;
import org.sonar.duplications.index.CloneGroup;

import java.util.List;

public class BruteForceCloneGroupFilter implements CloneGroupFilter {

  public List<CloneGroup> filter(List<CloneGroup> clones) {
    List<CloneGroup> filtered = Lists.newArrayList();
    for (int i = 0; i < clones.size(); i++) {
      CloneGroup first = clones.get(i);
      boolean covered = false;
      for (int j = 0; j < clones.size(); j++) {
        if (i == j) {
          continue;
        }

        CloneGroup second = clones.get(j);
        covered |= first.containsIn(second);
        if (covered) {
          break;
        }
      }
      if (!covered) {
        filtered.add(first);
      }
    }
    return filtered;
  }
}
