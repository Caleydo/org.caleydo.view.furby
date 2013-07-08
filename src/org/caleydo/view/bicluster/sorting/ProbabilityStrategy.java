/*******************************************************************************
 * Caleydo - visualization for molecular biology - http://caleydo.org
 *
 * Copyright(C) 2005, 2012 Graz University of Technology, Marc Streit, Alexander
 * Lex, Christian Partl, Johannes Kepler University Linz </p>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 *******************************************************************************/
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