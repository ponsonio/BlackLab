/*******************************************************************************
 * Copyright (c) 2010, 2012 Institute for Dutch Lexicology.
 * All rights reserved.
 *******************************************************************************/
package nl.inl.blacklab.index;

/**
 * Used to report progress while indexing, so we can give feedback to the user.
 */
public class IndexListenerReportConsole extends IndexListener {
	final int REPORT_INTERVAL_SEC = 10;

	long prevCharsDoneReported = 0;

	long prevTokensDoneReported = 0;

	double prevReportTime = 0;

	double curSpeed = -1;

	double curTokensSpeed = -1;

	@Override
	public synchronized void fileStarted(String name) {
		super.fileStarted(name);
		// System.out.println("File started: " + name);
	}

	@Override
	public synchronized void fileDone(String name) {
		super.fileDone(name);
		// System.out.println("File done: " + name);
	}

	@Override
	public synchronized void charsDone(long charsDone) {
		super.charsDone(charsDone);

		double elapsed = getElapsed();
		if (elapsed == 0)
			elapsed = 0.1;
		double secondsSinceLastReport = elapsed - prevReportTime;
		if (secondsSinceLastReport >= REPORT_INTERVAL_SEC) {
			long totalCharsDone = getCharsProcessed();
			long charsDoneSinceLastReport = totalCharsDone - prevCharsDoneReported;

			double lastMbDone = charsDoneSinceLastReport / 1000000.0;
			double lastSpeed = lastMbDone / secondsSinceLastReport;

			double mbDone = totalCharsDone / 1000000.0;
			double overallSpeed = mbDone / elapsed;

			if (curSpeed < 0)
				curSpeed = overallSpeed;
			curSpeed = curSpeed * 0.7 + lastSpeed * 0.3;

			long totalTokensDone = getTokensProcessed();
			long tokensDoneSinceLastReport = totalTokensDone - prevTokensDoneReported;

			double lastKDone = tokensDoneSinceLastReport / 1000.0;
			double lastTokensSpeed = lastKDone / secondsSinceLastReport;

			double kTokensDone = totalTokensDone / 1000.0;
			double overallTokenSpeed = kTokensDone / elapsed;

			if (curTokensSpeed < 0)
				curTokensSpeed = overallTokenSpeed;
			curTokensSpeed = curTokensSpeed * 0.7 + lastTokensSpeed * 0.3;

			System.out
					.printf("%d docs done (%d MB, %dk tokens). Average speed %.1fk tokens/s (%.1f MB/s), currently %.1fk tokens/s (%.1f MB/s)\n",
							docsDone, (int) mbDone, (int) kTokensDone, overallTokenSpeed,
							overallSpeed, curTokensSpeed, curSpeed);

			prevCharsDoneReported = totalCharsDone;
			prevTokensDoneReported = totalTokensDone;
			prevReportTime = elapsed;
		}
	}

	private double getElapsed() {
		return (System.currentTimeMillis() - indexStartTime) / 1000.0;
	}

	@Override
	public void closeEnd() {
		super.closeEnd();
		System.out.println("Closing index complete.");
	}

	@Override
	public void closeStart() {
		super.closeStart();
		System.out.println("Closing index...");
	}

	@Override
	public synchronized void documentDone(String name) {
		super.documentDone(name);
		// System.out.println("Document done: " + name);
	}

	@Override
	public synchronized void documentStarted(String name) {
		super.documentStarted(name);
		// System.out.println("Document started: " + name);
	}

	@Override
	public void indexEnd() {
		super.indexEnd();
		System.out.println("Done indexing.");
	}

	@Override
	public void indexerClosed() {
		super.indexerClosed();
		System.out.println("Indexer closed.");
	}

	@Override
	public void indexerCreated(Indexer indexer) {
		super.indexerCreated(indexer);
		System.out.println("Indexer created.");
	}

	@Override
	public void indexStart() {
		super.indexStart();
		System.out.println("Start indexing.");
	}

	@Override
	public synchronized void luceneDocumentAdded() {
		super.luceneDocumentAdded();
		// System.out.println("Lucene doc added.");
	}

}
