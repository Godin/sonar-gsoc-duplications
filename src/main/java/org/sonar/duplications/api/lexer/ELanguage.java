package org.sonar.duplications.api.lexer;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sonar.duplications.api.codeunit.statement.IStatementOracle;
import org.sonar.duplications.api.codeunit.statement.StatementOracle;

/**
 * @author sharif
 *
 */
public enum ELanguage {

	JAVA(asHashSet(";", "}", "{"), "java"),

	CPP(asHashSet(";", "}", "{"), "cpp", "cc", "c", "h", "hh", "hpp");

	private final String[] extensions;

	private static HashMap<String, ELanguage> extension2LanguageMap = new HashMap<String, ELanguage>();

	private final IStatementOracle statementOracle;

	static {
		for (ELanguage language : values()) {
			for (String extension : language.extensions) {
				assert !extension2LanguageMap.containsKey(extension.toLowerCase()) : "Duplicate extension " + extension;
				extension2LanguageMap.put(extension.toLowerCase(), language);
			}
		}
	}

	private ELanguage(Set<String> statementDelimiters, String... extensions) {
		statementOracle = new StatementOracle(statementDelimiters);
		this.extensions = extensions;
	}

	private static Set<String> asHashSet(String... element) {
		List<String> list = Arrays.asList(element);
		Set<String> result = new HashSet<String>(list);
		return result;
	}
	
	public String[] getFileExtensions() {
		return extensions;
	}

	public IStatementOracle getStatementOracle() {
		return statementOracle;
	}

	public static ELanguage fromFileExtension(String path) {
		return fromFileExtension(new File(path));
	}

	public static ELanguage fromFileExtension(File file) {
		String extension = getFileExtension(file);
		if (extension == null || extension.length() == 0) {
			throw new IllegalArgumentException("Filename has no extension");
		}
		ELanguage result = extension2LanguageMap.get(extension.toLowerCase());

		if (result == null) {
			throw new IllegalArgumentException("Extension '" + extension
					+ "' unknown");
		}
		return result;
	}

	//TODO: move file utility
	private static String getFileExtension(File file) {
		String ext = null;
		if(file.isFile()){
		    int dot = file.getPath().lastIndexOf(".");
		    if(dot > -1)
		    	ext = file.getPath().substring(dot + 1);
		}
		return ext;
	}

	 
}