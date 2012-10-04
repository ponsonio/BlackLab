/*******************************************************************************
 * Copyright (c) 2010, 2012 Institute for Dutch Lexicology.
 * All rights reserved.
 *******************************************************************************/
package nl.inl.blacklab.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.search.BooleanClause.Occur;

/**
 * A TextPattern that performs document-level combining of boolean clauses. Modeled after Lucene's
 * BooleanQuery, each clause can get the MUST, SHOULD or MUST NOT label attached.
 *
 * MUST-clauses must all occur in the document. At least one of all SHOULD clauses must occur in the
 * document. None of the MUST NOT clauses must occur in the document.
 *
 * For documents that satisfy these criteria, all the MUST and SHOULD hits are reported.
 */
public class TextPatternBoolean extends TextPattern {

	private List<TextPattern> must = new ArrayList<TextPattern>();

	private List<TextPattern> should = new ArrayList<TextPattern>();

	private List<TextPattern> mustNot = new ArrayList<TextPattern>();

	public TextPatternBoolean(boolean disableCoord) {
		// do nothing (disableCoord is ignored as we don't use scoring)
	}

	@Override
	public <T> T translate(TextPatternTranslator<T> translator, String fieldName) {

		// First, translate clauses into complete TextPattern. Then translate that.
		TextPattern translated = null;

		// MUST and SHOULD
		TextPattern tpMust = null, tpShould = null;
		if (must.size() > 0) {
			// Build a TextPattern that combines all MUST queries with AND
			tpMust = new TextPatternDocLevelAnd(must.toArray(new TextPattern[0]));
		}
		if (should.size() > 0) {
			// Build a TextPattern that combines all SHOULD queries with OR
			tpShould = new TextPatternOr(should.toArray(new TextPattern[0]));
		}
		if (tpMust == null && tpShould == null)
			throw new RuntimeException("Query must contain included terms (cannot just exclude)");
		else if (tpMust != null && tpShould != null) {
			// Require all MUST queries and at least one of the SHOULD queries
			translated = new TextPatternDocLevelAnd(tpMust, tpShould);
		} else {
			translated = tpMust == null ? tpShould : tpMust;
		}

		// MUST NOT
		if (mustNot.size() > 0) {
			// Build a TextPattern that combines all MUST NOT queries with AND
			translated = new TextPatternDocLevelAndNot(translated, new TextPatternDocLevelAnd(
					mustNot.toArray(new TextPattern[0])));
		}

		return translated.translate(translator, fieldName);
	}

	public void add(TextPattern query, Occur occur) {
		add(new TPBooleanClause(query, occur));
	}

	public void add(TPBooleanClause clause) {
		switch (clause.getOccur()) {
		case MUST:
			must.add(clause.getQuery());
			break;
		case SHOULD:
			should.add(clause.getQuery());
			break;
		case MUST_NOT:
			mustNot.add(clause.getQuery());
			break;
		}
	}

}
