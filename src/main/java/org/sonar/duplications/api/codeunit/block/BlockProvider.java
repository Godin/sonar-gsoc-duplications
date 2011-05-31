package org.sonar.duplications.api.codeunit.block;

import java.io.IOException;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.sonar.duplications.api.DuplicationsException;
import org.sonar.duplications.api.codeunit.statement.Statement;
import org.sonar.duplications.api.codeunit.statement.StatementProvider;
import org.sonar.duplications.api.provider.ProviderBase;
import org.sonar.duplications.api.sourcecode.ISourceCodeElement;

/**
 * @author sharif
 *
 */
public class BlockProvider extends 
	ProviderBase<ISourceCodeElement, Block, DuplicationsException> implements Serializable {

	private final StatementProvider statementProvider;

	private ISourceCodeElement currentElement;
	
	private int blockSize;
	
	private final MessageDigest digest;

	public BlockProvider(StatementProvider statementProvider, int blockSize) {
		this.statementProvider = statementProvider;
		this.blockSize = blockSize;
		try {
			this.digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			//e.printStackTrace();
			throw new DuplicationsException(e.getMessage());
		}
	}

	@Override
	public void init(ISourceCodeElement currentElement) throws DuplicationsException {
		statementProvider.init(currentElement);
		this.currentElement = currentElement;
	}

	private List<Statement> statementsForBlock= new ArrayList<Statement>();
	
	@Override
	protected Block provideNext() throws DuplicationsException {
		if(statementsForBlock.isEmpty()){
			for (int i = 0; i < blockSize && statementProvider.lookahead(1) != null; i++) {
				statementsForBlock.add(statementProvider.getNext());
			}
			return new Block(getOriginId(currentElement), buildBlockHash(statementsForBlock), statementsForBlock.get(0).getIndexInFile(), 
					statementsForBlock.get(0).getStartLine(), statementsForBlock.get(statementsForBlock.size()-1).getEndLine());
			
		} else if(statementProvider.lookahead(1) != null){
			statementsForBlock.remove(0);
			statementsForBlock.add(statementProvider.getNext());
			return new Block(getOriginId(currentElement), buildBlockHash(statementsForBlock), statementsForBlock.get(0).getIndexInFile(), 
					statementsForBlock.get(0).getStartLine(), statementsForBlock.get(statementsForBlock.size()-1).getEndLine());
			
		}
		return null;
	}

	private byte[] buildBlockHash(List<Statement> statementList) {
		digest.reset();
		for (Statement statement : statementList) {
			digest.update(statement.getNormalizedContent().getBytes());
		}
		return digest.digest();
	}
	
	private static String getOriginId(ISourceCodeElement element) {
		try {
			return element.getFile().getCanonicalPath();
		} catch (IOException e) {
			throw new DuplicationsException(e.getMessage());
		}
	}
	
}