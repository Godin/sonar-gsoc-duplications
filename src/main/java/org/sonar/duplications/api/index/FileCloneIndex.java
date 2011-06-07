package org.sonar.duplications.api.index;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.sonar.duplications.api.codeunit.Block;
import org.sonar.duplications.api.codeunit.BlockProvider;

/**
 * @author sharif
 *
 */
public class FileCloneIndex {

	private File sourceFile;
	private List<Block> blockList;

	public FileCloneIndex(File sourceFile, BlockProvider blockProvider) {
		this.sourceFile = sourceFile;
		blockList = new ArrayList<Block>();
		init(blockProvider);
	}

	public File getSourceFile() {
		return sourceFile;
	}

	public List<Block> getBlockList() {
		return blockList;
	}

	private void init(BlockProvider blockProvider){
		blockProvider.init(sourceFile);
		Block block = null;
		while ((block = blockProvider.getNext()) != null) {
			blockList.add(block);
		}
	}

}
