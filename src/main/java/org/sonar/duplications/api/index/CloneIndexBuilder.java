package org.sonar.duplications.api.index;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.sonar.duplications.api.DuplicationsException;
import org.sonar.duplications.api.codeunit.CodeUnitProviderFactory;
import org.sonar.duplications.api.codeunit.block.BlockProvider;
import org.sonar.duplications.api.index.store.ICloneIndexStore;
import org.sonar.duplications.api.index.store.StoreFactory;
import org.sonar.duplications.api.index.store.StoreStrategy;
import org.sonar.duplications.api.lexer.ELanguage;
import org.sonar.duplications.api.sourcecode.ISourceCodeElement;
import org.sonar.duplications.api.sourcecode.SourceCodeElement;

/**
 * @author sharif
 *
 */
public class CloneIndexBuilder    {

	protected final static int DEFAULT_BLOCK_LENGTH = 5;

	private int blockLength;

	public CloneIndexBuilder() {
		this(DEFAULT_BLOCK_LENGTH);
	}

	public CloneIndexBuilder(int blockLength) {
		super();
		this.blockLength = blockLength;
	}

	/*public void setStore(ICloneIndexStore store) {
		this.store = store;
	}*/

	public void setBlockLength(int blockLength)
			throws  Exception {
		if (blockLength <= 0) {
			throw new Exception("Block index must be positive!");
		}
		this.blockLength = blockLength;
	}
	/*
	public void setBlockProvider(BlockProvider blockProvider) {
		this.blockProvider = blockProvider;
	}*/

	public void processInput(SourceCodeElement element) throws Exception {
		processInput(element.getFile().getCanonicalPath(), element.getEncoding(), element.getLanguage());
	}

	public void processInput(String filename, Charset encoding, ELanguage language) throws Exception {
		
		ICloneIndexStore store = StoreFactory.getStore(StoreStrategy.DEFAULT);
		
		BlockProvider blockProvider = CodeUnitProviderFactory.getBlockProvider(language, blockLength);
		
		CloneIndex index = new CloneIndex(store, blockProvider);
		List<ISourceCodeElement> elements = listFileElements(filename, encoding, language);

		int fileCount = 0;
		for (ISourceCodeElement element : elements) {
			int units = index.insertFileInStore(element.getFile(), element.getLanguage());
		}
	}

	private List<ISourceCodeElement> listFileElements(String rootDir, Charset encoding, ELanguage language) {
		List<ISourceCodeElement> sourceCodeElementList = new ArrayList<ISourceCodeElement>();
		
		List<String> fileList = new ArrayList<String>();
		try {
			listFiles( new File(rootDir), fileList);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(String fileName : fileList){
			ISourceCodeElement sce = new SourceCodeElement(getOriginId(fileName), encoding, language);
			sourceCodeElementList.add(sce);
		}
		
		return sourceCodeElementList;
	}
	
	//TODO: move to utility class
	private void listFiles(File rootDir, List<String> fileList) throws IOException {
	    if (rootDir.isDirectory()) {
	        String[] children = rootDir.list();
	        for (int i=0; i<children.length; i++) {
	            listFiles(new File(rootDir, children[i]), fileList);
	        }
	    } else {
	        fileList.add(rootDir.getCanonicalPath());
	    }
	}
	
	private static String getOriginId(String fileName) {
		try {
			return new File(fileName).getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
			throw new DuplicationsException("invalid file name: "+ e.getMessage());
		}
	}

}
