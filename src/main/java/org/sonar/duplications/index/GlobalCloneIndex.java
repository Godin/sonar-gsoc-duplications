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
package org.sonar.duplications.index;

import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.FileCloneIndex;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GlobalCloneIndex {

  private final CloneIndexBackend backend;

  public GlobalCloneIndex(CloneIndexBackend backend) {
    this.backend = backend;
  }

  public void addOrUpdateFileCloneIndex(FileCloneIndex fileBlockGroup) {
    removeFileCloneIndex(fileBlockGroup.getFileResourceId());
    for (Block block : fileBlockGroup.getBlockList()) {
      backend.insert(block);
    }
  }

  public void removeFileCloneIndex(String fileResourceId) {
    if (backend.containsResourceId(fileResourceId)) {
      backend.remove(fileResourceId);
    }
  }

  public Set<Clone> getClones() {
    Set<String> resourceIds = backend.getAllUniqueResourceId();
    Set<Clone> clones = new HashSet<Clone>();
    for (String resourceId : resourceIds) {
      List<Clone> res = CloneReporter.reportClones(resourceId, backend);
      clones.addAll(res);
    }
    return clones;
  }
}
