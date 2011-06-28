package org.sonar.duplications.java;

import org.sonar.duplications.CloneFinder;
import org.sonar.duplications.block.BlockChunker;
import org.sonar.duplications.index.CloneIndex;
import org.sonar.duplications.statement.JavaStatementBuilder;

/**
 * @author sharif
 *
 */
public class JavaCloneFinder {

	private JavaCloneFinder() {
	}

	public static final CloneFinder build(CloneIndex cloneIndex) {
		CloneFinder.Builder builder = CloneFinder.build()
				.setTokenChunker(JavaTokenProducer.build()) 
				.setStatementChunker(JavaStatementBuilder.build())
				.setBlockChunker(new BlockChunker(5)) 
				.setCloneIndex(cloneIndex);
		return builder.build();
	}
}
