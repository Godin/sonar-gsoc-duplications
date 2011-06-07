package org.sonar.duplications.api.codeunit;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.sonar.duplications.api.DuplicationsException;
import org.sonar.duplications.api.provider.ProviderBase;

/**
 * @author sharif
 *
 */
public class BlockProvider extends ProviderBase<File, Block> {

	private static final long serialVersionUID = -7421443570641400239L;

	public final static int DEFAULT_BLOCK_SIZE = 5;
	
	private final StatementProvider statementProvider;

	private File currentElement;
	
	private int blockSize;
	
	private final MessageDigest digest;

	public BlockProvider(StatementProvider statementProvider, int blockSize) {
		this.statementProvider = statementProvider;
		this.blockSize = blockSize;
		try {
			this.digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new DuplicationsException(e.getMessage());
		}
	}

	@Override
	public void init(File currentElement) {
		statementProvider.init(currentElement);
		this.currentElement = currentElement;
	}

	private List<Statement> statementsForBlock= new ArrayList<Statement>();
	
	@Override
	protected Block provideNext() {
		try{
		if(statementsForBlock.isEmpty()){
			for (int i = 0; i < blockSize && statementProvider.lookahead(1) != null; i++) {
				statementsForBlock.add(statementProvider.getNext());
			}
			return new Block(currentElement.getCanonicalPath(), buildBlockHash(statementsForBlock), statementsForBlock.get(0).getIndexInFile(), 
					statementsForBlock.get(0).getStartLine(), statementsForBlock.get(statementsForBlock.size()-1).getEndLine());
			
		} else if(statementProvider.lookahead(1) != null){
			statementsForBlock.remove(0);
			statementsForBlock.add(statementProvider.getNext());
			return new Block(currentElement.getCanonicalPath(), buildBlockHash(statementsForBlock), statementsForBlock.get(0).getIndexInFile(), 
					statementsForBlock.get(0).getStartLine(), statementsForBlock.get(statementsForBlock.size()-1).getEndLine());
			
		}
		} catch(Exception e){
			throw new DuplicationsException(e.getMessage()); 
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
	
}