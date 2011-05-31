package org.sonar.duplications.api.index.store;

import java.io.Serializable;
import java.util.List;

import org.sonar.duplications.api.codeunit.block.Block;

public interface ICloneIndexStore {

	Serializable getOption(String key) throws StorageException;

	void setOption(String key, Serializable value) throws StorageException;

	void open() throws StorageException;

	void close() throws StorageException;

	void batchInsertBlocks(List<Block> blocks);
}
