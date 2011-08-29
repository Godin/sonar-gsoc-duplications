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
package org.sonar.duplications.benchmark.db;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.sonar.duplications.block.Block;

public interface Mapper {

  void insert(
      @Param("hash") String hash,
      @Param("resource_id") String resourceId,
      @Param("index_in_file") int indexInFile,
      @Param("first_line") int firstLineNumber,
      @Param("last_line") int lastLineNumber,
      @Param("current_snapshot_id") int snapshotId);

  List<Block> get(
      @Param("resource_id") String resourceId,
      @Param("current_snapshot_id") int snapshotId);

  void recreateTable();

  void newSnapshot(Snapshot snapshot);

  void doneSnapshot(Snapshot snapshot);

  void clean(String project);

}
