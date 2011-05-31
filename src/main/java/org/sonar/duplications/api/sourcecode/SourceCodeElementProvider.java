package org.sonar.duplications.api.sourcecode;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sonar.duplications.api.DuplicationsException;
import org.sonar.duplications.api.lexer.ELanguage;
import org.sonar.duplications.api.provider.ProviderBase;
import org.sonar.duplications.api.sourcecode.ISourceCodeElement;
import org.sonar.duplications.api.sourcecode.SourceCodeElement;

/**
 * @author sharif
 *
 * @param <Element>
 */
public class SourceCodeElementProvider<Element extends ISourceCodeElement>
		extends ProviderBase<Element, Element, DuplicationsException>
		implements ISourceCodeElementProvider<Element>, Serializable {

	private transient Iterator<Element> elementsIterator;

	public SourceCodeElementProvider() {
		// nothing to do
	}

	@Override
	public void init(Element root) {
		List<Element> elements = listFileElements(root);
		elementsIterator = elements.iterator();
	}

	private List<Element> listFileElements(Element root) {
		List<Element> sourceCodeElementList = new ArrayList<Element>();
		
		List<String> fileList = new ArrayList<String>();
		try {
			listFiles( root.getFile(), fileList);
		} catch (IOException e) {
			throw new DuplicationsException(e.getMessage());
		}
		
		for(String fileName : fileList){
			ISourceCodeElement sce = new SourceCodeElement(getOriginId(fileName), root.getEncoding(), root.getLanguage());
			sourceCodeElementList.add((Element) sce);
		}
		
		return sourceCodeElementList;
	}
	
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
	
	
	
	/** {@inheritDoc} */
	@Override
	protected Element provideNext() {
		// no more elements to return
		if (elementsIterator == null) {
			return null;
		}

		// if more elements present, return next element
		if (elementsIterator.hasNext()) {
			return elementsIterator.next();
		}

		// no more elements: delete iterator and return null
		elementsIterator = null;
		return null;
	}

}