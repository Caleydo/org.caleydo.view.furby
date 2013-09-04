/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.sorting;

import static org.caleydo.view.bicluster.internal.prefs.MyPreferences.UNBOUND_NUMBER;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
/**
 * @author Samuel Gratzl
 *
 */
public final class FuzzyClustering {
	private final static IntFloat ZERO = new IntFloat(0, 0);

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

}
