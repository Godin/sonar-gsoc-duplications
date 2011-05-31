package org.sonar.duplications.api.lexer;

import org.sonar.duplications.api.lexer.family.JavaLexer;


/**
 * @author sharif
 *
 */
public class LexterFactory {

	public static Lexer getLexer(ELanguage lang) {
		if(lang == ELanguage.JAVA){
			return JavaLexer.build();
		}
		return null;
	}
	
	public static Lexer getJavaLexer() {
		return JavaLexer.build();
	}
}
