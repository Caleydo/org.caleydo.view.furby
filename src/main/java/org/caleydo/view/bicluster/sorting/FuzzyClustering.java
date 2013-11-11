/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.sorting;

import static org.caleydo.view.bicluster.internal.prefs.MyPreferences.UNBOUND_NUMBER;

import java.util.Iterator;
import java.util.List;

import org.caleydo.core.util.function.DoubleSizedIterables;
import org.caleydo.core.util.function.IDoubleFunction;
import org.caleydo.core.util.function.IDoubleSizedIterable;
import org.caleydo.core.util.function.IDoubleSizedIterator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;

/**
 * clustering for a specific clusterer in one dimension
 *
 * @author Samuel Gratzl
 *
 */
public final class FuzzyClustering implements IDoubleSizedIterable {
	private final static IntFloat ZERO = new IntFloat(0, 0);

	/**
	 * index,probability sorted by probability
	 */
	private final ImmutableSortedSet<IntFloat> probabilities;

	public FuzzyClustering(ImmutableSortedSet<IntFloat> probabilities) {
		this.probabilities = probabilities;
	}

	public float getAbsMinValue() {
		IntFloat a = probabilities.ceiling(ZERO); // >= 0
		IntFloat b = probabilities.floor(ZERO); // <= 0
		float a_f = a == null ? Float.POSITIVE_INFINITY : a.getProbability();
		float b_f = b == null ? Float.POSITIVE_INFINITY : -b.getProbability();
		return Math.min(a_f, b_f);
	}

	public float getAbsMaxValue() {
		IntFloat a = probabilities.last(); // largest positive
		IntFloat b = probabilities.first(); // largest negative
		float a_f = a == null ? Float.NEGATIVE_INFINITY : a.getProbability();
		float b_f = b == null ? Float.NEGATIVE_INFINITY : -b.getProbability();
		return Math.max(a_f, b_f);
	}

	public ImmutableList<IntFloat> negatives(float threshold, int maxElements) {
		ImmutableSortedSet<IntFloat> r = probabilities.headSet(new IntFloat(Integer.MAX_VALUE, -Math.abs(threshold)),
				true);
		ImmutableList<IntFloat> l = r.asList();

		if (l.size() <= maxElements || maxElements == UNBOUND_NUMBER)
			return l;
		// the larger the index the nearer to zero to less interesting
		return l.subList(0, maxElements);

	}

	public ImmutableList<IntFloat> positives(float threshold, int maxElements) {
		ImmutableSortedSet<IntFloat> r = probabilities.tailSet(new IntFloat(Integer.MIN_VALUE, Math.abs(threshold)),
				true);
		ImmutableList<IntFloat> l = r.asList();

		if (l.size() <= maxElements || maxElements == UNBOUND_NUMBER)
			return l;
		// the lower the index the nearer to zero to less interesting
		return l.subList(l.size() - maxElements, l.size());
	}

	public List<IntFloat> filter(float threshold, int maxElements, EThresholdMode mode) {
		if (threshold == 0 && maxElements == UNBOUND_NUMBER)
			return probabilities.asList();
		ImmutableList<IntFloat> negatives = mode.includeNegatives() ? negatives(threshold, maxElements) : ImmutableList
				.<IntFloat> of();
		ImmutableList<IntFloat> positives = mode.includePositives() ? positives(threshold, maxElements) : ImmutableList
				.<IntFloat> of();
		if (maxElements == UNBOUND_NUMBER || (negatives.size() + positives.size()) <= maxElements) //just add negatives and positives
			return ConcatedList.concat(negatives, positives);

		if (negatives.isEmpty()) {
			return positives.subList(Math.max(0, positives.size() - maxElements), positives.size());
		}
		if (positives.isEmpty())
			return negatives.subList(0, Math.min(negatives.size(), maxElements));

		// take the abs top X elements
		Iterator<IntFloat> negIt = negatives.iterator();
		Iterator<IntFloat> posIt = Lists.reverse(positives).iterator();
		IntFloat negEnd = null;
		IntFloat negAct = negIt.next();
		IntFloat posStart = null;
		IntFloat posAct = posIt.next();

		for(int i = 0; i < maxElements; ++i) {
			if (negAct == null || (posAct != null && posAct.getProbability() > -negAct.getProbability())) {
				posStart = posAct;
				posAct = posIt.hasNext() ? posIt.next() : null;
			} else {
				negEnd = negAct;
				negAct = negIt.hasNext() ? negIt.next() : null;
			}
		}

		ImmutableSortedSet<IntFloat> headSet = negEnd == null ? ImmutableSortedSet.<IntFloat> of() : probabilities
				.headSet(negEnd, true);
		ImmutableSortedSet<IntFloat> tailSet = posStart == null ? ImmutableSortedSet.<IntFloat> of() : probabilities
				.tailSet(posStart, true);
		return ConcatedList.concat(headSet.asList(), tailSet.asList());
	}

	@Override
	public int size() {
		return this.size();
	}

	@Override
	public IDoubleSizedIterator iterator() {
		return new IDoubleSizedIterator() {
			private final Iterator<IntFloat> it = probabilities.iterator();
			private final int size = probabilities.size();

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Double next() {
				return nextPrimitive();
			}

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public double nextPrimitive() {
				return it.next().getProbability();
			}

			@Override
			public int size() {
				return size;
			}
		};
	}

	@Override
	public IDoubleSizedIterable map(IDoubleFunction f) {
		return DoubleSizedIterables.map(this, f);
	}
}
