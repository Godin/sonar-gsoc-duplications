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
package org.sonar.plugins.cpd.backends;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.cpd.IndexBlock;
import org.sonar.api.database.DatabaseSession;
import org.sonar.duplications.block.Block;
import org.sonar.duplications.block.ByteArray;
import org.sonar.duplications.index.CloneIndex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class DbCloneIndex implements CloneIndex, CacheSequenceHashQuery {

  private static final Logger LOG = LoggerFactory.getLogger(DbCloneIndex.class);

  private final DatabaseSession session;

  private int cachedNextBlock;
  private Block[] cachedResourceBlocks;
  private HashMap<ByteArray, List<Block>> cachedFoundBlocks;
  private String cachedResourceId;
  private boolean cached = false;

  public DbCloneIndex(DatabaseSession session) {
    this.session = session;
  }

  public Collection<String> getAllUniqueResourceId() {
    String hql = "SELECT d FROM IndexBlock d WHERE index_in_file = 0 GROUP BY resource_id";
    List<IndexBlock> list = session.createQuery(hql).getResultList();
    List<String> resources = new ArrayList<String>();
    for (IndexBlock indexBlock : list) {
      resources.add(indexBlock.getResourceId());
    }
    return resources;
  }

  public boolean containsResourceId(String resourceId) {
    String hql = "SELECT d FROM IndexBlock d WHERE resource_id=:resource_id";
    hql += " AND index_in_file = 0";
    List<IndexBlock> list = session.createQuery(hql)
        .setParameter("resource_id", resourceId)
        .getResultList();
    return !list.isEmpty();
  }

  public Collection<Block> getByResourceId(String resourceId) {
    String hql = "SELECT d FROM IndexBlock d WHERE resource_id=:resource_id";
    hql += " ORDER BY index_in_file ASC";
    List<IndexBlock> list = session.createQuery(hql)
        .setParameter("resource_id", resourceId)
        .getResultList();
    List<Block> blocks = new ArrayList<Block>(list.size());
    for (IndexBlock indexBlock : list) {
      Block block = new Block(indexBlock.getResourceId(),
          new ByteArray(indexBlock.getBlockHash()), indexBlock.getIndexInFile(),
          indexBlock.getStartLine(), indexBlock.getEndLine());
      blocks.add(block);
    }
    return blocks;
  }

  public Collection<Block> getBySequenceHash(ByteArray blockHash) {
    if (cached) {
      if (!cachedResourceBlocks[cachedNextBlock].getBlockHash().equals(blockHash)) {
        cachedNextBlock++;
        if (cachedNextBlock >= cachedResourceBlocks.length) {
          cachedNextBlock = cachedResourceBlocks.length - 1;
        }
      }
      if (cachedResourceBlocks[cachedNextBlock].getBlockHash().equals(blockHash)) {
        List<Block> blocks = cachedFoundBlocks.get(blockHash);
        return blocks;
      } else {
        LOG.info("BlockHash " + blockHash.toString() + " not found in cache for file " + cachedResourceId);
        cached = false;
      }
    }

    String sql = "SELECT * from index_blocks WHERE block_hash=:block_hash";
    List<IndexBlock> list = session.getEntityManager()
        .createNativeQuery(sql, IndexBlock.class)
        .setParameter("block_hash", blockHash.toString())
        .getResultList();
    List<Block> blocks = new ArrayList<Block>(list.size());
    for (IndexBlock indexBlock : list) {
      Block block = new Block(indexBlock.getResourceId(),
          new ByteArray(indexBlock.getBlockHash()), indexBlock.getIndexInFile(),
          indexBlock.getStartLine(), indexBlock.getEndLine());
      blocks.add(block);
    }
    return blocks;
  }

  public void insert(Block block) {
    IndexBlock indexBlock = new IndexBlock(block.getResourceId(),
        block.getBlockHash().toString(), block.getIndexInFile(),
        block.getFirstLineNumber(), block.getLastLineNumber());
    session.save(indexBlock);
  }

  public void remove(String resourceId) {
    String hql = "DELETE FROM IndexBlock WHERE resource_id=:resource_id";
    session.createQuery(hql)
        .setParameter("resource_id", resourceId)
        .executeUpdate();
  }

  public void remove(Block block) {
    String hql = "DELETE FROM IndexBlock WHERE block_hash=:block_hash";
    hql += " AND resource_id=:resource_id AND index_in_file=:index_in_file";
    session.createQuery(hql)
        .setParameter("block_hash", block.getBlockHash())
        .setParameter("resource_id", block.getResourceId())
        .setParameter("index_in_file", block.getIndexInFile())
        .executeUpdate();
  }

  public void removeAll() {
    session.createQuery("DELETE FROM IndexBlock")
        .executeUpdate();
  }

  public int size() {
    return session.createQuery("SELECT d FROM IndexBlock d")
        .getResultList().size();
  }

  public void cacheResourceIdForSequenceHashQueries(String resourceId) {
    cachedNextBlock = 0;
    cachedResourceId = resourceId;
    cached = true;

    String sql1 = "SELECT * FROM index_blocks WHERE resource_id = :resource_id";
    sql1 += " ORDER BY index_in_file ASC";
    List<IndexBlock> resource_blocks = session.getEntityManager()
        .createNativeQuery(sql1, IndexBlock.class)
        .setParameter("resource_id", resourceId)
        .getResultList();
    cachedResourceBlocks = new Block[resource_blocks.size()];
    cachedFoundBlocks = new HashMap<ByteArray, List<Block>>(resource_blocks.size());
    int counter = 0;
    for (IndexBlock indexBlock : resource_blocks) {
      Block block = new Block(indexBlock.getResourceId(),
          new ByteArray(indexBlock.getBlockHash()),
          indexBlock.getIndexInFile(),
          indexBlock.getStartLine(),
          indexBlock.getEndLine());
      cachedResourceBlocks[counter++] = block;
      cachedFoundBlocks.put(block.getBlockHash(), new ArrayList<Block>());
    }

    String sql2 = "SELECT * FROM index_blocks WHERE block_hash in";
    sql2 += " ( SELECT block_hash FROM index_blocks WHERE resource_id = :resource_id )";
    List<IndexBlock> found_blocks = session.getEntityManager()
        .createNativeQuery(sql2, IndexBlock.class)
        .setParameter("resource_id", resourceId)
        .getResultList();
    for (IndexBlock indexBlock : found_blocks) {
      Block block = new Block(indexBlock.getResourceId(),
          new ByteArray(indexBlock.getBlockHash()),
          indexBlock.getIndexInFile(),
          indexBlock.getStartLine(),
          indexBlock.getEndLine());
      List<Block> blocks = cachedFoundBlocks.get(block.getBlockHash());
      blocks.add(block);
    }
  }
}
