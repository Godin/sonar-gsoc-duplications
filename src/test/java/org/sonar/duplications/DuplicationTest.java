package org.sonar.duplications;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.sonar.duplications.index.Clone;
import org.sonar.duplications.index.MemoryCloneIndex;
import org.sonar.duplications.java.JavaCloneFinder;

public class DuplicationTest {

	MemoryCloneIndex mci = new MemoryCloneIndex();
	File file1 = DuplicationsTestUtil
			.findFile("/org/sonar/duplications/cpd/CPDTest/CPDFile1.java");
	File file2 = DuplicationsTestUtil
			.findFile("/org/sonar/duplications/cpd/CPDTest/CPDFile2.java");
	File file3 = DuplicationsTestUtil
			.findFile("/org/sonar/duplications/cpd/CPDTest/CPDFile3.java");
	File dir = DuplicationsTestUtil
			.findFile("/org/sonar/duplications/cpd/CPDTest/");

	CloneFinder cf = JavaCloneFinder.build(mci); 

	@Test
	public void shouldFindDuplicateInFile() {

		initTestData(file1, file2);
		
		cf.addSourceFileForDetection(file1.getAbsolutePath());
		
		List<Clone> cloneList = cf.findClones();

		assertThat(cloneList.size(), is(1));
		
		//check the first one
		assertThat(cloneList.get(0).getCloneParts().size(), is(2) );
		
		assertThat(cloneList.get(0).getCloneParts().get(0).getResourceId(), is(file1.getAbsolutePath()));
		assertThat(cloneList.get(0).getCloneParts().get(0).getLineStart(), is(6) );
		assertThat(cloneList.get(0).getCloneParts().get(0).getLineEnd(), is(11) );
		
		assertThat(cloneList.get(0).getCloneParts().get(1).getResourceId(), is(file2.getAbsolutePath()));
		assertThat(cloneList.get(0).getCloneParts().get(1).getLineStart(), is(28) );
		assertThat(cloneList.get(0).getCloneParts().get(1).getLineEnd(), is(33) );
		
	}
	
	
	@Test
	public void shouldFindDuplicateInDirectory() {

		initTestData(file1, file2, file3);
		
		cf.addSourceDirectoryForDetection(dir.getAbsolutePath());
		
		List<Clone> cloneList = cf.findClones();

		assertThat(cloneList.size(), is(4));
		
		//check the first one
		assertThat(cloneList.get(0).getCloneParts().size(), is(3) );
		
		assertThat(cloneList.get(0).getCloneParts().get(0).getResourceId(), is(file1.getAbsolutePath()));
		assertThat(cloneList.get(0).getCloneParts().get(0).getLineStart(), is(6) );
		assertThat(cloneList.get(0).getCloneParts().get(0).getLineEnd(), is(9) );
		
		assertThat(cloneList.get(0).getCloneParts().get(1).getResourceId(), is(file2.getAbsolutePath()));
		assertThat(cloneList.get(0).getCloneParts().get(1).getLineStart(), is(28) );
		assertThat(cloneList.get(0).getCloneParts().get(1).getLineEnd(), is(31) );
		
		assertThat(cloneList.get(0).getCloneParts().get(2).getResourceId(), is(file3.getAbsolutePath()));
		assertThat(cloneList.get(0).getCloneParts().get(2).getLineStart(), is(28) );
		assertThat(cloneList.get(0).getCloneParts().get(2).getLineEnd(), is(31) );
		
		//check the last one
		assertThat(cloneList.get(cloneList.size()-1).getCloneParts().size(), is(2) );
		
		assertThat(cloneList.get(cloneList.size()-1).getCloneParts().get(0).getResourceId(), is(file2.getAbsolutePath()) );
		assertThat(cloneList.get(cloneList.size()-1).getCloneParts().get(0).getLineStart(), is(33) );
		assertThat(cloneList.get(cloneList.size()-1).getCloneParts().get(0).getLineEnd(), is(47) );
		
		assertThat(cloneList.get(cloneList.size()-1).getCloneParts().get(1).getResourceId(), is(file3.getAbsolutePath()) );
		assertThat(cloneList.get(cloneList.size()-1).getCloneParts().get(1).getLineStart(), is(31) );
		assertThat(cloneList.get(cloneList.size()-1).getCloneParts().get(1).getLineEnd(), is(45) );
		
	}
	
	private void initTestData(File... files) {
		mci.removeAll();
		cf.register(file1, file2);
	}
}
