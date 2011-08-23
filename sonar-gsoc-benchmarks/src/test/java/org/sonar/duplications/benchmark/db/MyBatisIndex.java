package org.sonar.duplications.benchmark.db;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
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

  public MyBatisIndex(String environment) {
    try {
      Reader reader = Resources.getResourceAsReader("mybatis.xml");
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader, environment);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void init() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      Mapper mapper = session.getMapper(Mapper.class);
      mapper.createTable();
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
      List<Block> blocks = mapper.get(resourceId);

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
      throw new IllegalStateException("Cache must be prepared");
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
        mapper.insert(block);
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

}
