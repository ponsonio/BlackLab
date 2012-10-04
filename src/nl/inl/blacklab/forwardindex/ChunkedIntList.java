/*******************************************************************************
 * Copyright (c) 2010, 2012 Institute for Dutch Lexicology.
 * All rights reserved.
 *******************************************************************************/
package nl.inl.blacklab.forwardindex;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Memory-efficient list of integers.
 *
 * Works by keeping a list of chunks. Each chunk is an array of a fixed size (the chunk size).
 * Together the chunks form the storage for the list.
 *
 * Add and remove operations in the middle of the list are relatively expensive as we may have to
 * shift elements over several chunks. Add and remove at the end should be fast, though.
 *
 * Advantage over ArrayList is that we don't have to reallocate when the array runs out.
 * Disadvantage is slighter slower lookup speed (first we have to determine the chunk the element is
 * in)
 */
public class ChunkedIntList extends AbstractList<Integer> {
	final static int DEFAULT_CHUNK_SIZE = 250000; // 1M per array

	List<int[]> chunks = new ArrayList<int[]>();

	/**
	 * How many int elements are in each chunk
	 */
	private int chunkSize;

	/**
	 * The size of the list (not the capacity of the chunks, the actual filled size).
	 */
	int size = 0;

	public ChunkedIntList(int chunkSize) {
		this.chunkSize = chunkSize;
	}

	public ChunkedIntList() {
		this(DEFAULT_CHUNK_SIZE);
	}

	public ChunkedIntList(Collection<Integer> coll) {
		this();
		addAll(coll);
	}

	public ChunkedIntList(Collection<Integer> coll, int chunkSize) {
		this(chunkSize);
		addAll(coll);
	}

	@Override
	public Integer get(int index) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException();
		int chunkNo = index / chunkSize;
		int indexInChunk = index % chunkSize;
		return chunks.get(chunkNo)[indexInChunk];
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public Integer set(int index, Integer element) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException();
		int chunkNo = index / chunkSize;
		int indexInChunk = index % chunkSize;
		Integer prevEl = chunks.get(chunkNo)[indexInChunk];
		chunks.get(chunkNo)[indexInChunk] = element;
		return prevEl;
	}

	@Override
	public void add(int index, Integer element) {
		if (index < 0 || index > size)
			throw new IndexOutOfBoundsException();

		// See if all chunks are full
		if (size / chunkSize == chunks.size()) {
			// Yes, we need a new one
			chunks.add(new int[chunkSize]);
		}

		// Move elements to make room
		int targetChunk = index / chunkSize;
		int indexInTargetChunk = index % chunkSize;
		int chunkContainingLastElement = (size - 1) / chunkSize;
		int chunkContainingFirstUnusedSlot = size / chunkSize;
		for (int i = chunkContainingLastElement; i >= targetChunk; i--) {
			int[] chunk = chunks.get(i);

			// Find chunk size
			int thisChunkSize = chunkSize;
			if (i == chunkContainingFirstUnusedSlot) {
				// Chunk is not full.
				thisChunkSize = size % chunkSize;
			} else {
				// Full chunk. Move last element in chunk to next chunk
				chunks.get(i + 1)[0] = chunk[chunkSize - 1];
			}

			// Move rest of the elements in chunk one position forward
			int srcPos = 0;
			if (i == targetChunk)
				srcPos = indexInTargetChunk;
			int length = thisChunkSize - srcPos - (i == chunkContainingFirstUnusedSlot ? 0 : 1);
			if (length > 0)
				System.arraycopy(chunk, srcPos, chunk, srcPos + 1, length);
		}

		// Finally, set the element and increment the list size
		chunks.get(targetChunk)[indexInTargetChunk] = element;
		size++;
	}

	@Override
	public Integer remove(int index) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException();

		// Move elements to make room
		int targetChunk = index / chunkSize;
		int indexInTargetChunk = index % chunkSize;
		int chunkContainingLastElement = (size - 1) / chunkSize;
		int chunkContainingFirstUnusedSlot = size / chunkSize;

		// Save deleted element to return it
		Integer deleted = chunks.get(targetChunk)[indexInTargetChunk];

		// Move elements backward
		for (int i = targetChunk; i <= chunkContainingLastElement; i++) {
			int[] chunk = chunks.get(i);

			// Find chunk size
			int thisChunkSize = chunkSize;
			if (i == chunkContainingFirstUnusedSlot) {
				// Chunk is not full.
				thisChunkSize = size % chunkSize;
			}

			// Move elements in chunk one position backward
			int srcPos = 1;
			if (i == targetChunk)
				srcPos = indexInTargetChunk + 1;
			int length = thisChunkSize - srcPos;
			if (length > 0)
				System.arraycopy(chunk, srcPos, chunk, srcPos - 1, length);

			if (i != chunkContainingLastElement) {
				// Next chunk has at least one element. Move first element to last position of this
				// chunk.
				chunk[chunkSize - 1] = chunks.get(i + 1)[0];
			}
		}

		size--;

		return deleted;
	}

}
