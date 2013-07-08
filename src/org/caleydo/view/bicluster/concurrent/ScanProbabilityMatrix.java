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

import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.util.execution.SafeCallable;
import org.caleydo.view.bicluster.sorting.ASortingStrategy;

/**
 * @author user
 * 
 */
public class ScanProbabilityMatrix implements SafeCallable<ScanResult> {
	private final float threshold;
	private final Table table;
	private final int bcNr;
	private final ASortingStrategy strategy;

	public ScanProbabilityMatrix(float threshold, Table t, int bcNr,
			ASortingStrategy strat) {
		this.threshold = threshold;
		this.table = t;
		this.bcNr = bcNr;
		this.strategy = strat;
	}

	private ScanResult scanProbTable() {

		final int tablesize = table.depth(); // table.getRowIDList().size();
		double max = 0;
		double min = 6e20;
		for (int nr = 0; nr < tablesize; nr++) {
			float p;
			p = (float) table.getRaw(bcNr, nr);
			if (Math.abs(p) > threshold) {
				strategy.addIndex(nr);
			}
			if (p > max)
				max = p;
			if (p < min)
				min = p;
		}

		List<Integer> indices = new ArrayList<>();
		for (Integer i : strategy) {
			indices.add(i);
		}
		return new ScanResult(indices, max, min);
	}

	@Override
	public ScanResult call() {
		return scanProbTable();

	}

}
