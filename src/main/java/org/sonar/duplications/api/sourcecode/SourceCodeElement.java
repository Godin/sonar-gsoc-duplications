package org.sonar.duplications.api.sourcecode;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sonar.duplications.api.DuplicationsException;
import org.sonar.duplications.api.lexer.ELanguage;

/**
 * @author sharif
 *
 */
public class SourceCodeElement implements ISourceCodeElement {

	private final String localName;

	private File file;

	private final Charset encoding;
	
	private final ELanguage language;
	
	private List<SourceCodeElement> children = null;

	private SourceCodeElement parent = null;

	public SourceCodeElement(String localName, Charset encoding,
			ELanguage language) throws DuplicationsException {
		this.localName = localName;
		this.encoding = encoding; 
		this.language = language;
	}
	
	public boolean hasChildren() {
		return children != null && !children.isEmpty();
	}

	public SourceCodeElement[] getChildren() {
		if (!hasChildren()) {
			return null;
		}
		SourceCodeElement[] result = allocateArray(children.size());
		children.toArray(result);
		return result;
	}

	private SourceCodeElement[] allocateArray(int size) {
		return new SourceCodeElement[size];
	}

	public SourceCodeElement getParent() {
		return parent;
	}

	protected void setParent(SourceCodeElement parent) {
		this.parent = parent ;
	}
	
	public void addChild(SourceCodeElement childNode) {
		if (children == null) {
			children = new ArrayList<SourceCodeElement>();
		}
		children.add(childNode);
		childNode.setParent(this);
	}
	
	public ELanguage getLanguage() {
		return language;
	}
	
	public String getLocalName() {
		return localName;
	}

	public Charset getEncoding() {
		return encoding;
	}
	
	public final String getId() throws IOException {
		return getFile().getCanonicalPath();
	}

	public File getFile() {
		if (file != null) {
			return file;
		}
		try {
			file = new File(createId());
			return file;
		} catch (IOException e) {
			throw new AssertionError(
					"We expect a concatenation of canonical pathes to be canonical.");
		}
	}

	private String createId() throws IOException {
		if (getParent() == null) {
			return localName;
		}
		String id = getParent().getId();
		if (id.length() > 0) {
			id += File.separator;
		}
		id += localName;
		return id;
	}
	
}