/*******************************************************************************
 * Copyright (c) 2010, 2012 Institute for Dutch Lexicology.
 * All rights reserved.
 *******************************************************************************/
package nl.inl.blacklab.search.lucene;

import java.io.IOException;
import java.util.Collection;

import nl.inl.blacklab.search.Hit;
import nl.inl.blacklab.search.grouping.HitProperty;

import org.apache.lucene.search.spans.Spans;

/**
 * Filter a Spans with a HitProperty object to show the results in a single group.
 *
 * This allows us to only consider certain documents (say, only documents in a certain domain) when
 * executing our query.
 */
public class SpansFilteredHitProperty extends SpansWithHit {
	Spans spans;

	HitProperty prop;

	boolean more;

	private String value;

	public SpansFilteredHitProperty(Spans spans, HitProperty prop, String value) {
		this.spans = spans;
		this.prop = prop;
		this.value = value;
		more = true;
	}

	private boolean synchronize() throws IOException {
		while (more && !prop.get(Hit.getHit(spans)).equals(value)) {
			more = spans.next();
		}
		return more;
	}

	@Override
	public boolean next() throws IOException {
		if (!more)
			return false;
		more = spans.next();
		return synchronize();
	}

	@Override
	public boolean skipTo(int target) throws IOException {
		if (!more)
			return false;
		more = spans.skipTo(target);
		return synchronize();
	}

	@Override
	public int doc() {
		return spans.doc();
	}

	@Override
	public int end() {
		return spans.end();
	}

	@Override
	public Collection<byte[]> getPayload() throws IOException {
		return spans.getPayload();
	}

	@Override
	public boolean isPayloadAvailable() {
		return spans.isPayloadAvailable();
	}

	@Override
	public int start() {
		return spans.start();
	}

	@Override
	public Hit getHit() {
		return Hit.getHit(spans);
	}

}
