package org.sonar.duplications.api.index.store;
 
public class StorageException extends RuntimeException {

	public StorageException(String message) {
		super(message);
	}

	public StorageException(String message, Throwable t) {
		super(message, t);
	}

	public StorageException(Throwable t) {
		super(t);
	}
}
