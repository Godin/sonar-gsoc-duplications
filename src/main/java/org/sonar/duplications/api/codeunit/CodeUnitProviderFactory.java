package org.sonar.duplications.api.codeunit;

import org.sonar.duplications.api.codeunit.block.BlockProvider;
import org.sonar.duplications.api.codeunit.statement.StatementProvider;
import org.sonar.duplications.api.codeunit.token.TokenProvider;
import org.sonar.duplications.api.lexer.ELanguage;
import org.sonar.duplications.api.lexer.Lexer;
import org.sonar.duplications.api.lexer.LexterFactory;

/**
 * @author sharif
 *
 */
public class CodeUnitProviderFactory {

	public static StatementProvider getStatementProvider(ELanguage language) {
		//SourceCodeElement rootSourceElement = new SourceCodeElement(filename , Charset.defaultCharset(), ELanguage.JAVA);
		Lexer lexer = LexterFactory.getLexer(language);
		TokenProvider tokenProvider = new TokenProvider(lexer);
		return new StatementProvider(tokenProvider, true);
	}

	public static BlockProvider getBlockProvider(ELanguage language,
			int blockLength) {
		StatementProvider statementProvider = getStatementProvider(language);
		return new BlockProvider(statementProvider, blockLength);
	}	
}
