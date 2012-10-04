/*******************************************************************************
 * Copyright (c) 2010, 2012 Institute for Dutch Lexicology.
 * All rights reserved.
 *******************************************************************************/
package nl.inl.blacklab.externalstorage;

import java.io.File;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestContentStoreDir {
	private ContentStore store;

	private File dir;

	String[] str = { "The quick brown fox ", "jumps over the lazy ", "dog.                " };

	public boolean deleteContentStoreDirectory() {
		if (dir.exists()) {
			File[] files = dir.listFiles();
			for (int i = 0; i < files.length; i++) {
				files[i].delete();
			}
		}
		return (dir.delete());
	}

	@Before
	public void setUp() {
		String tempPath = "d:\\temp";
		File tempDir = new File(tempPath);
		if (!tempDir.exists()) {
			tempPath = "c:\\temp";
			tempDir = new File(tempPath);
			if (!tempDir.exists())
				throw new RuntimeException("Directory " + tempPath
						+ " must exist to run this test.");
		}

		dir = new File(tempDir, "testcontentstore");
		if (dir.exists())
			deleteContentStoreDirectory();

		store = new ContentStoreDir(dir);
		((ContentStoreDir) store).setDataFileSizeHint(60); // 60 bytes per data file (1.5 strings)

		// Store strings
		for (int i = 0; i < str.length; i++) {
			Assert.assertEquals(i + 1, store.store(str[i]));
		}
	}

	@After
	public void tearDown() {
		store.close();
		deleteContentStoreDirectory();
	}

	@Test
	public void testRetrieve() {
		// Retrieve strings
		for (int i = 0; i < str.length; i++) {
			Assert.assertEquals(str[i], store.retrieve(i + 1));
		}
	}

	@Test
	public void testRetrievePart() {
		String[] parts = store.retrieveParts(2, new int[] { 5, 15 }, new int[] { 7, 18 });
		Assert.assertEquals(str[1].substring(5, 7), parts[0]);
		Assert.assertEquals(str[1].substring(15, 18), parts[1]);
	}

	@Test
	public void testDelete() {
		store.delete(2);
		Assert.assertNull(store.retrieve(2));
		Assert.assertEquals(str[0], store.retrieve(1));
	}

	@Test
	public void testCloseReopen() {
		store.close();
		store = new ContentStoreDir(dir);
		Assert.assertEquals(str[0], store.retrieve(1));
	}

	@Test
	public void testCloseReopenAppend() {
		store.close();
		store = new ContentStoreDir(dir);
		Assert.assertEquals(4, store.store("test"));
	}
}
