package org.sonar.duplications.api.sourcecode;

import java.io.File;
import java.nio.charset.Charset;

import org.sonar.duplications.api.lexer.ELanguage;

/**
 * @author sharif
 *
 */
public interface ISourceCodeElement {

	public File getFile();
	
	public Charset getEncoding();
	
	public ELanguage getLanguage();

	public ISourceCodeElement[] getChildren();

	public ISourceCodeElement getParent();

}
