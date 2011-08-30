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
