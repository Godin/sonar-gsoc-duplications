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

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.ByteArray;
import org.sonar.duplications.index.AbstractCloneIndex;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Note that this implementation is not thread-safe.
 */
public class MyBatisIndex extends AbstractCloneIndex implements BatchIndex {

  private static final int BATCH_SIZE = 200;

  private final SqlSessionFactory sqlSessionFactory;

  private String cachedResource = "";
  private List<Block> byResourceId = Lists.newArrayList();
  private Map<ByteArray, List<Block>> byHash = Maps.newHashMap();

  private List<Block> blocksToInsert = Lists.newArrayListWithCapacity(BATCH_SIZE);

  private Snapshot snapshot;

  public MyBatisIndex(String environment) {
    try {
      Reader reader = Resources.getResourceAsReader("mybatis.xml");
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader, environment);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void removeAll() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      Mapper mapper = session.getMapper(Mapper.class);
      mapper.recreateTable();
      session.commit();
    } finally {
      session.close();
    }
  }

  /**
   * TODO Godin: note that this will not work if resource was not inserted into index
   */
  public void prepareCache(String resourceId) {
    if (!blocksToInsert.isEmpty()) {
      batchInsert();
    }

    SqlSession session = sqlSessionFactory.openSession();
    try {
      Mapper mapper = session.getMapper(Mapper.class);
      List<Block> blocks = mapper.get(resourceId, snapshot.getId());

      cachedResource = resourceId;
      byResourceId.clear();
      byHash.clear();
      for (Block block : blocks) {
        if (resourceId.equals(block.getResourceId())) {
          byResourceId.add(block);
        }

        List<Block> sameHash = byHash.get(block.getBlockHash());
        if (sameHash == null) {
          sameHash = Lists.newArrayList();
          byHash.put(block.getBlockHash(), sameHash);
        }
        sameHash.add(block);
      }
    } finally {
      session.close();
    }
  }

  public Collection<Block> getByResourceId(String resourceId) {
    if (cachedResource.equals(resourceId)) {
      return byResourceId;
    } else {
      throw new IllegalStateException("Cache must be prepared");
    }
  }

  public Collection<Block> getBySequenceHash(ByteArray sequenceHash) {
    List<Block> result = byHash.get(sequenceHash);
    if (result != null) {
      return result;
    } else {
      return Collections.emptyList();
      // TODO Godin: causes exception on a first analysis
      // throw new IllegalStateException("Cache must be prepared");
    }
  }

  public void insert(Block block) {
    blocksToInsert.add(block);
    if (blocksToInsert.size() == BATCH_SIZE) {
      batchInsert();
    }
  }

  private void batchInsert() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      Mapper mapper = session.getMapper(Mapper.class);
      for (Block block : blocksToInsert) {
        mapper.insert(block.getHashHex(), block.getResourceId(), block.getIndexInFile(), block.getFirstLineNumber(), block.getLastLineNumber(), snapshot.getId());
      }
      session.commit();
      blocksToInsert.clear();
    } finally {
      session.close();
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + sqlSessionFactory.getConfiguration().getEnvironment().getId() + "]";
  }

  /**
   * Create new snapshot.
   */
  public void start(String project) {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      Mapper mapper = session.getMapper(Mapper.class);
      snapshot = new Snapshot();
      snapshot.setProject(project);
      mapper.newSnapshot(snapshot);
      session.commit();
    } finally {
      session.close();
    }
  }

  /**
   * Finalize current snapshot and purge old snapshots.
   */
  public void done() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      Mapper mapper = session.getMapper(Mapper.class);
      mapper.doneSnapshot(snapshot);
      mapper.clean(snapshot.getProject());
      session.commit();
    } finally {
      session.close();
    }
  }

}
