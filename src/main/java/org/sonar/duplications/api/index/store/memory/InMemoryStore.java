package org.sonar.duplications.api.index.store.memory;

import java.io.Serializable;
import java.util.List;

import org.sonar.duplications.api.codeunit.block.Block;
import org.sonar.duplications.api.index.store.ICloneIndexStore;
import org.sonar.duplications.api.index.store.StorageException;

//sample class
public class InMemoryStore implements ICloneIndexStore {

	public Serializable getOption(String key) throws StorageException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setOption(String key, Serializable value)
			throws StorageException {
		// TODO Auto-generated method stub

	}

	public void open() throws StorageException {
		// TODO Auto-generated method stub

	}

	public void close() throws StorageException {
		// TODO Auto-generated method stub

	}

	public void batchInsertBlocks(List<Block> blocks) {
		// TODO Auto-generated method stub

	}

}
