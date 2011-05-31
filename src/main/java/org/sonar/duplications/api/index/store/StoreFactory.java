package org.sonar.duplications.api.index.store;

import org.sonar.duplications.api.index.store.memory.InMemoryStore;

//sample class
public class StoreFactory {
	public static ICloneIndexStore getStore(StoreStrategy strategy){
		return new InMemoryStore();
	}
}
