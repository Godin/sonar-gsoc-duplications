package org.sonar.duplications.api.index;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.sonar.duplications.api.DuplicationsException;
import org.sonar.duplications.api.codeunit.block.Block;
import org.sonar.duplications.api.codeunit.block.BlockProvider;
import org.sonar.duplications.api.index.store.ICloneIndexStore;
import org.sonar.duplications.api.lexer.ELanguage;
import org.sonar.duplications.api.sourcecode.ISourceCodeElement;
import org.sonar.duplications.api.sourcecode.SourceCodeElement;

/**
 * @author sharif
 *
 */
public class CloneIndex {

	private final ICloneIndexStore store;		//sample class, need implementation
	private final BlockProvider blockProvider;

	public CloneIndex(ICloneIndexStore store, BlockProvider blockProvider) {
		this.store = store;
		this.blockProvider = blockProvider;
	}

	
	/**
	 * create block for each file and update to store
	 */
	public int insertFileInStore(File sourceFile, ELanguage language) throws  Exception {

		blockProvider.init(createSourceCodeElement(language, sourceFile));

		List<Block> blocks = new ArrayList<Block>();
		Block block = null;
		while ((block = blockProvider.getNext()) != null) {
			blocks.add(block);
		}
		if (blocks.isEmpty()) {
			return 0;
		}
		
		//insert all the blocks at a time
		store.batchInsertBlocks(blocks);
		return blocks.size();
	}


	private static ISourceCodeElement createSourceCodeElement(ELanguage language, File sourceFile)
			throws AssertionError, DuplicationsException, IOException {
		return new SourceCodeElement(sourceFile.getCanonicalPath(), Charset.defaultCharset(), language);
	}

}
