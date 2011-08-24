package org.sonar.duplications.benchmark.db;

import java.util.List;

import org.sonar.duplications.block.Block;

public interface Mapper {

  void insert(Block block);

  List<Block> get(String resource);

  void recreateTable();

}
