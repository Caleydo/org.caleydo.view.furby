/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.sorting;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.util.collection.Pair.ComparablePair;

import com.google.common.collect.Iterators;

/**
 * @author Michael Gillhofer
 *
 */
public class ProbabilityStrategy extends ASortingStrategy {

	private final int bcNr;
	protected final Table t;

	/**
	 *
	 */
	public ProbabilityStrategy(Table t, int bcNr) {
		this.bcNr = bcNr;
		this.t = t;
		this.indices = new TreeSet<>(new Comparator<Pair<Integer, Float>>() {

			@Override
			public int compare(Pair<Integer, Float> o1, Pair<Integer, Float> o2) {
				int val = o2.getSecond().compareTo(o1.getSecond());
				if (val == 0)
					val = o2.getFirst().compareTo(o1.getFirst());
				return val;
			}

		});
	}

	@Override
	public void setIndices(List<Integer> dimIndices) {
		for (Integer index : dimIndices) {
			ComparablePair<Integer, Float> pair = new ComparablePair<>(index, (float) t.getRaw(index, bcNr));
			this.indices.add(pair);
		}
	}

	@Override
	public void addIndex(int index) {
		ComparablePair<Integer, Float> pair = new ComparablePair<>(index, (float) t.getRaw(bcNr, index));
		indices.add(pair);
		// if (!indices.add(pair))
		// System.out.println(pair.hashCode());
	}


	@Override
	public Iterator<Integer> iterator() {
		return Iterators.transform(indices.iterator(), Pair.<Integer, Float> mapFirst());
	}

}