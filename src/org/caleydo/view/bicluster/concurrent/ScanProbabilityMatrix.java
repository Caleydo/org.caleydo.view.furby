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
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.util.collection.Pair;

/**
 * @author user
 *
 */
public class ScanProbabilityMatrix implements Callable<ArrayList<Integer>> {

	private float threshold;
	private Table table;
	private int bcNr;


	public ScanProbabilityMatrix(float threshold, Table t, int bcNr) {
		this.threshold = threshold;
		this.table = t;
		this.bcNr = bcNr;
	}

	private ArrayList<Integer> scanProbTable() {
		Set<Pair<Integer, Float>> indicesList = new TreeSet<Pair<Integer, Float>>(
				new Comparator<Pair<Integer, Float>>() {

					@Override
					public int compare(Pair<Integer, Float> o1, Pair<Integer, Float> o2) {
						return o1.getSecond().compareTo(o2.getSecond());
					}

				});
		for (int nr = 0; nr < table.getRowIDList().size(); nr++) {
			Pair<Integer, Float> pair;
			float p;
			synchronized (table) {
				p = (float) table.getRaw(bcNr, nr);
			}
			if (p > threshold) {
				pair = new Pair<>(nr, p);
				indicesList.add(pair);
			}
		}
		ArrayList<Integer> indices = new ArrayList<>(indicesList.size());
		for (Pair<Integer, Float> p : indicesList) {
			indices.add(p.getFirst());
		}
		return indices;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public ArrayList<Integer> call() throws Exception {
		return scanProbTable();

	}

}
