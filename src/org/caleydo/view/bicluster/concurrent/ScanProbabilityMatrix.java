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
package org.caleydo.view.bicluster.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.caleydo.core.data.collection.table.Table;
import org.caleydo.view.bicluster.sorting.ASortingStrategy;

/**
 * @author user
 *
 */
public class ScanProbabilityMatrix implements Callable<List<Integer>> {

	private float threshold;
	private Table table;
	private int bcNr;
	private ASortingStrategy strategy;

	public ScanProbabilityMatrix(float threshold, Table t, int bcNr, ASortingStrategy strat) {
		this.threshold = threshold;
		this.table = t;
		this.bcNr = bcNr;
		this.strategy = strat;
	}

	private List<Integer> scanProbTable() {
		// Comparator<Pair<Integer, Float>> cmp = new Comparator<Pair<Integer, Float>>() {
		//
		// @Override
		// public int compare(Pair<Integer, Float> o1, Pair<Integer, Float> o2) {
		//
		// return o2.getSecond().compareTo(o1.getSecond());
		// }
		//
		// };
		// Set<Pair<Integer, Float>> indicesList = new TreeSet<Pair<Integer, Float>>(cmp);
		final int tablesize = table.depth(); // table.getRowIDList().size();
		for (int nr = 0; nr < tablesize; nr++) {
			float p;
			p = (float) table.getRaw(bcNr, nr);
			if (Math.abs(p) > threshold) {
				strategy.addIndex(nr);
			}
		}
		List<Integer> indices = new ArrayList<>();
		for (Integer i : strategy) {
			indices.add(i);
		}
		return indices;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public List<Integer> call() throws Exception {
		return scanProbTable();

	}

}
