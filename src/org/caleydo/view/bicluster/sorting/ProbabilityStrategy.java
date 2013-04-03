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

/**
 * @author Michael Gillhofer
 *
 */
public class ProbabilityStrategy extends ASortingStrategy {

	private int bcNr;
	protected Table t;

	/**
	 *
	 */
	public ProbabilityStrategy(Table t, int bcNr) {
		this.bcNr = bcNr;
		this.t = t;
		this.indices = new TreeSet<>();
	}

	@Override
	public void setIndices(List<Integer> dimIndices) {
		for (Integer indice : dimIndices) {
			ComparablePair<Float, Integer> pair = new ComparablePair<>((float) t.getRaw(indice, bcNr), indice);
			this.indices.add(pair);
		}
	}

	@Override
	public void addIndex(int index) {
		if (this.indices == null)
			this.indices = new TreeSet<>(new Comparator<Pair<Float, Integer>>() {

				@Override
				public int compare(Pair<Float, Integer> o1, Pair<Float, Integer> o2) {
					return o1.getFirst().compareTo(o2.getFirst());
				}

			});
		ComparablePair<Float, Integer> pair = new ComparablePair<>((float) t.getRaw(bcNr, index), index);
		this.indices.add(pair);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Integer> iterator() {
		return new Iterator<Integer>() {
			final private Iterator<Pair<Float, Integer>> superIter = indices.iterator();

			@Override
			public boolean hasNext() {

				return superIter.hasNext();
			}

			@Override
			public Integer next() {
				Pair<Float, Integer> next = superIter.next();
				// System.out.println(next.getFirst());
				return next.getSecond();
			}

			@Override
			public void remove() {
				superIter.remove();

			}
		};
	}

}