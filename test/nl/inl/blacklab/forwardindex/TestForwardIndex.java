/*******************************************************************************
 * Copyright (c) 2010, 2012 Institute for Dutch Lexicology.
 * All rights reserved.
 *******************************************************************************/
package nl.inl.blacklab.forwardindex;

import java.io.File;
import java.util.Arrays;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestForwardIndex {
	private ForwardIndex fi;

	private File dir;

	String[][] str = { { "How", "much", "wood" }, { "would", "a", "woodchuck", "chuck" },
			{ "if", "a", "woodchuck", "could", "chuck", "wood" } };

	public boolean deleteFIDirectory() {
		if (dir.exists()) {
			File[] files = dir.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (!files[i].delete())
					throw new RuntimeException("Could not delete " + files[i]);
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

		dir = new File(tempDir, "testforwardindex");
		if (dir.exists())
			deleteFIDirectory();

		fi = new ForwardIndex(dir, true, true);
		try {
			// Store strings
			for (int i = 0; i < str.length; i++) {
				Assert.assertEquals(i, fi.addDocument(Arrays.asList(str[i])));
			}
		} finally {
			fi.close(); // close so everything is guaranteed to be written
		}
		fi = new ForwardIndex(dir);
	}

	@After
	public void tearDown() {
		if (fi != null)
			fi.close();
		deleteFIDirectory();
	}

	@Test
	public void testRetrieve() {
		// Retrieve strings
		for (int i = 0; i < str.length; i++) {
			Assert.assertEquals(Arrays.asList(str[i]), Arrays.asList(fi.retrievePart(i, -1, -1)));
		}
	}

}
