package org.sonar.duplications.benchmark.hash;

import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;
import org.sonar.duplications.benchmark.*;
import org.sonar.duplications.block.BlockChunker;

import java.io.File;
import java.util.List;

import static org.hamcrest.Matchers.greaterThan;

@Ignore("Irrelevant for the moment")
public class HashCollisionsTest {

  protected static int BLOCK_SIZE = 13;
  protected static int WARMUP_ROUNDS = 1;
  protected static int BENCHMARK_ROUNDS = 10;

  protected static List<File> files;
  protected static BenchmarksDiff results = new BenchmarksDiff();

  protected BenchmarkResult run(Benchmark benchmark) {
    return benchmark.runBenchmark(BENCHMARK_ROUNDS, WARMUP_ROUNDS);
  }

  protected void printStatistics(HashCollisionsBenchmark benchmark) {
    System.out.println("Total blocks: " + benchmark.getTotalBlocks());
    System.out.println("Total unique hashes: " + benchmark.getUniqueHashes());
    System.out.println("Hashes checksum: " + benchmark.getHashesChecksum());
  }

  @BeforeClass
  public static void before() {
    files = Utils.filesFromJdk16();
    Assume.assumeThat(files.size(), greaterThan(0));
  }

  @Test
  public void longRabinKarpHash() {
    BlockChunker chunker = new BlockChunker(BLOCK_SIZE);
    HashCollisionsBenchmark benchmark = new HashCollisionsBenchmark("Rabin-Karp long", files, chunker);
    results.setReference(run(benchmark));
    printStatistics(benchmark);
  }

  @Test
  public void intRabinKarpHash() {
    BlockChunker chunker = new IntRabinKarpBlockChunker(BLOCK_SIZE);
    HashCollisionsBenchmark benchmark = new HashCollisionsBenchmark("Rabin-Karp int", files, chunker);
    results.add(run(benchmark));
    printStatistics(benchmark);
  }

  @Test
  public void md5Hash() {
    DigestHashBlockChunker.Algorithm algo = DigestHashBlockChunker.Algorithm.MD5;
    BlockChunker chunker = new DigestHashBlockChunker(algo, BLOCK_SIZE);
    HashCollisionsBenchmark benchmark = new HashCollisionsBenchmark("MD5", files, chunker);
    results.add(run(benchmark));
    printStatistics(benchmark);
  }

  @Test
  public void shaHash() {
    DigestHashBlockChunker.Algorithm algo = DigestHashBlockChunker.Algorithm.SHA;
    BlockChunker chunker = new DigestHashBlockChunker(algo, BLOCK_SIZE);
    HashCollisionsBenchmark benchmark = new HashCollisionsBenchmark("SHA", files, chunker);
    results.add(run(benchmark));
    printStatistics(benchmark);
  }

  @Test
  public void murmurHash() {
    BlockChunker chunker = new MurmurHashBlockChunker(BLOCK_SIZE);
    HashCollisionsBenchmark benchmark = new HashCollisionsBenchmark("Murmur", files, chunker);
    results.add(run(benchmark));
    printStatistics(benchmark);
  }

  @AfterClass
  public static void after() {
    results.print();
  }

}
